package ng.com.chprbn.mobile.feature.auth.domain.model

/**
 * Domain model representing the authenticated practitioner (tutor) session.
 *
 * [username] is the license number used to sign in (mobile API `username` field).
 */
data class User(
    val id: String,
    val username: String,
    val email: String,
    val fullName: String?,
    val accessToken: String,
    val permissions: List<String>,
    val userPhoto: String?,
    val role: String? = null,
    val staffId: String? = null,
    val unit: String? = null,
    val organization: String? = null,
    val lastLoginAt: String? = null
)
