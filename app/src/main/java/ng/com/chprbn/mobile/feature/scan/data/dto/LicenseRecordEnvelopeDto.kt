package ng.com.chprbn.mobile.feature.scan.data.dto

import com.google.gson.annotations.SerializedName

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
    /** Base64 passport bytes (no `data:` prefix) per mobile API docs. */
    val photo: String? = null,
    /** Some deployments / OpenAPI use `photo_url` (absolute or relative URL) instead. */
    @SerializedName("photo_url")
    val photoUrl: String? = null,
    val profession: String,
    val authority: String,
    val license_status: String,
    val expiry_date: String,
    val subtitle: String? = null,
    val issue_date: String? = null,
    val gender: String? = null,
    val graduation_date: String? = null,
    @SerializedName("institution_attended")
    val institution_attended: InstitutionAttendedDto? = null
)
