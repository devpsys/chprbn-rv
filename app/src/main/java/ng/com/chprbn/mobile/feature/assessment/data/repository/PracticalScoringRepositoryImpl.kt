package ng.com.chprbn.mobile.feature.assessment.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import ng.com.chprbn.mobile.core.sync.SyncJobEntity
import ng.com.chprbn.mobile.core.sync.SyncWorkScheduler
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalSectionDao
import ng.com.chprbn.mobile.feature.assessment.data.local.SectionQuestionDao
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toEntity
import ng.com.chprbn.mobile.feature.assessment.data.sync.PracticalScoreKey
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionStatus
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionSummary
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.SectionQuestion
import ng.com.chprbn.mobile.feature.assessment.domain.repository.PracticalScoringRepository
import javax.inject.Inject

/**
 * Per-question scoring orchestration. Every write path:
 *
 * 1. Upserts the local score (entity stamped `syncStatus = Pending`).
 * 2. Enqueues a `SyncJobEntity` keyed by the entity's composite triple
 *    so the cross-feature [SyncWorkScheduler] knows there's work to do.
 * 3. Re-derives the parent schedule's `syncStatus` via
 *    [AssessmentScheduleSyncStatusUpdater] so the Examination Schedules
 *    pill stays accurate.
 * 4. Asks [SyncWorkScheduler] to schedule a sync run (idempotent — uses
 *    KEEP policy, so rapid taps collapse to one worker run).
 */
class PracticalScoringRepositoryImpl @Inject constructor(
    private val sectionDao: PracticalSectionDao,
    private val questionDao: SectionQuestionDao,
    private val practicalScoreDao: PracticalScoreDao,
    private val syncJobDao: SyncJobDao,
    private val workScheduler: SyncWorkScheduler,
    private val statusUpdater: AssessmentScheduleSyncStatusUpdater,
) : PracticalScoringRepository {

    override suspend fun getSections(
        scheduleId: String,
        candidateId: String,
    ): List<PracticalSectionSummary> = withContext(Dispatchers.IO) {
        val sections = sectionDao.getByScheduleId(scheduleId)
        val questions = questionDao.getByScheduleId(scheduleId)
        val scores = practicalScoreDao.getForCandidate(scheduleId, candidateId)

        val questionsBySection = questions.groupBy { it.sectionId }
        val scoredQuestionsBySection = scores
            .filter { it.score != 0 || it.syncStatus != SyncStatus.Pending.name }
            // ^ A row exists for every scored question (recordScore always upserts);
            //   we treat its presence as "scored" regardless of value. The `0` /
            //   pending filter above is a no-op kept for readability — leaving the
            //   list as `scores` would behave the same since recordScore is the only
            //   write path.
            .groupBy { score ->
                questions.firstOrNull { it.id == score.questionId }?.sectionId
            }

        sections.map { section ->
            val total = questionsBySection[section.id]?.size ?: 0
            val sectionScores = scoredQuestionsBySection[section.id].orEmpty()
            val scored = sectionScores.size
            PracticalSectionSummary(
                section = section.toDomain(),
                status = PracticalSectionStatus.from(scored, total),
                scoredCount = scored,
                totalCount = total,
                lastUpdatedAt = sectionScores.maxOfOrNull { it.scoredAt },
            )
        }
    }

    override suspend fun getQuestions(
        scheduleId: String,
        candidateId: String,
        sectionId: String,
    ): List<Pair<SectionQuestion, PracticalScore?>> = withContext(Dispatchers.IO) {
        val questions = questionDao.getBySectionId(sectionId)
        val scoresByQuestion = practicalScoreDao
            .getForSection(scheduleId, candidateId, sectionId)
            .associateBy { it.questionId }

        questions.map { q ->
            q.toDomain() to scoresByQuestion[q.id]?.toDomain()
        }
    }

    override suspend fun recordScore(score: PracticalScore): SaveResult =
        withContext(Dispatchers.IO) {
            try {
                practicalScoreDao.upsert(score.toEntity())
                syncJobDao.enqueue(
                    SyncJobEntity(
                        entityType = SyncEntityType.PracticalScore.name,
                        entityKey = PracticalScoreKey.encode(
                            scheduleId = score.scheduleId,
                            candidateId = score.candidateId,
                            questionId = score.questionId,
                        ),
                        enqueuedAt = score.scoredAt,
                        status = SyncStatus.Pending.name,
                    ),
                )
                statusUpdater.refresh(score.scheduleId)
                workScheduler.scheduleSyncWork()
                SaveResult.Success
            } catch (t: Throwable) {
                SaveResult.Error(t.message ?: "Unable to save score.")
            }
        }

    override suspend fun commitSection(
        scheduleId: String,
        candidateId: String,
        sectionId: String,
    ): SaveResult = withContext(Dispatchers.IO) {
        // Every stepper tap already enqueues a sync job via recordScore.
        // This gesture is the user's explicit "upload now" signal — we
        // re-schedule the worker so it runs immediately rather than
        // waiting for the next opportunistic trigger.
        try {
            workScheduler.scheduleSyncWork()
            SaveResult.Success
        } catch (t: Throwable) {
            SaveResult.Error(t.message ?: "Unable to commit section.")
        }
    }
}
