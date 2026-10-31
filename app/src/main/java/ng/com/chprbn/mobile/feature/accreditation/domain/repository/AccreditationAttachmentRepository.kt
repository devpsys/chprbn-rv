package ng.com.chprbn.mobile.feature.accreditation.domain.repository

import android.net.Uri
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AccreditationAttachmentRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftOpResult

interface AccreditationAttachmentRepository {
    suspend fun add(
        assessmentId: String,
        parentKey: String,
        fromUri: Uri,
        isPhoto: Boolean,
        title: String? = null,
        locationLabel: String? = null,
    ): AccreditationAttachmentRecord?

    suspend fun remove(attachmentId: String): DraftOpResult

    suspend fun listForParent(
        assessmentId: String,
        parentKey: String,
    ): List<AccreditationAttachmentRecord>
}
