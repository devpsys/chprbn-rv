package ng.com.chprbn.mobile.feature.auth.data.dto

/**
 * Mobile API v1 login body: practitioner license number + password.
 */
data class LoginRequestDto(
    val username: String,
    val password: String
)
