package ng.com.chprbn.mobile.feature.verification.data.sync

import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.verification.data.local.VerifiedLicenseDao
import ng.com.chprbn.mobile.feature.verification.data.mappers.toDbValue
import ng.com.chprbn.mobile.feature.verification.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.verification.data.mappers.toVerifiedSyncRequestDto
import ng.com.chprbn.mobile.feature.verification.data.source.VerifiedSyncRemoteSource
import ng.com.chprbn.mobile.feature.verification.domain.model.SyncStatus
import javax.inject.Inject

/**
 * Plugs the verified-license uploader into the cross-feature
 * `core.sync.SyncWorker` via Hilt multibinding. The entityKey is the
 * candidate's registrationNumber (the verified table's primary key).
 *
 * Mirrors the dual-state contract of [ng.com.chprbn.mobile.feature.exam.data.sync.AttendanceSyncHandler]:
 * the queue row's status is owned by `SyncBatchRunner`, but the verified-list
 * UI reads `VerifiedLicenseEntity.syncStatus` / `lastSyncAttempt` /
 * `syncError`, so the handler keeps that row in sync with the outcome.
 */
class VerifiedLicenseSyncHandler @Inject constructor(
    private val verifiedLicenseDao: VerifiedLicenseDao,
    private val remoteSource: VerifiedSyncRemoteSource,
    private val clock: Clock,
) : SyncEntityHandler {

    override suspend fun upload(entityKey: String): SyncOutcome {
        val row = verifiedLicenseDao.getByRegistrationNumber(entityKey)
            ?: return SyncOutcome.Failure("Verified license not found locally: $entityKey")

        val payload = row.toDomain().toVerifiedSyncRequestDto()
        val now = clock.nowMillis()

        return remoteSource.uploadVerifiedRecord(payload).fold(
            onSuccess = {
                verifiedLicenseDao.updateSyncMetadata(
                    registrationNumber = entityKey,
                    syncStatus = SyncStatus.Synced.toDbValue(),
                    lastSyncAttempt = now,
                    syncError = null,
                )
                SyncOutcome.Success
            },
            onFailure = { t ->
                val message = t.message?.takeIf { it.isNotBlank() } ?: "Sync failed"
                verifiedLicenseDao.updateSyncMetadata(
                    registrationNumber = entityKey,
                    syncStatus = SyncStatus.Failed.toDbValue(),
                    lastSyncAttempt = now,
                    syncError = message,
                )
                SyncOutcome.Failure(message)
            },
        )
    }
}
