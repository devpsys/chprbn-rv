package ng.com.chprbn.mobile.feature.verification.domain.usecase

import ng.com.chprbn.mobile.feature.verification.domain.model.SyncRecord
import ng.com.chprbn.mobile.feature.verification.domain.repository.SyncRepository
import javax.inject.Inject

class GetSyncRecordsUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(): List<SyncRecord> = syncRepository.getSyncRecords()
}
