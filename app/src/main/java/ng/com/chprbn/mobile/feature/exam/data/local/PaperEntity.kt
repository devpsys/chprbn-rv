package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "papers",
    indices = [Index("centerId")],
)
data class PaperEntity(
    @PrimaryKey val id: String,
    val centerId: String,
    val title: String,
    val subtitle: String,
    val paperKind: String,
    val startAt: Long,
    val endAt: Long,
    val hall: String,
    val totalCandidates: Int,
)
