package ng.com.chprbn.mobile.feature.assessment.data.api

import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncBatchEnvelopeDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ProjectScoreSyncBatchEnvelopeDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ProjectScoreSyncBatchRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * **SPECULATIVE.** Batched score upload. One HTTP request carries up to
 * N rows; the server returns per-row results so a partial-success batch
 * can be reconciled by the client. Per-row dedup is on the row's
 * composite identity, not [client_id] — the latter is purely a response
 * correlation key.
 */
interface AssessmentSyncApiService {

    /**
     * [idempotencyKey] — see `ExamSyncApiService.uploadAttendanceBatch`;
     * X3 audit fix, mirrored so a retry of the same batch is server-side
     * deduplicated rather than double-applied.
     */
    @POST("assessments/practical-scores/batch")
    suspend fun uploadPracticalScoreBatch(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: PracticalScoreSyncBatchRequestDto,
    ): Response<PracticalScoreSyncBatchEnvelopeDto>

    @POST("assessments/project-scores/batch")
    suspend fun uploadProjectScoreBatch(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: ProjectScoreSyncBatchRequestDto,
    ): Response<ProjectScoreSyncBatchEnvelopeDto>
}
