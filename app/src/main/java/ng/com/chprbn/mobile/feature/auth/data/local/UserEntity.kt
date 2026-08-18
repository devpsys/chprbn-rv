package ng.com.chprbn.mobile.feature.auth.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ng.com.chprbn.mobile.core.persistence.converters.JsonStringListTypeConverter

/**
 * Cached authenticated user (Room).
 *
 * [passwordSalt] / [passwordVerifier] / [passwordAlgorithm] are the
 * PBKDF2 credential written on a successful online sign-in and checked on
 * offline login — see `PasswordVerifier` and `AuthRepositoryImpl.login()`.
 * Nullable so rows carried over from schema v7 (which had no verifier
 * columns) still round-trip; those users are refused offline login until
 * they've signed in online at least once against v8.
 */
@Entity(tableName = "auth_user")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val fullName: String?,
    @TypeConverters(JsonStringListTypeConverter::class)
    val permissions: List<String>,
    val userPhoto: String?,
    val role: String? = null,
    val staffId: String? = null,
    val unit: String? = null,
    val organization: String? = null,
    val lastLoginAt: String? = null,
    val location: String? = null,
    val passwordSalt: String? = null,
    val passwordVerifier: String? = null,
    val passwordAlgorithm: String? = null,
    /**
     * Added in schema v10 so the sync layer can build the `assessor`
     * block that `attendance/push-record`, `project/push-record`, and
     * `practical/push-record` require. Rows carried over from v9 have
     * these as null and must sign in online at least once to backfill.
     */
    val phone: String? = null,
    val status: Int? = null,
    val assessorId: Long? = null,
)
