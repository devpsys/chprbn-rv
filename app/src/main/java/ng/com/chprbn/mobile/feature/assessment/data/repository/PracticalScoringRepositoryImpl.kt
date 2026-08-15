package ng.com.chprbn.mobile.feature.assessment.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import ng.com.chprbn.mobile.core.sync.SyncJobEntity
import ng.com.chprbn.mobile.core.sync.SyncWorkScheduler
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreEntity
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalSectionDao
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalSectionEntity
import ng.com.chprbn.mobile.feature.assessment.data.local.SectionQuestionDao
import ng.com.chprbn.mobile.feature.assessment.data.local.SectionQuestionEntity
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
 * 3. Asks [SyncWorkScheduler] to schedule a sync run (idempotent — uses
 *    KEEP policy, so rapid taps collapse to one worker run).
 *
 * The schedules-list "sync status" pill is now derived at read time in
 * `AssessmentScheduleRepositoryImpl.getSchedules` (the persisted status
 * column was removed alongside `assessment_schedules`) — no explicit
 * refresh step here.
 */
class PracticalScoringRepositoryImpl @Inject constructor(
    private val sectionDao: PracticalSectionDao,
    private val questionDao: SectionQuestionDao,
    private val practicalScoreDao: PracticalScoreDao,
    private val syncJobDao: SyncJobDao,
    private val workScheduler: SyncWorkScheduler,
) : PracticalScoringRepository {

    override suspend fun getSections(
        scheduleId: String,
        candidateId: String,
    ): List<PracticalSectionSummary> = withContext(Dispatchers.IO) {
        val sections = sectionDao.getByScheduleId(scheduleId)
        val questions = questionDao.getByScheduleId(scheduleId)
        val scores = practicalScoreDao.getForCandidate(scheduleId, candidateId)
        buildSummaries(sections, questions, scores)
    }

    override fun observeSections(
        scheduleId: String,
        candidateId: String,
    ): Flow<List<PracticalSectionSummary>> = flow {
        // Sections + questions come from the (immutable) downloaded package,
        // so we read them once per emission — cheap. The score flow is the
        // change signal; each score upsert emits a fresh list.
        val sections = sectionDao.getByScheduleId(scheduleId)
        val questions = questionDao.getByScheduleId(scheduleId)
        emitAll(
            practicalScoreDao.observeForCandidate(scheduleId, candidateId).map { scores ->
                buildSummaries(sections, questions, scores)
            },
        )
    }.flowOn(Dispatchers.IO)

    private fun buildSummaries(
        sections: List<PracticalSectionEntity>,
        questions: List<SectionQuestionEntity>,
        scores: List<PracticalScoreEntity>,
    ): List<PracticalSectionSummary> {
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

        return sections.map { section ->
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

    override fun observeStartedCandidateCount(scheduleId: String): Flow<Int> =
        practicalScoreDao.observeStartedCandidateCountForSchedule(scheduleId)

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
            // Enqueue first, then upsert. Scores live in AssessmentDatabase and
            // sync jobs in SyncDatabase, so we cannot share a Room transaction.
            // See AttendanceRepositoryImpl.markAttendance for the full rationale
            // + self-heal contract (PracticalScoreSyncHandler drops ghost jobs
            // whose local row is missing).
            try {
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
                practicalScoreDao.upsert(score.toEntity())
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
