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

// region Adhoc profile — GET /adhoc/profile (Bearer from POST adhoc/login)

data class AdhocProfileEnvelopeDto(
    val status: Boolean,
    val message: String? = null,
    val data: AdhocProfileDataDto? = null
)

/**
 * Mobile API v1 adhoc user profile (`data` from GET /adhoc/profile).
 * Numeric [id] may be serialized as JSON number.
 *
 * [permissions] is intentionally nullable (not `= emptyList()`): Gson's
 * reflective deserialization does NOT honor Kotlin constructor defaults,
 * so an omitted field on the wire would land as a null reference despite
 * a non-null declared type — and any consumer dereferencing it would
 * NPE. Mapper layer `.orEmpty()`s it on the way to the domain `User`.
 * See `VerificationApiServiceTest` for the regression-pin of the same
 * Gson gotcha on the tutor side.
 */
data class AdhocProfileDataDto(
    val id: Double? = null,
    val name: String,
    val email: String,
    val phone: String? = null,
    val username: String,
    val status: Int? = null,
    val role: String? = null,
    val department: String? = null,
    val permissions: List<String>? = null
)

// endregion

// region Profile — GET /user & GET /dashboard/profile (practitioner tutor)

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
