package ng.com.chprbn.mobile.feature.exam.data.sync

import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceDao
import ng.com.chprbn.mobile.feature.exam.data.local.CandidateDao
import ng.com.chprbn.mobile.feature.exam.data.local.CenterDao
import ng.com.chprbn.mobile.feature.exam.data.mappers.attendanceClientId
import ng.com.chprbn.mobile.feature.exam.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.exam.data.source.AttendanceUploadRow
import ng.com.chprbn.mobile.feature.exam.data.source.ExamSyncRemoteSource
import javax.inject.Inject

/**
 * Plugs the attendance row uploader into the cross-feature
 * `core.sync.SyncWorker` via Hilt multibinding. The runner hands in the
 * batch's slash-delimited entity keys; the handler resolves each row,
 * sends a single batched HTTP request, and flips per-row
 * `attendance.syncStatus` based on each result.
 *
 * `attendance/push-record` needs `scheduledCandidateId`/`scheduleId`
 * (from the cached dossier assignment) and `year` (from the cached
 * centre) — none of which live on the `attendance` row itself, so
 * they're resolved here at sync time via [candidateDao]/[centerDao]
 * rather than duplicated onto `AttendanceEntity`'s schema. A row whose
 * assignment/centre can't be resolved (e.g. attendance marked from a
 * dossier that's since been re-downloaded and no longer contains that
 * candidate) fails with a clear message and self-heals on the next
 * successful dossier download rather than being dropped outright.
 *
 * [ng.com.chprbn.mobile.feature.exam.data.source.AttendanceUploadRow.remark]
 * is left `null` — there's no confirmed mapping yet from the officer's
 * free-text remark log to the single push-record remark code.
 *
 * One [SyncOutcome] per input key in the returned map. Malformed or
 * missing-local rows are degraded to [SyncOutcome.Failure] without
 * being sent.
 */
class AttendanceSyncHandler @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val candidateDao: CandidateDao,
    private val centerDao: CenterDao,
    private val remoteSource: ExamSyncRemoteSource,
    private val clock: Clock,
) : SyncEntityHandler {

    override suspend fun uploadBatch(entityKeys: List<String>): Map<String, SyncOutcome> {
        val outcomes = LinkedHashMap<String, SyncOutcome>(entityKeys.size)
        val toUpload = mutableListOf<UploadRow>()
        val year = centerDao.getFirst()?.year

        for (key in entityKeys) {
            val parsed = AttendanceKey.decode(key)
            if (parsed == null) {
                outcomes[key] = SyncOutcome.Failure("Malformed attendance key: $key")
                continue
            }
            val (paperId, candidateId) = parsed
            val entity = attendanceDao.getOne(paperId, candidateId)
            if (entity == null) {
                // Ghost sync job — the write-path upsert never landed (see
                // enqueue-before-upsert order in AttendanceRepositoryImpl).
                // Drop rather than fail so we don't retry a non-existent row.
                outcomes[key] = SyncOutcome.Drop
                continue
            }
            val assignment = candidateDao.getAssignment(paperId, candidateId)
                ?.takeIf { it.scheduledCandidateId.isNotBlank() && it.scheduleId.isNotBlank() }
            if (assignment == null || year == null) {
                outcomes[key] = SyncOutcome.Failure(
                    "Missing scheduledCandidateId/scheduleId/year for this candidate — " +
                        "re-download today's dossier and retry.",
                )
                continue
            }
            val domain = entity.toDomain()
            toUpload.add(
                UploadRow(
                    entityKey = key,
                    domain = domain,
                    clientId = attendanceClientId(domain.paperId, domain.candidateId),
                    uploadRow = AttendanceUploadRow(
                        paperId = domain.paperId,
                        candidateId = domain.candidateId,
                        scheduledCandidateId = assignment.scheduledCandidateId,
                        scheduleId = assignment.scheduleId,
                        year = year,
                        status = domain.status,
                    ),
                ),
            )
        }

        if (toUpload.isEmpty()) return outcomes

        val remoteResults = remoteSource.uploadAttendanceBatch(toUpload.map { it.uploadRow })
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
        val uploadRow: AttendanceUploadRow,
    )
}
