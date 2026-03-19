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
    val userPhoto: String?,
    /** Role/title for Dashboard (e.g. "Senior Field Officer"). */
    val role: String? = null,
    /** Staff ID for display (e.g. "44920"). */
    val staffId: String? = null,
    /** Unit for display (e.g. "Unit 7B"). */
    val unit: String? = null,
    /** Organization for profile (e.g. "Health Council"). */
    val organization: String? = null,
    /** Last login display (e.g. "Today, 10:30 AM"); set on login success. */
    val lastLoginAt: String? = null
)

