package ng.com.chprbn.mobile.feature.scan.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached license record (Room). Keyed by registration number.
 */
@Entity(tableName = "license_records")
data class LicenseRecordEntity(
    @PrimaryKey val registrationNumber: String,
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
    val institutionAttendedName: String? = null
)
