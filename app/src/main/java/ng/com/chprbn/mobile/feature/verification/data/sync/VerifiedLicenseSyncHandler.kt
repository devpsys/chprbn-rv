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
 * row's `registrationNumber` (the verified table's primary key); the
 * remote source's returned map is keyed by `license_number` which is
 * the same value.
 *
 * The wire endpoint is still per-row, so the remote source loops
 * internally; the handler sees a uniform batched contract regardless.
 * Mirrors the dual-state contract of the exam/assessment handlers: the
 * queue row's status is owned by `SyncBatchRunner`, the verified-list
 * UI reads `VerifiedLicenseEntity.syncStatus` /
 * `lastSyncAttempt` / `syncError`, so the handler keeps that row in
 * sync with each outcome.
 */
class VerifiedLicenseSyncHandler @Inject constructor(
    private val verifiedLicenseDao: VerifiedLicenseDao,
    private val remoteSource: VerifiedSyncRemoteSource,
    private val clock: Clock,
) : SyncEntityHandler {

    override suspend fun uploadBatch(entityKeys: List<String>): Map<String, SyncOutcome> {
        val outcomes = LinkedHashMap<String, SyncOutcome>(entityKeys.size)
        val toUpload = mutableListOf<UploadRow>()

        for (key in entityKeys) {
            val row = verifiedLicenseDao.getByRegistrationNumber(key)
            if (row == null) {
                outcomes[key] = SyncOutcome.Failure("Verified license not found locally: $key")
                continue
            }
            val payload = row.toDomain().toVerifiedSyncRequestDto()
            toUpload.add(UploadRow(entityKey = key, payload = payload))
        }

        if (toUpload.isEmpty()) return outcomes

        val remoteResults = remoteSource.uploadVerifiedBatch(toUpload.map { it.payload })
        val now = clock.nowMillis()

        for (row in toUpload) {
            val key = row.entityKey
            val result = remoteResults[row.payload.licenseNumber]
                ?: Result.failure(IllegalStateException("No remote result for ${row.payload.licenseNumber}"))
            outcomes[key] = result.fold(
                onSuccess = {
                    verifiedLicenseDao.updateSyncMetadata(
                        registrationNumber = key,
                        syncStatus = SyncStatus.Synced.toDbValue(),
                        lastSyncAttempt = now,
                        syncError = null,
                    )
                    SyncOutcome.Success
                },
                onFailure = { t ->
                    val message = t.message?.takeIf { it.isNotBlank() } ?: "Sync failed"
                    verifiedLicenseDao.updateSyncMetadata(
                        registrationNumber = key,
                        syncStatus = SyncStatus.Failed.toDbValue(),
                        lastSyncAttempt = now,
                        syncError = message,
                    )
                    SyncOutcome.Failure(message)
                },
            )
        }

        return outcomes
    }

    private data class UploadRow(
        val entityKey: String,
        val payload: ng.com.chprbn.mobile.feature.verification.data.dto.VerifiedSyncRequestDto,
    )
}
