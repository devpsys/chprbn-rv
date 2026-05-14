package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "centers")
data class CenterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val location: String,
    val heroImageUrl: String? = null,
)
