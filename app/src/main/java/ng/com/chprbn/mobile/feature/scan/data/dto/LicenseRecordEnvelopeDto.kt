package ng.com.chprbn.mobile.feature.scan.data.dto

/**
 * Mobile API v1 envelope for GET practitioners/license.
 */
data class LicenseRecordEnvelopeDto(
    val status: Boolean,
    val message: String? = null,
    val data: LicenseRecordDataDto? = null
)

/**
 * `data` payload for license card (snake_case per API).
 */
data class LicenseRecordDataDto(
    val registration_number: String,
    val full_name: String,
    val photo: String? = null,
    val profession: String,
    val authority: String,
    val license_status: String,
    val expiry_date: String,
    val subtitle: String? = null
)
