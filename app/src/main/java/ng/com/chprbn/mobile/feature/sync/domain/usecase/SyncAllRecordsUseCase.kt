package ng.com.chprbn.mobile.feature.sync.domain.usecase

import ng.com.chprbn.mobile.feature.sync.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.feature.sync.domain.repository.SyncRepository
import javax.inject.Inject

class SyncAllRecordsUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(): SyncBatchResult = syncRepository.syncAllPendingAndFailed()
}
