package ng.com.chprbn.mobile.feature.verification.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.verification.data.local.VerifiedLicenseDao
import ng.com.chprbn.mobile.feature.verification.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.verification.data.mappers.toVerifiedLicenseEntity
import ng.com.chprbn.mobile.feature.verification.domain.model.SaveVerifiedLicenseResult
import ng.com.chprbn.mobile.feature.verification.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.verification.domain.model.VerifiedLicense
import ng.com.chprbn.mobile.feature.verification.domain.repository.VerifiedRepository
import javax.inject.Inject

class VerifiedRepositoryImpl @Inject constructor(
    private val verifiedLicenseDao: VerifiedLicenseDao
) : VerifiedRepository {

    override suspend fun saveVerifiedLicense(
        licenseRecord: LicenseRecord,
        remark: String,
        verifiedAt: Long
    ): SaveVerifiedLicenseResult = withContext(Dispatchers.IO) {
        // Defensive check (UI/use-case should already enforce this).
//        if (!licenseRecord.licenseStatus.equals("Active", ignoreCase = true)) {
//            return SaveVerifiedLicenseResult.Error("Only practitioners with an active license can be verified.")
//        }

        val entity = licenseRecord.toVerifiedLicenseEntity(
            verificationLocation = "",
            practitionerPresent = true,
            remark = remark,
            verifiedAt = verifiedAt,
            syncStatus = SyncStatus.Pending
        )

        try {
            verifiedLicenseDao.insertOrUpdate(entity)
            SaveVerifiedLicenseResult.Success
        } catch (t: Throwable) {
            SaveVerifiedLicenseResult.Error(t.message ?: "Unable to save verification.")
        }
    }

    override suspend fun getVerifiedLicenses(): List<VerifiedLicense> = withContext(Dispatchers.IO) {
        val entities = verifiedLicenseDao.getAll()
        entities.map { it.toDomain() }
    }
}

