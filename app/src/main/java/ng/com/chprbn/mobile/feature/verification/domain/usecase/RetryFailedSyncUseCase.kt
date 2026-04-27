package ng.com.chprbn.mobile.feature.verification.domain.usecase

import ng.com.chprbn.mobile.feature.verification.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.feature.verification.domain.repository.SyncRepository
import javax.inject.Inject

class RetryFailedSyncUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(): SyncBatchResult = syncRepository.retryFailed()
}
