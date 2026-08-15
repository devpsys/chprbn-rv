package ng.com.chprbn.mobile.feature.exam.data.source

import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark

/**
 * A local attendance row plus everything `attendance/push-record`
 * requires that isn't stored on [ng.com.chprbn.mobile.feature.exam.domain.model.Attendance]
 * itself — [scheduledCandidateId]/[scheduleId] (resolved from the
 * cached dossier assignment) and [year] (from the cached centre) —
 * resolved by `AttendanceSyncHandler` at sync time.
 */
data class AttendanceUploadRow(
    val paperId: String,
    val candidateId: String,
    val scheduledCandidateId: String,
    val scheduleId: String,
    val year: Int,
    val status: AttendanceStatus,
    val remark: String? = null,
)

/**
 * Write-side abstraction for the exam sync queue. One HTTP call per
 * batch.
 *
 * **Returned map shape.** The key is the row's `clientId` (the string
 * the mapper computes from the row's composite identity; see
 * `SyncPayloadMappers.kt`). Every input row produces exactly one entry
 * in the map. On a transport-level failure (network down, 5xx, parse
 * error) every row is failed with the same exception so the handler
 * still has a uniform per-row result contract.
 *
 * `uploadAttendanceBatch` is confirmed live per `docs/mobile-api-guide.html`
 * §5 — see [ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncItemDto]'s
 * doc comment. The server returns no per-row results for this endpoint
 * (just the whole batch's `status` + a list of processed `candidate_id`s),
 * so every row in a batch shares one outcome — a partial failure inside
 * a mixed batch can't be distinguished client-side.
 *
 * `uploadRemarkBatch` matches `docs/api/full-api-documentation.md` §8.3 —
 * unconfirmed against a live response as of this writing (see
 * [ng.com.chprbn.mobile.feature.exam.data.api.ExamSyncApiService]'s doc
 * comment).
 *
 * No `Fake*` companion — uploading to nowhere is never the right dev
 * behaviour. Tests inject a mock.
 */
interface ExamSyncRemoteSource {

    suspend fun uploadAttendanceBatch(rows: List<AttendanceUploadRow>): Map<String, Result<Unit>>

    suspend fun uploadRemarkBatch(rows: List<Remark>): Map<String, Result<Unit>>
}
