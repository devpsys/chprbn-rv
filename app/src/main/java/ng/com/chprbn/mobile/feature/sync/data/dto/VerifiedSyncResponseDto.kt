package ng.com.chprbn.mobile.feature.sync.data.dto

/**
 * Mobile API v1 envelope for POST practitioners/verified-sync success.
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
