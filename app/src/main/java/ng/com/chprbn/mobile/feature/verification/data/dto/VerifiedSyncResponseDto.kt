package ng.com.chprbn.mobile.feature.verification.data.dto

/**
 * Mobile API v1 envelope for POST `adhoc/verified-sync` success (field-officer sync).
 */
data class VerifiedSyncEnvelopeDto(
    val status: Boolean,
    val message: String? = null,
    val data: VerifiedSyncResponseDataDto? = null
)

data class VerifiedSyncResponseDataDto(
    val id: Long? = null,
    val license_number: String? = null,
    val verified_at: Long? = null
)
