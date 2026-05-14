package ng.com.chprbn.mobile.feature.assessment.domain.repository

import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentCandidateRow

/**
 * Read side of the per-schedule candidate roster. The list query supports
 * a free-text filter applied to `fullName` and `examNumber` server-side
 * (i.e. inside the SQL `WHERE`) so the screen doesn't pull the full cohort
 * into the JVM just to filter.
 *
 * `getCandidate` returns the lightweight cross-feature `Candidate` shape
 * because the assessment side never needs the full attendance / verification
 * detail — only identity for the scan-result and project-assessment screens.
 */
interface AssessmentCandidateRepository {

    suspend fun getCandidates(
        scheduleId: String,
        query: String = "",
    ): List<AssessmentCandidateRow>

    suspend fun getCandidate(scheduleId: String, candidateId: String): Candidate?
}
