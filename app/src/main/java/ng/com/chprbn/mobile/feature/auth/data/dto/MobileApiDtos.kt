package ng.com.chprbn.mobile.feature.auth.data.dto

// region Login — POST /login (same contract as POST /auth/login)

data class LoginEnvelopeDto(
    val status: Boolean,
    val message: String? = null,
    val data: LoginDataDto? = null
)

data class LoginDataDto(
    val token: String
)

// endregion

// region Profile — GET /user & GET /dashboard/profile

data class ProfileEnvelopeDto(
    val status: Boolean,
    val message: String? = null,
    val data: ProfileDataDto? = null
)

/**
 * Matches mobile API v1 `data` object for practitioner profile.
 * [photo] is raw Base64 image bytes (no data: prefix) when present.
 */
data class ProfileDataDto(
    val photo: String? = null,
    val name: String,
    val gender: String? = null,
    val username: String,
    val email: String,
    val phone: String? = null,
    val permissions: List<String> = emptyList(),
    val id: String,
    val role: String? = null,
    val unit: String? = null,
    val lastLoginAt: String? = null
)

// endregion
