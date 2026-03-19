package ng.com.chprbn.mobile.feature.auth.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * Cached authenticated user (Room).
 *
 * Security note:
 * - access token is stored as plain text for now to keep the example simple.
 * - isolate this in the local data layer so we can swap to encrypted storage later.
 */
@Entity(tableName = "auth_user")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String?,
    val accessToken: String,
    @TypeConverters(ng.com.chprbn.mobile.core.persistence.converters.JsonStringListTypeConverter::class)
    val permissions: List<String>,
    val userPhoto: String?,
    val role: String? = null,
    val staffId: String? = null,
    val unit: String? = null,
    val organization: String? = null,
    val lastLoginAt: String? = null
)

