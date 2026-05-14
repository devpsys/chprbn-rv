package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentScheduleRepository
import javax.inject.Inject

/**
 * Wipes every locally-cached row for one schedule, or every schedule when
 * `scheduleId` is null. Pending score writes for the cleared schedule are
 * lost — the caller is responsible for surfacing the destructive-warning
 * dialog before invoking this.
 */
class ClearAssessmentCacheUseCase @Inject constructor(
    private val repository: AssessmentScheduleRepository,
) {
    suspend operator fun invoke(scheduleId: String? = null): SaveResult {
        val trimmed = scheduleId?.trim()?.takeIf { it.isNotEmpty() }
        return repository.clearCache(trimmed)
    }
}
