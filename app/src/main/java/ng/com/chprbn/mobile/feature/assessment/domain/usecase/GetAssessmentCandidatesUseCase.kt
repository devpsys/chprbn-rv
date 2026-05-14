package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentCandidateRow
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentCandidateRepository
import javax.inject.Inject

/**
 * Fetches the per-schedule candidate roster with optional free-text filter
 * applied at the SQL layer. An empty `query` returns the full cohort —
 * matching the screen's "no filter" state.
 */
class GetAssessmentCandidatesUseCase @Inject constructor(
    private val repository: AssessmentCandidateRepository,
) {
    suspend operator fun invoke(
        scheduleId: String,
        query: String = "",
    ): List<AssessmentCandidateRow> {
        val trimmedSchedule = scheduleId.trim()
        if (trimmedSchedule.isEmpty()) return emptyList()
        return repository.getCandidates(trimmedSchedule, query.trim())
    }
}
