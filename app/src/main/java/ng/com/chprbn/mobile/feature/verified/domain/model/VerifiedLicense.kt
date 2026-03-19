package ng.com.chprbn.mobile.feature.verified.domain.model

/**
 * Domain model for a verified practitioner license record.
 *
 * Contains:
 * - License data (copied from `license_records`)
 * - Verification form data
 * - Sync metadata for future backend synchronization
 */
data class VerifiedLicense(
    val registrationNumber: String,
    val fullName: String,
    val photoUrl: String?,
    val profession: String,
    val authority: String,
    val licenseStatus: String,
    val expiryDate: String,
    val subtitle: String?,
    val verifiedAt: Long,
    val verificationLocation: String,
    val practitionerPresent: Boolean,
    val remark: String,
    val syncStatus: SyncStatus
)

