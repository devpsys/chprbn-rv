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
        remark: String,
        verifiedAt: Long = System.currentTimeMillis()
    ): SaveVerifiedLicenseResult {
        val remarkTrimmed = remark.trim()

        if (remarkTrimmed.isEmpty()) {
            return SaveVerifiedLicenseResult.Error("Please select an officer remark.")
        }
//        if (!licenseRecord.licenseStatus.equals("Active", ignoreCase = true)) {
//            return SaveVerifiedLicenseResult.Error("Only practitioners with an active license can be verified.")
//        }

        return repository.saveVerifiedLicense(
            licenseRecord = licenseRecord,
            remark = remarkTrimmed,
            verifiedAt = verifiedAt
        )
    }
}

