package ng.com.chprbn.mobile.feature.verification.data.mappers

import ng.com.chprbn.mobile.feature.verification.data.dto.VerifiedSyncRequestDto
import ng.com.chprbn.mobile.feature.verification.domain.model.VerifiedLicense

fun VerifiedLicense.toVerifiedSyncRequestDto(): VerifiedSyncRequestDto =
    VerifiedSyncRequestDto(
        licenseNumber = registrationNumber,
        verificationLocation = verificationLocation,
        practitionerPresent = practitionerPresent,
        remark = remark,
        verifiedAt = verifiedAt
    )
