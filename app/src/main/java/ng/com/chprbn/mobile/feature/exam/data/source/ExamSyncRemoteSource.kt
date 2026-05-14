package ng.com.chprbn.mobile.feature.exam.data.source

import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark

/**
 * Write-side abstraction for the exam sync queue. One row per HTTP
 * request, matching the only template the backend currently offers.
 * Returns [Result.success] on 2xx acceptance, [Result.failure] on any
 * other outcome with the error message in the exception. The feature's
 * sync handlers translate the [Result] to a `core.sync.SyncOutcome`.
 *
 * No `Fake*` companion — uploading to nowhere is never the right dev
 * behaviour. Tests inject a mock.
 */
interface ExamSyncRemoteSource {

    suspend fun uploadAttendance(attendance: Attendance): Result<Unit>

    suspend fun uploadRemark(remark: Remark): Result<Unit>
}
