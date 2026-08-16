package ng.com.chprbn.mobile.feature.assessment.domain.repository

import kotlinx.coroutines.flow.Flow
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentCandidateRow
import ng.com.chprbn.mobile.feature.assessment.domain.model.ScoreLevel

/**
 * Read side of the per-schedule candidate roster. The list query supports
 * a free-text filter applied to `fullName` and `examNumber` server-side
 * (i.e. inside the SQL `WHERE`) so the screen doesn't pull the full cohort
 * into the JVM just to filter.
 *
 * `lowScoreThreshold` is forwarded to the data layer's projection mapper
 * to drive the `ScoreLevel.Low` / `Normal` band; defaulted to
 * `ScoreLevel.DEFAULT_LOW_THRESHOLD` (50) at the interface so non-DI
 * callers still work.
 *
 * `getCandidate` returns the lightweight cross-feature `Candidate` shape
 * because the assessment side never needs the full attendance / verification
 * detail — only identity for the scan-result and project-assessment screens.
 */
interface AssessmentCandidateRepository {

    suspend fun getCandidates(
        scheduleId: String,
        query: String = "",
        lowScoreThreshold: Int = ScoreLevel.DEFAULT_LOW_THRESHOLD,
    ): List<AssessmentCandidateRow>

    suspend fun getCandidate(scheduleId: String, candidateId: String): Candidate?

    /**
     * Resolves a QR-scanned or manually-entered exam number to the roster
     * row for [scheduleId]. Returns `null` when the number isn't assigned
     * to this schedule — the UI then routes to a "not on this schedule"
     * error state.
     *
     * Fallback semantics: if the per-schedule package hasn't been
     * downloaded yet, the assessment table has no rows for the schedule.
     * The implementation checks the exam-side dossier roster (same
     * candidates, keyed by paper id = schedule id) so the sections hub
     * still resolves the header before the officer downloads the package.
     */
    suspend fun getCandidateByExamNumber(
        scheduleId: String,
        examNumber: String,
    ): Candidate?

    /** Live count of candidates assigned to [scheduleId]. Denominator for A-S5 progress. */
    fun observeAssignedCount(scheduleId: String): Flow<Int>
}
