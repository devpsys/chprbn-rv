package ng.com.chprbn.mobile.feature.assessment.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentCandidateDao
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentCandidateRow
import ng.com.chprbn.mobile.feature.assessment.domain.model.ScoreLevel
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentCandidateRepository
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamCandidateRepository
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Reads the candidate list + assigned-count for a PE/PA schedule.
 *
 * The assessment `schedule_candidate_assignments` table is no longer
 * populated (the per-schedule package download is gone). The live roster
 * is the exam dossier's `paper_candidate_assignments` (schedule id ==
 * exam paper id). When the assessment table still has leftover rows we
 * prefer them; otherwise we fall back to exam.db.
 *
 * The directory score is the project mark. It lives in assessment.db
 * (`project_scores`) and is stamped in the JVM after the roster read —
 * the two files can't be SQL-joined.
 */
class AssessmentCandidateRepositoryImpl @Inject constructor(
    private val candidateDao: AssessmentCandidateDao,
    private val examCandidateRepository: ExamCandidateRepository,
    private val projectScoreDao: ProjectScoreDao,
) : AssessmentCandidateRepository {

    override suspend fun getCandidates(
        scheduleId: String,
        query: String,
        lowScoreThreshold: Int,
    ): List<AssessmentCandidateRow> = withContext(Dispatchers.IO) {
        val likeArg = if (query.isBlank()) "" else "%${escapeLike(query.trim())}%"
        val local = candidateDao.rowsForSchedule(scheduleId, likeArg)
            .map { it.toDomain(lowScoreThreshold) }
        val rows = if (local.isNotEmpty()) {
            local
        } else {
            val trimmedQuery = query.trim()
            examCandidateRepository.getAssignedCandidates(scheduleId)
                .filter { c -> trimmedQuery.isEmpty() || c.matches(trimmedQuery) }
                .map { it.toShellRow(lowScoreThreshold) }
        }
        return@withContext rows.withScores(scheduleId, lowScoreThreshold)
    }

    override suspend fun getCandidate(
        scheduleId: String,
        candidateId: String,
    ): Candidate? = withContext(Dispatchers.IO) {
        candidateDao.getForSchedule(scheduleId, candidateId)?.toDomain()
            ?: examCandidateRepository.getCandidateById(candidateId)
    }

    override suspend fun getCandidateByExamNumber(
        scheduleId: String,
        examNumber: String,
    ): Candidate? = withContext(Dispatchers.IO) {
        candidateDao.getForScheduleByExamNumber(scheduleId, examNumber)?.toDomain()
            ?: examCandidateRepository.getCandidateForPaperByExamNumber(scheduleId, examNumber)
    }

    override fun observeAssignedCount(scheduleId: String): Flow<Int> =
        combine(
            candidateDao.observeAssignedCandidateCount(scheduleId),
            examCandidateRepository.observeAssignedCandidateCount(scheduleId),
        ) { assessment, exam -> if (assessment > 0) assessment else exam }

    private fun Candidate.matches(query: String): Boolean =
        fullName.contains(query, ignoreCase = true) ||
            examNumber.contains(query, ignoreCase = true)

    private fun Candidate.toShellRow(threshold: Int): AssessmentCandidateRow =
        AssessmentCandidateRow(
            candidate = this,
            aggregateScore = 0,
            level = ScoreLevel.fromScore(0, threshold),
            scoredQuestions = 0,
            totalQuestions = 0,
            // No scores exist yet, so nothing can be Pending/Failed.
            // Synced is the vacuous no-writes state.
            syncStatus = SyncStatus.Synced,
        )

    /**
     * Overlay the rounded project score onto each roster row. Candidates
     * without a project row keep their existing aggregate (0 on the exam
     * fallback path).
     */
    private suspend fun List<AssessmentCandidateRow>.withScores(
        scheduleId: String,
        threshold: Int,
    ): List<AssessmentCandidateRow> {
        if (isEmpty()) return this
        val projectByCandidate = projectScoreDao.getForSchedule(scheduleId)
            .associate { it.candidateId to it.score.roundToInt() }
        if (projectByCandidate.isEmpty()) return this
        return map { row ->
            val project = projectByCandidate[row.candidate.id] ?: return@map row
            if (project == row.aggregateScore) row
            else row.copy(
                aggregateScore = project,
                level = ScoreLevel.fromScore(project, threshold),
            )
        }
    }

    // SQLite LIKE treats `%`, `_`, and `\` specially. Escape them so a
    // search for "100%" doesn't match every candidate.
    private fun escapeLike(input: String): String = buildString {
        for (ch in input) when (ch) {
            '%', '_', '\\' -> { append('\\'); append(ch) }
            else -> append(ch)
        }
    }
}
