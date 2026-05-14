package ng.com.chprbn.mobile.feature.assessment.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentCandidateDao
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentCandidateRow
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentCandidateRepository
import javax.inject.Inject

class AssessmentCandidateRepositoryImpl @Inject constructor(
    private val candidateDao: AssessmentCandidateDao,
) : AssessmentCandidateRepository {

    override suspend fun getCandidates(
        scheduleId: String,
        query: String,
    ): List<AssessmentCandidateRow> = withContext(Dispatchers.IO) {
        val likeArg = if (query.isBlank()) "" else "%${escapeLike(query.trim())}%"
        candidateDao.rowsForSchedule(scheduleId, likeArg).map { it.toDomain() }
    }

    override suspend fun getCandidate(
        scheduleId: String,
        candidateId: String,
    ): Candidate? = withContext(Dispatchers.IO) {
        candidateDao.getForSchedule(scheduleId, candidateId)?.toDomain()
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
