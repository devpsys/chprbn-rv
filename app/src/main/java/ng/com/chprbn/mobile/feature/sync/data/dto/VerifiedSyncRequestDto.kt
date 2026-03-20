package ng.com.chprbn.mobile.feature.sync.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Payload for uploading a local verification to the central registry.
 */
data class VerifiedSyncRequestDto(
    @SerializedName("license_number")
    val licenseNumber: String,
    @SerializedName("verification_location")
    val verificationLocation: String,
    @SerializedName("practitioner_present")
    val practitionerPresent: Boolean,
    @SerializedName("remark")
    val remark: String,
    /** Epoch millis (UTC). */
    @SerializedName("verified_at")
    val verifiedAt: Long
)
