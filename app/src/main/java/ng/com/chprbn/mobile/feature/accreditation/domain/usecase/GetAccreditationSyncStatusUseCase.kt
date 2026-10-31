package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.SyncStatusSnapshot
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AccreditationSyncRepository
import javax.inject.Inject

class GetAccreditationSyncStatusUseCase @Inject constructor(
    private val repository: AccreditationSyncRepository,
) {
    suspend operator fun invoke(): SyncStatusSnapshot = repository.getSyncStatus()
}
