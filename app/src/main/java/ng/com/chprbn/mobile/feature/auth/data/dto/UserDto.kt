package ng.com.chprbn.mobile.feature.auth.data.dto

data class UserDto(
    val id: String,
    val email: String,
    val fullName: String?,
    val permissions: List<String> = emptyList(),
    val userPhoto: String? = null
)

