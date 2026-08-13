package ng.com.chprbn.mobile.feature.verification.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Mobile API v1 envelope for POST `adhoc/verified-sync` success (field-officer sync).
 */
data class VerifiedSyncEnvelopeDto(
    @SerializedName(value = "success", alternate = ["status"])
    val success: Boolean,
    val message: String? = null,
    val data: VerifiedSyncResponseDataDto? = null
)

data class VerifiedSyncResponseDataDto(
    val id: Long? = null,
    val license_number: String? = null,
    val verified_at: Long? = null
)
