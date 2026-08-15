package ng.com.chprbn.mobile.feature.exam.data.api

import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncItemDto
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncResponseDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncBatchEnvelopeDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncBatchRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Served by the jarabawa backend (see [ng.com.chprbn.mobile.feature.exam.data.di.JarabawaNetworkModule]),
 * not the app-wide API — **no bearer token**; `x-location` is the sole
 * request credential, attached transparently by
 * [ng.com.chprbn.mobile.feature.auth.data.network.LocationHeaderInterceptor]
 * from the officer's `adhoc/profile` location — no param needed here.
 *
 * `uploadAttendanceBatch` is confirmed **live** per `docs/mobile-api-guide.html`
 * §5 — see [AttendanceSyncItemDto]'s doc comment for the exact body-shape
 * constraints. `uploadRemarkBatch` (the free-text remark log write) has
 * no confirmed live contract as of this writing — the guide only
 * documents `GET attendance-remarks` (a lookup catalog, not a write) —
 * kept speculative pending confirmation.
 */
interface ExamSyncApiService {

    @POST("attendance/push-record")
    suspend fun uploadAttendanceBatch(
        @Body body: List<AttendanceSyncItemDto>,
    ): Response<AttendanceSyncResponseDto>

    /**
     * [idempotencyKey] — an opaque, client-generated UUID that identifies
     * one *attempt* at a batch. The server persists it against the
     * per-row `client_id`s so a retry (same key + same items) returns the
     * original result rather than re-applying (X3 audit).
     */
    @POST("attendance-remarks")
    suspend fun uploadRemarkBatch(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: RemarkSyncBatchRequestDto,
    ): Response<RemarkSyncBatchEnvelopeDto>
}
