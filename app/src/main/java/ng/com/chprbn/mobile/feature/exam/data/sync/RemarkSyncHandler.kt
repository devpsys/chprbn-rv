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

    override suspend fun upload(entityKey: String): SyncOutcome {
        val id = RemarkKey.decode(entityKey)
            ?: return SyncOutcome.Failure("Malformed remark key: $entityKey")

        val entity = remarkDao.getById(id)
            ?: return SyncOutcome.Failure("Remark not found locally: $entityKey")

        val result = remoteSource.uploadRemark(entity.toDomain())
        val now = clock.nowMillis()
        return result.fold(
            onSuccess = {
                remarkDao.updateSyncMetadata(
                    id = id,
                    syncStatus = SyncStatus.Synced.name,
                    syncError = null,
                    lastSyncAttemptAt = now,
                )
                SyncOutcome.Success
            },
            onFailure = { t ->
                val message = t.message ?: "Upload failed."
                remarkDao.updateSyncMetadata(
                    id = id,
                    syncStatus = SyncStatus.Failed.name,
                    syncError = message,
                    lastSyncAttemptAt = now,
                )
                SyncOutcome.Failure(message)
            },
        )
    }
}
