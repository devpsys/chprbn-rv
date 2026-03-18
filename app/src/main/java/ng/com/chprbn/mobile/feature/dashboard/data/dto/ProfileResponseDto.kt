package ng.com.chprbn.mobile.feature.dashboard.data.dto

/**
 * API response for dashboard profile. Align with backend; map to domain [User] in repository.
 */
data class ProfileResponseDto(
    val id: String,
    val email: String,
    val fullName: String?,
    val permissions: List<String> = emptyList(),
    val userPhoto: String? = null,
    val role: String? = null,
    val staffId: String? = null,
    val unit: String? = null
)
