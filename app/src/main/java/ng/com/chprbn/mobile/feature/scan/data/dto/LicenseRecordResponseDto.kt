package ng.com.chprbn.mobile.feature.scan.data.dto

/**
 * API response for license lookup. Align field names with backend.
 */
data class LicenseRecordResponseDto(
    val registration_number: String,
    val full_name: String,
    val photo_url: String? = null,
    val profession: String,
    val authority: String,
    val license_status: String,
    val expiry_date: String,
    val subtitle: String? = null
)
