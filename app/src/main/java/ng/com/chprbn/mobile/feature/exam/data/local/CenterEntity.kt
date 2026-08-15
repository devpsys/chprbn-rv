package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "centers")
data class CenterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val location: String,
    val heroImageUrl: String? = null,
    /** Added in schema v2 — see `ExamDatabase`'s `AutoMigration(1, 2)`. */
    @ColumnInfo(defaultValue = "0")
    val hasSections: Boolean = false,
    /** Added in schema v3 — see `ExamDatabase`'s `AutoMigration(2, 3)`. Null for rows persisted before this column existed, until the next dossier download. */
    val year: Int? = null,
)
