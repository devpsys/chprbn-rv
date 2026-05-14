package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentSyncRepository
import javax.inject.Inject

/**
 * Triggers a manual sync pass for all pending / failed assessment-side
 * rows. Background sync runs through WorkManager on every local write;
 * this use case is the user-initiated "Sync Now" gesture and surfaces a
 * counter back to the UI.
 */
class SyncAssessmentScoresUseCase @Inject constructor(
    private val repository: AssessmentSyncRepository,
) {
    suspend operator fun invoke(): SyncBatchResult = repository.syncPending()
}
