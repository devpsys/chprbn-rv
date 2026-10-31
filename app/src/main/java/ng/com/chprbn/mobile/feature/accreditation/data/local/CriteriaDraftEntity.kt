package ng.com.chprbn.mobile.feature.accreditation.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "criteria_drafts")
data class CriteriaDraftEntity(
    @PrimaryKey val assessmentId: String,
    val payloadJson: String,
    val evaluatedCount: Int,
    val totalCriteria: Int,
    val status: String,
)
