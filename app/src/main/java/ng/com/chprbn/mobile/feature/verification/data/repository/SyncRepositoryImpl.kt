package ng.com.chprbn.mobile.feature.verification.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.feature.verification.data.mappers.toVerifiedSyncRequestDto
import ng.com.chprbn.mobile.feature.verification.data.source.VerifiedSyncRemoteSource
import ng.com.chprbn.mobile.feature.verification.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.feature.verification.domain.model.SyncRecord
import ng.com.chprbn.mobile.feature.verification.domain.repository.SyncRepository
import ng.com.chprbn.mobile.feature.verification.data.local.VerifiedLicenseDao
import ng.com.chprbn.mobile.feature.verification.data.mappers.toDbValue
import ng.com.chprbn.mobile.feature.verification.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.verification.domain.model.SyncStatus
import javax.inject.Inject

class SyncRepositoryImpl @Inject constructor(
    private val verifiedLicenseDao: VerifiedLicenseDao,
    private val remoteSource: VerifiedSyncRemoteSource
) : SyncRepository {

    override suspend fun getSyncRecords(): List<SyncRecord> = withContext(Dispatchers.IO) {
        verifiedLicenseDao.getAll().map { it.toDomain() }
    }

    override suspend fun syncAllPendingAndFailed(): SyncBatchResult = withContext(Dispatchers.IO) {
        val rows = verifiedLicenseDao.getPendingOrFailed()
        syncEach(rows.map { it.registrationNumber to it.toDomain() })
    }

    override suspend fun retryFailed(): SyncBatchResult = withContext(Dispatchers.IO) {
        val rows = verifiedLicenseDao.getFailed()
        syncEach(rows.map { it.registrationNumber to it.toDomain() })
    }

    private suspend fun syncEach(
        items: List<Pair<String, SyncRecord>>
    ): SyncBatchResult {
        if (items.isEmpty()) {
            return SyncBatchResult(attempted = 0, succeeded = 0, failed = 0)
        }
        var succeeded = 0
        var failed = 0
        val errorLines = mutableListOf<String>()
        val now = System.currentTimeMillis()

        for ((registrationNumber, record) in items) {
            val payload = record.toVerifiedSyncRequestDto()
            val result = remoteSource.uploadVerifiedRecord(payload)
            result.fold(
                onSuccess = {
                    verifiedLicenseDao.updateSyncMetadata(
                        registrationNumber = registrationNumber,
                        syncStatus = SyncStatus.Synced.toDbValue(),
                        lastSyncAttempt = now,
                        syncError = null
                    )
                    succeeded++
                },
                onFailure = { t ->
                    val message = t.message?.takeIf { it.isNotBlank() } ?: "Sync failed"
                    verifiedLicenseDao.updateSyncMetadata(
                        registrationNumber = registrationNumber,
                        syncStatus = SyncStatus.Failed.toDbValue(),
                        lastSyncAttempt = now,
                        syncError = message
                    )
                    failed++
                    errorLines += "$registrationNumber: $message"
                }
            )
        }

        return SyncBatchResult(
            attempted = items.size,
            succeeded = succeeded,
            failed = failed,
            errors = errorLines
        )
    }
}
