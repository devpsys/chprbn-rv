package ng.com.chprbn.mobile.feature.verified.domain.usecase

import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.verified.domain.model.SaveVerifiedLicenseResult
import ng.com.chprbn.mobile.feature.verified.domain.repository.VerifiedRepository
import javax.inject.Inject

/**
 * Use case: save a verified practitioner license locally.
 */
class SaveVerifiedLicenseUseCase @Inject constructor(
    private val repository: VerifiedRepository
) {
    suspend operator fun invoke(
        licenseRecord: LicenseRecord,
        verificationLocation: String,
        practitionerPresent: Boolean,
        remark: String,
        verifiedAt: Long = System.currentTimeMillis()
    ): SaveVerifiedLicenseResult {
        val locationTrimmed = verificationLocation.trim()
        val remarkTrimmed = remark.trim()

        if (locationTrimmed.isEmpty()) {
            return SaveVerifiedLicenseResult.Error("Verification location is required.")
        }
        if (remarkTrimmed.isEmpty()) {
            return SaveVerifiedLicenseResult.Error("Officer remarks are required.")
        }
        if (!practitionerPresent) {
            return SaveVerifiedLicenseResult.Error("Practitioner must be marked as verified.")
        }
//        if (!licenseRecord.licenseStatus.equals("Active", ignoreCase = true)) {
//            return SaveVerifiedLicenseResult.Error("Only practitioners with an active license can be verified.")
//        }

        return repository.saveVerifiedLicense(
            licenseRecord = licenseRecord,
            verificationLocation = locationTrimmed,
            practitionerPresent = practitionerPresent,
            remark = remarkTrimmed,
            verifiedAt = verifiedAt
        )
    }
}

