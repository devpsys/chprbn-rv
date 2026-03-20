package ng.com.chprbn.mobile.feature.sync.domain.usecase

import ng.com.chprbn.mobile.feature.sync.domain.model.SyncRecord
import ng.com.chprbn.mobile.feature.sync.domain.repository.SyncRepository
import javax.inject.Inject

class GetSyncRecordsUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(): List<SyncRecord> = syncRepository.getSyncRecords()
}
