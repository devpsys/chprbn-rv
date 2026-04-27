package ng.com.chprbn.mobile.feature.verification.domain.model

/**
 * Domain model for a practitioner license record (from scan or manual lookup).
 * Backs RecordDetailScreen; single source of truth is local cache after remote fetch.
 */
data class LicenseRecord(
    val registrationNumber: String,
    val fullName: String,
    val photoUrl: String?,
    val profession: String,
    val certificateNo: String = "",
    val email: String = "",
    val phone: String = "",
    val licenseStatus: String,
    val expiryDate: String,
    val subtitle: String?,
    val issueDate: String = "",
    val gender: String = "",
    val graduationDate: String = "",
    val institutionAttended: InstitutionAttended? = null
)
