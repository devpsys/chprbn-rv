package ng.com.chprbn.mobile.feature.exam.data.source

import ng.com.chprbn.mobile.feature.exam.data.api.ExamSyncApiService
import ng.com.chprbn.mobile.feature.exam.data.mappers.toSyncRequestDto
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import retrofit2.Response
import javax.inject.Inject

/**
 * Retrofit-backed sync source. Wraps each upload in `runCatching` so
 * the sync handler never sees a thrown exception — failures arrive as
 * `Result.failure(...)` with a human-readable message.
 *
 * Acceptance is `response.isSuccessful` (any 2xx) — matches
 * verification's deliberate flexibility on whether the server returns
 * an enveloped body or 204 No Content.
 */
class ApiExamSyncRemoteSource @Inject constructor(
    private val api: ExamSyncApiService,
) : ExamSyncRemoteSource {

    override suspend fun uploadAttendance(attendance: Attendance): Result<Unit> =
        runCatching {
            val response = api.uploadAttendance(attendance.toSyncRequestDto())
            response.requireSuccess()
        }

    override suspend fun uploadRemark(remark: Remark): Result<Unit> =
        runCatching {
            val response = api.uploadRemark(remark.toSyncRequestDto())
            response.requireSuccess()
        }

    private fun Response<*>.requireSuccess() {
        if (!isSuccessful) {
            error("Upload failed: HTTP ${code()} ${message()}")
        }
    }
}
