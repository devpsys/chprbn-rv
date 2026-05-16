package ng.com.chprbn.mobile.feature.exam.data.sync

import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.exam.data.local.RemarkDao
import ng.com.chprbn.mobile.feature.exam.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.exam.data.source.ExamSyncRemoteSource
import javax.inject.Inject

/** Remark equivalent of [AttendanceSyncHandler]. */
class RemarkSyncHandler @Inject constructor(
    private val remarkDao: RemarkDao,
    private val remoteSource: ExamSyncRemoteSource,
    private val clock: Clock,
) : SyncEntityHandler {

    override suspend fun uploadBatch(entityKeys: List<String>): Map<String, SyncOutcome> {
        val outcomes = LinkedHashMap<String, SyncOutcome>(entityKeys.size)
        val toUpload = mutableListOf<UploadRow>()

        for (key in entityKeys) {
            val id = RemarkKey.decode(key)
            if (id == null) {
                outcomes[key] = SyncOutcome.Failure("Malformed remark key: $key")
                continue
            }
            val entity = remarkDao.getById(id)
            if (entity == null) {
                outcomes[key] = SyncOutcome.Failure("Remark not found locally: $key")
                continue
            }
            // Remark's clientId is the row id itself (matches SyncPayloadMappers).
            toUpload.add(UploadRow(entityKey = key, id = id, domain = entity.toDomain()))
        }

        if (toUpload.isEmpty()) return outcomes

        val remoteResults = remoteSource.uploadRemarkBatch(toUpload.map { it.domain })
        val now = clock.nowMillis()

        for (row in toUpload) {
            val result = remoteResults[row.id]
                ?: Result.failure(IllegalStateException("No remote result for ${row.id}"))
            outcomes[row.entityKey] = result.fold(
                onSuccess = {
                    remarkDao.updateSyncMetadata(
                        id = row.id,
                        syncStatus = SyncStatus.Synced.name,
                        syncError = null,
                        lastSyncAttemptAt = now,
                    )
                    SyncOutcome.Success
                },
                onFailure = { t ->
                    val message = t.message ?: "Upload failed."
                    remarkDao.updateSyncMetadata(
                        id = row.id,
                        syncStatus = SyncStatus.Failed.name,
                        syncError = message,
                        lastSyncAttemptAt = now,
                    )
                    SyncOutcome.Failure(message)
                },
            )
        }

        return outcomes
    }

    private data class UploadRow(
        val entityKey: String,
        val id: String,
        val domain: ng.com.chprbn.mobile.feature.exam.domain.model.Remark,
    )
}
