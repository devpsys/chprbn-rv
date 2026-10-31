package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.SyncDraftsResult
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AccreditationSyncRepository
import javax.inject.Inject

class SyncAllAccreditationDraftsUseCase @Inject constructor(
    private val repository: AccreditationSyncRepository,
) {
    suspend operator fun invoke(): SyncDraftsResult = repository.syncAll()
}
