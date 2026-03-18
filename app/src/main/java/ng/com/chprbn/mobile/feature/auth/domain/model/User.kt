package ng.com.chprbn.mobile.feature.auth.domain.model

/**
 * Domain model representing the authenticated user session.
 *
 * Note: access token storage is handled in the Data layer (Room cache).
 */
data class User(
    val id: String,
    val email: String,
    val fullName: String?,
    val accessToken: String,
    val permissions: List<String>,
    val userPhoto: String?
)

