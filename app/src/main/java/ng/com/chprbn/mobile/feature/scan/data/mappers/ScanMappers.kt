package ng.com.chprbn.mobile.feature.scan.data.mappers

import ng.com.chprbn.mobile.core.network.normalizeApiPhotoToDataUri
import ng.com.chprbn.mobile.feature.scan.data.dto.LicenseRecordDataDto
import ng.com.chprbn.mobile.feature.scan.data.local.LicenseRecordEntity
import ng.com.chprbn.mobile.feature.scan.domain.model.InstitutionAttended
import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord

fun LicenseRecordDataDto.toDomain(): LicenseRecord = LicenseRecord(
    registrationNumber = registration_number,
    fullName = full_name,
    photoUrl = (photo ?: photoUrl).normalizeApiPhotoToDataUri(),
    profession = profession,
    certificateNo = certificate_no.orEmpty(),
    email = email.orEmpty(),
    phone = phone.orEmpty(),
    licenseStatus = license_status,
    expiryDate = expiry_date,
    subtitle = subtitle,
    issueDate = issue_date.orEmpty(),
    gender = gender.orEmpty(),
    graduationDate = graduation_date.orEmpty(),
    institutionAttended = institution_attended?.name?.takeIf { it.isNotBlank() }
        ?.let { InstitutionAttended(name = it) }
)

fun LicenseRecord.toEntity(): LicenseRecordEntity = LicenseRecordEntity(
    registrationNumber = registrationNumber,
    fullName = fullName,
    photoUrl = photoUrl,
    profession = profession,
    certificateNo = certificateNo,
    email = email,
    phone = phone,
    licenseStatus = licenseStatus,
    expiryDate = expiryDate,
    subtitle = subtitle,
    issueDate = issueDate,
    gender = gender,
    graduationDate = graduationDate,
    institutionAttendedName = institutionAttended?.name
)

fun LicenseRecordEntity.toDomain(): LicenseRecord = LicenseRecord(
    registrationNumber = registrationNumber,
    fullName = fullName,
    photoUrl = photoUrl,
    profession = profession,
    certificateNo = certificateNo,
    email = email,
    phone = phone,
    licenseStatus = licenseStatus,
    expiryDate = expiryDate,
    subtitle = subtitle,
    issueDate = issueDate,
    gender = gender,
    graduationDate = graduationDate,
    institutionAttended = institutionAttendedName?.takeIf { it.isNotBlank() }
        ?.let { InstitutionAttended(name = it) }
)
