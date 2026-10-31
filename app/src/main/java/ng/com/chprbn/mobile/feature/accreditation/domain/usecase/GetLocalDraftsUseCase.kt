package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.LocalDraftListItem
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AccreditationSyncRepository
import javax.inject.Inject

class GetLocalDraftsUseCase @Inject constructor(
    private val repository: AccreditationSyncRepository,
) {
    suspend operator fun invoke(): List<LocalDraftListItem> = repository.listLocalDrafts()
}
