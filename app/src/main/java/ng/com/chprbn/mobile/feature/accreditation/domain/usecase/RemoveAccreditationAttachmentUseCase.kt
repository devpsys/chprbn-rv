package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftOpResult
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AccreditationAttachmentRepository
import javax.inject.Inject

class RemoveAccreditationAttachmentUseCase @Inject constructor(
    private val repository: AccreditationAttachmentRepository,
) {
    suspend operator fun invoke(attachmentId: String): DraftOpResult =
        repository.remove(attachmentId)
}
