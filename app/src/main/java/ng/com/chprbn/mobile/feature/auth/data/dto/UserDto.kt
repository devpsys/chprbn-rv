package ng.com.chprbn.mobile.feature.auth.data.dto

data class UserDto(
    val id: String,
    val email: String,
    val fullName: String?,
    val permissions: List<String> = emptyList(),
    val userPhoto: String? = null,
    val role: String? = null,
    val staffId: String? = null,
    val unit: String? = null,
    val organization: String? = null,
    val lastLoginAt: String? = null
)

