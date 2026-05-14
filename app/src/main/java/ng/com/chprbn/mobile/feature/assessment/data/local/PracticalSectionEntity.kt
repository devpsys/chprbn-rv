package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per practical section (e.g. "A — Patient Assessment"). Indexed
 * on `scheduleId` so the per-schedule hub query is a non-table-scan.
 */
@Entity(
    tableName = "practical_sections",
    indices = [Index("scheduleId")],
)
data class PracticalSectionEntity(
    @PrimaryKey val id: String,
    val scheduleId: String,
    val title: String,
    val subtitle: String,
    val ordering: Int,
)
