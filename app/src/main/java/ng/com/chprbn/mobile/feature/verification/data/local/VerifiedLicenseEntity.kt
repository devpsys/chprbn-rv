package ng.com.chprbn.mobile.feature.verification.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local persistence for verified practitioner records.
 *
 * Table: verified_licenses
 */
@Entity(tableName = "verified_licenses")
data class VerifiedLicenseEntity(
    @PrimaryKey val registrationNumber: String,

    // License fields (copied from license_records)
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
    val institutionAttendedName: String? = null,

    // Verification form fields
    val verificationLocation: String,
    val practitionerPresent: Boolean,
    val remark: String,

    // Metadata
    val verifiedAt: Long,
    /** Stored as [ng.com.chprbn.mobile.feature.verification.domain.model.SyncStatus] name. */
    val syncStatus: String,
    /** Epoch millis of last upload attempt, or null if never attempted. */
    val lastSyncAttempt: Long? = null,
    /** Error message from last failed attempt, or null. */
    val syncError: String? = null
)

