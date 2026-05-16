package ng.com.chprbn.mobile.feature.exam.data.api

import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncBatchEnvelopeDto
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncBatchEnvelopeDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncBatchRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * **SPECULATIVE.** Batched per-feature upload. One HTTP request carries
 * up to N rows; the server returns per-row results so a partial-success
 * batch can be reconciled by the client. Per-row dedup is on the row's
 * composite identity, not [client_id] — the latter is purely a response
 * correlation key.
 *
 * Replaces the legacy per-row template; the legacy verified-sync endpoint
 * still ships per-row pending its own batch upgrade.
 */
interface ExamSyncApiService {

    @POST("exam/attendance/batch")
    suspend fun uploadAttendanceBatch(
        @Body body: AttendanceSyncBatchRequestDto,
    ): Response<AttendanceSyncBatchEnvelopeDto>

    @POST("exam/remarks/batch")
    suspend fun uploadRemarkBatch(
        @Body body: RemarkSyncBatchRequestDto,
    ): Response<RemarkSyncBatchEnvelopeDto>
}
