package ng.com.chprbn.mobile.feature.scan.data.mappers

import ng.com.chprbn.mobile.core.network.normalizeApiPhotoToDataUri
import ng.com.chprbn.mobile.feature.scan.data.dto.LicenseRecordDataDto
import ng.com.chprbn.mobile.feature.scan.data.local.LicenseRecordEntity
import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord

fun LicenseRecordDataDto.toDomain(): LicenseRecord = LicenseRecord(
    registrationNumber = registration_number,
    fullName = full_name,
    photoUrl = photo.normalizeApiPhotoToDataUri(),
    profession = profession,
    authority = authority,
    licenseStatus = license_status,
    expiryDate = expiry_date,
    subtitle = subtitle
)

fun LicenseRecord.toEntity(): LicenseRecordEntity = LicenseRecordEntity(
    registrationNumber = registrationNumber,
    fullName = fullName,
    photoUrl = photoUrl,
    profession = profession,
    authority = authority,
    licenseStatus = licenseStatus,
    expiryDate = expiryDate,
    subtitle = subtitle
)

fun LicenseRecordEntity.toDomain(): LicenseRecord = LicenseRecord(
    registrationNumber = registrationNumber,
    fullName = fullName,
    photoUrl = photoUrl,
    profession = profession,
    authority = authority,
    licenseStatus = licenseStatus,
    expiryDate = expiryDate,
    subtitle = subtitle
)
