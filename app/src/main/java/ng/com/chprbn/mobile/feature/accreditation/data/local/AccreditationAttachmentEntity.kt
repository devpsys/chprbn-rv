package ng.com.chprbn.mobile.feature.accreditation.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accreditation_attachments",
    indices = [
        Index("assessmentId"),
        Index(value = ["assessmentId", "parentKey"]),
    ],
)
data class AccreditationAttachmentEntity(
    @PrimaryKey val id: String,
    val assessmentId: String,
    val parentKey: String,
    val title: String,
    val sizeLabel: String,
    val isPhoto: Boolean,
    val localPath: String,
    val contentUri: String?,
    val locationLabel: String?,
    val uploadState: String,
)
