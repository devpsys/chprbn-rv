package ng.com.chprbn.mobile.feature.accreditation.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "score_drafts")
data class ScoreDraftEntity(
    @PrimaryKey val assessmentId: String,
    val payloadJson: String,
    val completedCount: Int,
    val totalGroups: Int,
    val status: String,
)
