package ng.com.chprbn.mobile.feature.exam.data.api

import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncBatchEnvelopeDto
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncBatchEnvelopeDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncBatchRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
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
 *
 * `x-location` is attached transparently by [ng.com.chprbn.mobile.feature.auth.data.network.LocationHeaderInterceptor]
 * from the officer's `adhoc/profile` location — no param needed here.
 */
interface ExamSyncApiService {

    /**
     * [idempotencyKey] — an opaque, client-generated UUID that identifies
     * one *attempt* at a batch. The server persists it against the
     * per-row `client_id`s so a retry (same key + same items) returns the
     * original result rather than re-applying (X3 audit).
     */
    @POST("attendance/push-record")
    suspend fun uploadAttendanceBatch(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: AttendanceSyncBatchRequestDto,
    ): Response<AttendanceSyncBatchEnvelopeDto>

    @POST("attendance-remarks")
    suspend fun uploadRemarkBatch(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: RemarkSyncBatchRequestDto,
    ): Response<RemarkSyncBatchEnvelopeDto>
}
