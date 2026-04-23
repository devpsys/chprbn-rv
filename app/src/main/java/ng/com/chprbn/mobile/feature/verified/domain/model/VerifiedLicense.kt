package ng.com.chprbn.mobile.feature.verified.domain.model

import ng.com.chprbn.mobile.feature.scan.domain.model.InstitutionAttended

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
    val issueDate: String = "",
    val gender: String = "",
    val graduationDate: String = "",
    val institutionAttended: InstitutionAttended? = null,
    val verifiedAt: Long,
    val verificationLocation: String,
    val practitionerPresent: Boolean,
    val remark: String,
    val syncStatus: SyncStatus,
    /** Epoch millis of last sync attempt (success or failure), if any. */
    val lastSyncAttempt: Long? = null,
    /** Server or network error from last failed sync, if any. */
    val syncError: String? = null
)

