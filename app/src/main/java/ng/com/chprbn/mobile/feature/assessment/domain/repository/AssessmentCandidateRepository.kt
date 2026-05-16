package ng.com.chprbn.mobile.feature.assessment.domain.repository

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
}
