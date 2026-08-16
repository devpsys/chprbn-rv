package ng.com.chprbn.mobile.feature.exam.domain.repository

import kotlinx.coroutines.flow.Flow
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceFilter
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamCandidateRow

/**
 * Read side of the per-paper candidate roster. The list query supports
 * a free-text filter (applied at the SQL layer over `fullName` and
 * `examNumber`) plus an [AttendanceFilter] state filter.
 *
 * `getCandidateByExamNumber` is the lookup the scan-result screen uses
 * to resolve a scanned QR payload to a candidate.
 */
interface ExamCandidateRepository {

    suspend fun getCandidatesForPaper(
        paperId: String,
        filter: AttendanceFilter = AttendanceFilter.All,
        query: String = "",
    ): List<ExamCandidateRow>

    suspend fun getCandidateByExamNumber(examNumber: String): Candidate?

    /** Lookup by the stable DB id (as opposed to the exam-number lookup above). */
    suspend fun getCandidateById(candidateId: String): Candidate?

    /**
     * Plain (attendance-free) candidate list assigned to [paperId] — used
     * by the assessment feature as the shell roster for a PE/PA paper
     * before the schedule's package has been downloaded. See
     * `AssessmentCandidateRepositoryImpl.getCandidates`.
     */
    suspend fun getAssignedCandidates(paperId: String): List<Candidate>

    /**
     * Scoped exam-number lookup — the assessment feature's QR scan flow
     * falls back to this when the practical package hasn't been downloaded.
     * Returns `null` if the candidate isn't on this paper's dossier roster,
     * so a scan of the wrong candidate stays rejected instead of resolving
     * against every candidate the app has ever cached.
     */
    suspend fun getCandidateForPaperByExamNumber(
        paperId: String,
        examNumber: String,
    ): Candidate?

    /**
     * Live count of candidates assigned to [paperId]. Same use case as
     * [getAssignedCandidates] but as a Flow — feeds the paper-detail
     * progress pill's denominator on the assessment side when the
     * package isn't downloaded yet.
     */
    fun observeAssignedCandidateCount(paperId: String): Flow<Int>
}
