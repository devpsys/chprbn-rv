package ng.com.chprbn.mobile.feature.exam.domain.usecase

import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceFilter
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamCandidateRow
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamCandidateRepository
import javax.inject.Inject

/**
 * Returns the per-paper candidate roster, optionally filtered by
 * attendance state and a free-text query. Both filters apply at the SQL
 * layer; the JVM never sees the unfiltered cohort.
 */
class GetExamCandidatesUseCase @Inject constructor(
    private val repository: ExamCandidateRepository,
) {
    suspend operator fun invoke(
        paperId: String,
        filter: AttendanceFilter = AttendanceFilter.All,
        query: String = "",
    ): List<ExamCandidateRow> {
        val trimmedPaper = paperId.trim()
        if (trimmedPaper.isEmpty()) return emptyList()
        return repository.getCandidatesForPaper(trimmedPaper, filter, query.trim())
    }
}
