package ng.com.chprbn.mobile.feature.verification.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import ng.com.chprbn.mobile.core.sync.SyncJobEntity
import ng.com.chprbn.mobile.feature.verification.data.local.VerifiedLicenseDao
import ng.com.chprbn.mobile.feature.verification.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.verification.data.mappers.toVerifiedLicenseEntity
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.verification.domain.model.SaveVerifiedLicenseResult
import ng.com.chprbn.mobile.feature.verification.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.verification.domain.model.VerifiedLicense
import ng.com.chprbn.mobile.feature.verification.domain.repository.VerifiedRepository
import ng.com.chprbn.mobile.core.domain.model.SyncStatus as CoreSyncStatus
import javax.inject.Inject

class VerifiedRepositoryImpl @Inject constructor(
    private val verifiedLicenseDao: VerifiedLicenseDao,
    private val syncJobDao: SyncJobDao,
    private val clock: Clock,
) : VerifiedRepository {

    override suspend fun saveVerifiedLicense(
        licenseRecord: LicenseRecord,
        remark: String,
        verifiedAt: Long
    ): SaveVerifiedLicenseResult = withContext(Dispatchers.IO) {
        val entity = licenseRecord.toVerifiedLicenseEntity(
            verificationLocation = "",
            practitionerPresent = true,
            remark = remark,
            verifiedAt = verifiedAt,
            syncStatus = SyncStatus.Pending
        )

        try {
            verifiedLicenseDao.insertOrUpdate(entity)
            // Enqueue onto the cross-feature outbox so the canonical
            // `SyncBatchRunner` picks the row up next batch (background or
            // user-initiated). Same pattern as Attendance / Remark /
            // PracticalScore / ProjectScore.
            syncJobDao.enqueue(
                SyncJobEntity(
                    entityType = SyncEntityType.VerifiedLicense.name,
                    entityKey = entity.registrationNumber,
                    enqueuedAt = clock.nowMillis(),
                    status = CoreSyncStatus.Pending.name,
                ),
            )
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
