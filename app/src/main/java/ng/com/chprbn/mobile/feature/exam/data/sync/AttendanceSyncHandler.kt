package ng.com.chprbn.mobile.feature.exam.data.sync

import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceDao
import ng.com.chprbn.mobile.feature.exam.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.exam.data.source.ExamSyncRemoteSource
import javax.inject.Inject

/**
 * Plugs the attendance row uploader into the cross-feature
 * `core.sync.SyncWorker` via Hilt multibinding. The runner hands in the
 * row's slash-delimited entity key; the handler resolves the row,
 * uploads it, and flips `attendance.syncStatus` to match the result.
 *
 * `SyncOutcome.Success` deletes the queue row; `SyncOutcome.Failure`
 * leaves it for the worker's backoff retry.
 */
class AttendanceSyncHandler @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val remoteSource: ExamSyncRemoteSource,
    private val clock: Clock,
) : SyncEntityHandler {

    override suspend fun upload(entityKey: String): SyncOutcome {
        val parsed = AttendanceKey.decode(entityKey)
            ?: return SyncOutcome.Failure("Malformed attendance key: $entityKey")
        val (paperId, candidateId) = parsed

        val entity = attendanceDao.getOne(paperId, candidateId)
            ?: return SyncOutcome.Failure("Attendance not found locally: $entityKey")

        val result = remoteSource.uploadAttendance(entity.toDomain())
        val now = clock.nowMillis()
        return result.fold(
            onSuccess = {
                attendanceDao.updateSyncMetadata(
                    paperId = paperId,
                    candidateId = candidateId,
                    syncStatus = SyncStatus.Synced.name,
                    syncError = null,
                    lastSyncAttemptAt = now,
                )
                SyncOutcome.Success
            },
            onFailure = { t ->
                val message = t.message ?: "Upload failed."
                attendanceDao.updateSyncMetadata(
                    paperId = paperId,
                    candidateId = candidateId,
                    syncStatus = SyncStatus.Failed.name,
                    syncError = message,
                    lastSyncAttemptAt = now,
                )
                SyncOutcome.Failure(message)
            },
        )
    }
}
