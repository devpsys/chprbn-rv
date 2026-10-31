package ng.com.chprbn.mobile.feature.accreditation.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assessment_drafts",
    indices = [
        Index(value = ["institutionId"], unique = true),
        Index("status"),
    ],
)
data class AssessmentDraftEntity(
    @PrimaryKey val id: String,
    val institutionId: String,
    val institutionName: String,
    val cycleLabel: String,
    val facilityCode: String,
    val locationLabel: String,
    val status: String,
    val updatedAt: Long,
)
