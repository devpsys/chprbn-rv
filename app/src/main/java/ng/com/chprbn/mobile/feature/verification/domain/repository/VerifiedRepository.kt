package ng.com.chprbn.mobile.feature.verification.domain.repository

import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.verification.domain.model.SaveVerifiedLicenseResult
import ng.com.chprbn.mobile.feature.verification.domain.model.VerifiedLicense

/**
 * Domain contract for persisting and retrieving verified practitioner licenses.
 *
 * Strategy: local persistence via Room today; future remote sync can extend repository implementation.
 */
interface VerifiedRepository {
    suspend fun saveVerifiedLicense(
        licenseRecord: LicenseRecord,
        remark: String,
        verifiedAt: Long,
    ): SaveVerifiedLicenseResult

    suspend fun getVerifiedLicenses(): List<VerifiedLicense>
}

