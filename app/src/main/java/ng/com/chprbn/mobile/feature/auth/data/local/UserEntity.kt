package ng.com.chprbn.mobile.feature.auth.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ng.com.chprbn.mobile.core.persistence.converters.JsonStringListTypeConverter

/**
 * Cached authenticated user (Room).
 */
@Entity(tableName = "auth_user")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val fullName: String?,
    val accessToken: String,
    @TypeConverters(JsonStringListTypeConverter::class)
    val permissions: List<String>,
    val userPhoto: String?,
    val role: String? = null,
    val staffId: String? = null,
    val unit: String? = null,
    val organization: String? = null,
    val lastLoginAt: String? = null
)
