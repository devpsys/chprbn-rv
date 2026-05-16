package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentCandidateRow
import ng.com.chprbn.mobile.feature.assessment.domain.model.LowScoreThreshold
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentCandidateRepository
import javax.inject.Inject

/**
 * Fetches the per-schedule candidate roster with optional free-text filter
 * applied at the SQL layer. An empty `query` returns the full cohort —
 * matching the screen's "no filter" state.
 *
 * `lowScoreThreshold` is injected via the [LowScoreThreshold] qualifier
 * so a future per-cohort override only touches the Hilt binding, not
 * this use case or the repository. The audit's "score > 0 isScored bug"
 * gets sidestepped by passing the explicit threshold all the way to the
 * mapper, never relying on a static `DEFAULT_LOW_THRESHOLD` resolved at
 * the data layer.
 */
class GetAssessmentCandidatesUseCase @Inject constructor(
    private val repository: AssessmentCandidateRepository,
    @LowScoreThreshold private val lowScoreThreshold: Int,
) {
    suspend operator fun invoke(
        scheduleId: String,
        query: String = "",
    ): List<AssessmentCandidateRow> {
        val trimmedSchedule = scheduleId.trim()
        if (trimmedSchedule.isEmpty()) return emptyList()
        return repository.getCandidates(trimmedSchedule, query.trim(), lowScoreThreshold)
    }
}
