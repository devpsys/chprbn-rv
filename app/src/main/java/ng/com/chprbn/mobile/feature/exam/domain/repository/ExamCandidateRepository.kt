package ng.com.chprbn.mobile.feature.exam.domain.repository

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
}
