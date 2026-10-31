package ng.com.chprbn.mobile.feature.accreditation.domain.usecase

import android.net.Uri
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AccreditationAttachmentRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AccreditationAttachmentRepository
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentDraftRepository
import javax.inject.Inject

class AddAccreditationAttachmentUseCase @Inject constructor(
    private val draftRepository: AssessmentDraftRepository,
    private val attachmentRepository: AccreditationAttachmentRepository,
) {
    suspend operator fun invoke(
        institutionId: String,
        parentKey: String,
        fromUri: Uri,
        isPhoto: Boolean,
        title: String? = null,
        locationLabel: String? = null,
    ): AccreditationAttachmentRecord? {
        val draft = draftRepository.getOrCreateCurrent(institutionId)
        return attachmentRepository.add(
            assessmentId = draft.id,
            parentKey = parentKey,
            fromUri = fromUri,
            isPhoto = isPhoto,
            title = title,
            locationLabel = locationLabel,
        )
    }
}
