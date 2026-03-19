package ng.com.chprbn.mobile.feature.verified.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.verified.data.local.VerifiedLicenseDao
import ng.com.chprbn.mobile.feature.verified.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.verified.data.mappers.toVerifiedLicenseEntity
import ng.com.chprbn.mobile.feature.verified.domain.model.SaveVerifiedLicenseResult
import ng.com.chprbn.mobile.feature.verified.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.verified.domain.model.VerifiedLicense
import ng.com.chprbn.mobile.feature.verified.domain.repository.VerifiedRepository
import javax.inject.Inject

class VerifiedRepositoryImpl @Inject constructor(
    private val verifiedLicenseDao: VerifiedLicenseDao
) : VerifiedRepository {

    override suspend fun saveVerifiedLicense(
        licenseRecord: LicenseRecord,
        verificationLocation: String,
        practitionerPresent: Boolean,
        remark: String,
        verifiedAt: Long
    ): SaveVerifiedLicenseResult = withContext(Dispatchers.IO) {
        // Defensive check (UI/use-case should already enforce this).
//        if (!licenseRecord.licenseStatus.equals("Active", ignoreCase = true)) {
//            return SaveVerifiedLicenseResult.Error("Only practitioners with an active license can be verified.")
//        }
        if (!practitionerPresent) {
            return@withContext SaveVerifiedLicenseResult.Error("Practitioner must be marked as verified.")
        }

        val entity = licenseRecord.toVerifiedLicenseEntity(
            verificationLocation = verificationLocation,
            practitionerPresent = practitionerPresent,
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

