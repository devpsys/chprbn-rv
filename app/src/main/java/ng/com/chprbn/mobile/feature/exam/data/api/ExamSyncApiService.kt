package ng.com.chprbn.mobile.feature.exam.data.api

import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncEnvelopeDto
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncRequestDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncEnvelopeDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * **SPECULATIVE.** Per-row upload, one HTTP request per row, mirroring
 * the existing `POST /practitioners/verified-sync` template. When the
 * backend eventually offers a batch endpoint, the worker swaps to it
 * without touching `SyncJobDao`.
 */
interface ExamSyncApiService {

    @POST("exam/attendance")
    suspend fun uploadAttendance(
        @Body body: AttendanceSyncRequestDto,
    ): Response<AttendanceSyncEnvelopeDto>

    @POST("exam/remarks")
    suspend fun uploadRemark(
        @Body body: RemarkSyncRequestDto,
    ): Response<RemarkSyncEnvelopeDto>
}
