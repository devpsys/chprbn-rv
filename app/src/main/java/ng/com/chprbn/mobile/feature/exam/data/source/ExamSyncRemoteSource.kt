package ng.com.chprbn.mobile.feature.exam.data.source

import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark

/**
 * Write-side abstraction for the exam sync queue. One HTTP call per
 * batch — matches the server contract in `docs/api/full-api-documentation.md`
 * §8.2 / §8.3.
 *
 * **Returned map shape.** The key is the row's `clientId` (the string
 * the mapper computes from the row's composite identity; see
 * `SyncPayloadMappers.kt`). Every input row produces exactly one entry
 * in the map. On a transport-level failure (network down, 5xx, parse
 * error) every row is failed with the same exception so the handler
 * still has a uniform per-row result contract.
 *
 * No `Fake*` companion — uploading to nowhere is never the right dev
 * behaviour. Tests inject a mock.
 */
interface ExamSyncRemoteSource {

    suspend fun uploadAttendanceBatch(rows: List<Attendance>): Map<String, Result<Unit>>

    suspend fun uploadRemarkBatch(rows: List<Remark>): Map<String, Result<Unit>>
}
