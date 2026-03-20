package ng.com.chprbn.mobile.feature.verified.data.mappers

import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.verified.data.local.VerifiedLicenseEntity
import ng.com.chprbn.mobile.feature.verified.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.verified.domain.model.VerifiedLicense

internal fun SyncStatus.toDbValue(): String = when (this) {
    SyncStatus.Pending -> "Pending"
    SyncStatus.Synced -> "Synced"
    SyncStatus.Failed -> "Failed"
}

internal fun String.toSyncStatus(): SyncStatus = when (this) {
    "Pending" -> SyncStatus.Pending
    "Synced" -> SyncStatus.Synced
    "Failed" -> SyncStatus.Failed
    else -> SyncStatus.Pending
}

fun VerifiedLicenseEntity.toDomain(): VerifiedLicense = VerifiedLicense(
    registrationNumber = registrationNumber,
    fullName = fullName,
    photoUrl = photoUrl,
    profession = profession,
    authority = authority,
    licenseStatus = licenseStatus,
    expiryDate = expiryDate,
    subtitle = subtitle,
    verifiedAt = verifiedAt,
    verificationLocation = verificationLocation,
    practitionerPresent = practitionerPresent,
    remark = remark,
    syncStatus = syncStatus.toSyncStatus(),
    lastSyncAttempt = lastSyncAttempt,
    syncError = syncError
)

fun VerifiedLicense.toEntity(): VerifiedLicenseEntity = VerifiedLicenseEntity(
    registrationNumber = registrationNumber,
    fullName = fullName,
    photoUrl = photoUrl,
    profession = profession,
    authority = authority,
    licenseStatus = licenseStatus,
    expiryDate = expiryDate,
    subtitle = subtitle,
    verificationLocation = verificationLocation,
    practitionerPresent = practitionerPresent,
    remark = remark,
    verifiedAt = verifiedAt,
    syncStatus = syncStatus.toDbValue(),
    lastSyncAttempt = lastSyncAttempt,
    syncError = syncError
)

/**
 * Creates a [VerifiedLicenseEntity] by copying license fields from the verified payload.
 * The mapping is kept in the data layer to ensure presentation/domain don't know about Room schema.
 */
fun LicenseRecord.toVerifiedLicenseEntity(
    verificationLocation: String,
    practitionerPresent: Boolean,
    remark: String,
    verifiedAt: Long,
    syncStatus: SyncStatus
): VerifiedLicenseEntity = VerifiedLicenseEntity(
    registrationNumber = registrationNumber,
    fullName = fullName,
    photoUrl = photoUrl,
    profession = profession,
    authority = authority,
    licenseStatus = licenseStatus,
    expiryDate = expiryDate,
    subtitle = subtitle,
    verificationLocation = verificationLocation,
    practitionerPresent = practitionerPresent,
    remark = remark,
    verifiedAt = verifiedAt,
    syncStatus = syncStatus.toDbValue(),
    lastSyncAttempt = null,
    syncError = null
)

