package ng.com.chprbn.mobile.feature.exam.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.feature.exam.data.local.CandidateDao
import ng.com.chprbn.mobile.feature.exam.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceFilter
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamCandidateRow
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamCandidateRepository
import javax.inject.Inject

class ExamCandidateRepositoryImpl @Inject constructor(
    private val candidateDao: CandidateDao,
) : ExamCandidateRepository {

    override suspend fun getCandidatesForPaper(
        paperId: String,
        filter: AttendanceFilter,
        query: String,
    ): List<ExamCandidateRow> = withContext(Dispatchers.IO) {
        val likeArg = if (query.isBlank()) "" else "%${escapeLike(query.trim())}%"
        candidateDao.rowsForPaper(
            paperId = paperId,
            attendanceFilter = filter.name,
            query = likeArg,
        ).map { it.toDomain(paperId) }
    }

    override suspend fun getCandidateByExamNumber(examNumber: String): Candidate? =
        withContext(Dispatchers.IO) {
            candidateDao.getByExamNumber(examNumber)?.toDomain()
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
