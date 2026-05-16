package ng.com.chprbn.mobile.feature.exam.data.source

import ng.com.chprbn.mobile.feature.exam.data.api.ExamSyncApiService
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.exam.data.mappers.toSyncItemDto
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import retrofit2.Response
import javax.inject.Inject

/**
 * Retrofit-backed sync source. Wraps each upload in `runCatching` so
 * the sync handler never sees a thrown exception — failures arrive as
 * `Result.failure(...)` with a human-readable message.
 *
 * The wire format is batch (`POST /exam/.../batch`) but this source still
 * exposes per-row methods because the handler contract is per-row today
 * (see `SyncBatchRunner` rework #3). It packages a single domain row into
 * a 1-element batch, then inspects the result row matching the client_id
 * we sent. When #3 lands and the handler is rewritten to accept a list,
 * this source can grow a `uploadAttendanceBatch(list)` overload.
 */
class ApiExamSyncRemoteSource @Inject constructor(
    private val api: ExamSyncApiService,
) : ExamSyncRemoteSource {

    override suspend fun uploadAttendance(attendance: Attendance): Result<Unit> =
        runCatching {
            val item = attendance.toSyncItemDto()
            val response = api.uploadAttendanceBatch(
                AttendanceSyncBatchRequestDto(items = listOf(item)),
            )
            response.requireSuccess()
            val resultForRow = response.body()?.data?.results?.firstOrNull { it.clientId == item.clientId }
                ?: response.body()?.data?.results?.firstOrNull()
            if (resultForRow != null && !resultForRow.accepted) {
                error(resultForRow.error ?: "Server rejected attendance row.")
            }
        }

    override suspend fun uploadRemark(remark: Remark): Result<Unit> =
        runCatching {
            val item = remark.toSyncItemDto()
            val response = api.uploadRemarkBatch(
                RemarkSyncBatchRequestDto(items = listOf(item)),
            )
            response.requireSuccess()
            val resultForRow = response.body()?.data?.results?.firstOrNull { it.clientId == item.clientId }
                ?: response.body()?.data?.results?.firstOrNull()
            if (resultForRow != null && !resultForRow.accepted) {
                error(resultForRow.error ?: "Server rejected remark row.")
            }
        }

    private fun Response<*>.requireSuccess() {
        if (!isSuccessful) {
            error("Upload failed: HTTP ${code()} ${message()}")
        }
    }
}
