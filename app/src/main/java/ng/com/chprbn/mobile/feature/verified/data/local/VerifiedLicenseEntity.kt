package ng.com.chprbn.mobile.feature.verified.data.local

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
    val authority: String,
    val licenseStatus: String,
    val expiryDate: String,
    val subtitle: String?,

    // Verification form fields
    val verificationLocation: String,
    val practitionerPresent: Boolean,
    val remark: String,

    // Metadata
    val verifiedAt: Long,
    val syncStatus: String // e.g. "Pending" / "Synced"
)

