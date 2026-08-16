package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSyncStats
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentSyncRepository
import javax.inject.Inject

class GetAssessmentSyncStatsUseCase @Inject constructor(
    private val repository: AssessmentSyncRepository,
) {
    suspend operator fun invoke(scheduleId: String): AssessmentSyncStats {
        val trimmed = scheduleId.trim()
        if (trimmed.isEmpty()) return AssessmentSyncStats(pendingSyncCount = 0, lastSyncAt = null)
        return repository.getSyncStats(trimmed)
    }
}
