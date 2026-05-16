package ng.com.chprbn.mobile.feature.exam.data.source

import ng.com.chprbn.mobile.feature.exam.data.api.ExamSyncApiService
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncItemDto
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncResultDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncItemDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncResultDto
import ng.com.chprbn.mobile.feature.exam.data.mappers.toSyncItemDto
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import retrofit2.Response
import javax.inject.Inject

/**
 * Retrofit-backed batched sync source. Sends one batched POST per call
 * and walks the response's per-row `results` array, producing a map
 * keyed by each row's `client_id`.
 *
 * Transport-level failure (non-2xx, network, parse error) is folded
 * into a per-row failure for every input row so the handler can still
 * map back uniformly.
 */
class ApiExamSyncRemoteSource @Inject constructor(
    private val api: ExamSyncApiService,
) : ExamSyncRemoteSource {

    override suspend fun uploadAttendanceBatch(
        rows: List<Attendance>,
    ): Map<String, Result<Unit>> {
        if (rows.isEmpty()) return emptyMap()
        val items: List<AttendanceSyncItemDto> = rows.map { it.toSyncItemDto() }

        val transportOutcome: Result<List<AttendanceSyncResultDto>> = runCatching {
            val response = api.uploadAttendanceBatch(
                AttendanceSyncBatchRequestDto(items = items),
            )
            response.requireSuccessOrThrow()
            response.body()?.data?.results.orEmpty()
        }

        return foldBatchResults(
            clientIds = items.map { it.clientId },
            transportOutcome = transportOutcome,
            acceptedOf = { it.accepted },
            errorOf = { it.error },
            clientIdOf = { it.clientId },
        )
    }

    override suspend fun uploadRemarkBatch(
        rows: List<Remark>,
    ): Map<String, Result<Unit>> {
        if (rows.isEmpty()) return emptyMap()
        val items: List<RemarkSyncItemDto> = rows.map { it.toSyncItemDto() }

        val transportOutcome: Result<List<RemarkSyncResultDto>> = runCatching {
            val response = api.uploadRemarkBatch(
                RemarkSyncBatchRequestDto(items = items),
            )
            response.requireSuccessOrThrow()
            response.body()?.data?.results.orEmpty()
        }

        return foldBatchResults(
            clientIds = items.map { it.clientId },
            transportOutcome = transportOutcome,
            acceptedOf = { it.accepted },
            errorOf = { it.error },
            clientIdOf = { it.clientId },
        )
    }

    private fun Response<*>.requireSuccessOrThrow() {
        if (!isSuccessful) {
            error("Upload failed: HTTP ${code()} ${message()}")
        }
    }
}

/**
 * Shared batch-result fold: walks the per-row `results` array (when the
 * transport call succeeded) or applies the same transport-level failure
 * to every input clientId.
 */
internal fun <R> foldBatchResults(
    clientIds: List<String>,
    transportOutcome: Result<List<R>>,
    acceptedOf: (R) -> Boolean,
    errorOf: (R) -> String?,
    clientIdOf: (R) -> String?,
): Map<String, Result<Unit>> = transportOutcome.fold(
    onSuccess = { rows ->
        val byClientId: Map<String, R> = rows.associateBy { clientIdOf(it).orEmpty() }
        clientIds.associateWith { id ->
            val r = byClientId[id]
            when {
                r == null -> Result.failure(
                    IllegalStateException("Server returned no result for $id"),
                )
                acceptedOf(r) -> Result.success(Unit)
                else -> Result.failure(
                    IllegalStateException(errorOf(r) ?: "Server rejected row."),
                )
            }
        }
    },
    onFailure = { t ->
        clientIds.associateWith { Result.failure<Unit>(t) }
    },
)
