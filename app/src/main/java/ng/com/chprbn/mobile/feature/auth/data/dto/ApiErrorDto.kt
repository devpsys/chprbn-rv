package ng.com.chprbn.mobile.feature.auth.data.dto

/**
 * Generic API error payload.
 * The backend may differ; this is used for best-effort parsing.
 */
data class ApiErrorDto(
    val message: String? = null
)

