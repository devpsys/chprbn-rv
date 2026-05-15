package ng.com.chprbn.mobile.feature.verification.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncBatchRunner
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import ng.com.chprbn.mobile.core.sync.SyncJobEntity
import ng.com.chprbn.mobile.feature.verification.data.local.VerifiedLicenseDao
import ng.com.chprbn.mobile.feature.verification.data.local.VerifiedLicenseEntity
import ng.com.chprbn.mobile.feature.verification.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.verification.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.feature.verification.domain.model.SyncRecord
import ng.com.chprbn.mobile.feature.verification.domain.repository.SyncRepository
import ng.com.chprbn.mobile.core.domain.model.SyncStatus as CoreSyncStatus
import javax.inject.Inject

/**
 * Verified-license sync now rides the cross-feature outbox queue
 * ([SyncBatchRunner] + [VerifiedLicenseSyncHandler]) like every other
 * feature does. This class keeps the legacy [SyncRepository] interface so
 * the existing `SyncViewModel` / `SyncAllRecordsUseCase` keep working; it
 * just becomes a thin adapter:
 *
 * 1. Backfill any `verified_licenses` rows whose `syncStatus` is
 *    `Pending` / `Failed` but have no matching `sync_jobs` entry (covers
 *    rows persisted before this refactor + any drift). The unique index on
 *    `(entityType, entityKey)` makes the insert idempotent.
 * 2. Delegate to [SyncBatchRunner.runBatch], which dispatches each job
 *    through the bound [VerifiedLicenseSyncHandler]; the handler writes
 *    the local `verifiedLicenseDao` syncStatus + lastSyncAttempt + syncError
 *    just like the legacy direct-POST flow did, so the verified-list UI
 *    keeps reading the same fields.
 */
class SyncRepositoryImpl @Inject constructor(
    private val verifiedLicenseDao: VerifiedLicenseDao,
    private val syncJobDao: SyncJobDao,
    private val batchRunner: SyncBatchRunner,
    private val clock: Clock,
) : SyncRepository {

    override suspend fun getSyncRecords(): List<SyncRecord> = withContext(Dispatchers.IO) {
        verifiedLicenseDao.getAll().map { it.toDomain() }
    }

    override suspend fun syncAllPendingAndFailed(): SyncBatchResult = withContext(Dispatchers.IO) {
        backfillQueue(verifiedLicenseDao.getPendingOrFailed())
        batchRunner.runBatch().toFeatureResult()
    }

    override suspend fun retryFailed(): SyncBatchResult = withContext(Dispatchers.IO) {
        backfillQueue(verifiedLicenseDao.getFailed())
        batchRunner.runBatch().toFeatureResult()
    }

    private suspend fun backfillQueue(rows: List<VerifiedLicenseEntity>) {
        if (rows.isEmpty()) return
        val now = clock.nowMillis()
        rows.forEach { row ->
            // The unique index on (entityType, entityKey) makes this
            // idempotent — a row that's already enqueued just gets its
            // existing job preserved.
            syncJobDao.enqueue(
                SyncJobEntity(
                    entityType = SyncEntityType.VerifiedLicense.name,
                    entityKey = row.registrationNumber,
                    enqueuedAt = now,
                    status = CoreSyncStatus.Pending.name,
                ),
            )
        }
    }

    private fun ng.com.chprbn.mobile.core.domain.model.SyncBatchResult.toFeatureResult(): SyncBatchResult =
        SyncBatchResult(
            attempted = attempted,
            succeeded = succeeded,
            failed = failed,
            errors = errors,
        )
}
