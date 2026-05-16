package ng.com.chprbn.mobile.feature.exam.data.sync

import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceDao
import ng.com.chprbn.mobile.feature.exam.data.mappers.attendanceClientId
import ng.com.chprbn.mobile.feature.exam.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.exam.data.source.ExamSyncRemoteSource
import javax.inject.Inject

/**
 * Plugs the attendance row uploader into the cross-feature
 * `core.sync.SyncWorker` via Hilt multibinding. The runner hands in the
 * batch's slash-delimited entity keys; the handler resolves each row,
 * sends a single batched HTTP request, and flips per-row
 * `attendance.syncStatus` based on each result.
 *
 * One [SyncOutcome] per input key in the returned map. Malformed or
 * missing-local rows are degraded to [SyncOutcome.Failure] without
 * being sent.
 */
class AttendanceSyncHandler @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val remoteSource: ExamSyncRemoteSource,
    private val clock: Clock,
) : SyncEntityHandler {

    override suspend fun uploadBatch(entityKeys: List<String>): Map<String, SyncOutcome> {
        val outcomes = LinkedHashMap<String, SyncOutcome>(entityKeys.size)
        // entityKey → domain row + clientId; rows that survive pre-upload checks.
        val toUpload = mutableListOf<UploadRow>()

        for (key in entityKeys) {
            val parsed = AttendanceKey.decode(key)
            if (parsed == null) {
                outcomes[key] = SyncOutcome.Failure("Malformed attendance key: $key")
                continue
            }
            val (paperId, candidateId) = parsed
            val entity = attendanceDao.getOne(paperId, candidateId)
            if (entity == null) {
                outcomes[key] = SyncOutcome.Failure("Attendance not found locally: $key")
                continue
            }
            val domain = entity.toDomain()
            toUpload.add(
                UploadRow(
                    entityKey = key,
                    domain = domain,
                    clientId = attendanceClientId(domain.paperId, domain.candidateId),
                ),
            )
        }

        if (toUpload.isEmpty()) return outcomes

        val remoteResults = remoteSource.uploadAttendanceBatch(toUpload.map { it.domain })
        val now = clock.nowMillis()

        for (row in toUpload) {
            val result = remoteResults[row.clientId]
                ?: Result.failure(IllegalStateException("No remote result for ${row.clientId}"))
            outcomes[row.entityKey] = result.fold(
                onSuccess = {
                    attendanceDao.updateSyncMetadata(
                        paperId = row.domain.paperId,
                        candidateId = row.domain.candidateId,
                        syncStatus = SyncStatus.Synced.name,
                        syncError = null,
                        lastSyncAttemptAt = now,
                    )
                    SyncOutcome.Success
                },
                onFailure = { t ->
                    val message = t.message ?: "Upload failed."
                    attendanceDao.updateSyncMetadata(
                        paperId = row.domain.paperId,
                        candidateId = row.domain.candidateId,
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
        val domain: ng.com.chprbn.mobile.feature.exam.domain.model.Attendance,
        val clientId: String,
    )
}
