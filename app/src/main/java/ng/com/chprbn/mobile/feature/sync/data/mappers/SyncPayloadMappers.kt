package ng.com.chprbn.mobile.feature.sync.data.mappers

import ng.com.chprbn.mobile.feature.sync.data.dto.VerifiedSyncRequestDto
import ng.com.chprbn.mobile.feature.verified.domain.model.VerifiedLicense

fun VerifiedLicense.toVerifiedSyncRequestDto(): VerifiedSyncRequestDto =
    VerifiedSyncRequestDto(
        licenseNumber = registrationNumber,
        verificationLocation = verificationLocation,
        practitionerPresent = practitionerPresent,
        remark = remark,
        verifiedAt = verifiedAt
    )
