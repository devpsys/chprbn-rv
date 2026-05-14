package ng.com.chprbn.mobile.feature.assessment.data.api

import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncEnvelopeDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncRequestDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ProjectScoreSyncEnvelopeDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ProjectScoreSyncRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * **SPECULATIVE.** Per-row upload, one HTTP request per row, mirroring the
 * existing `POST /practitioners/verified-sync` template. When the backend
 * eventually offers a batch endpoint, the worker swaps to it without
 * touching `SyncJobDao`.
 */
interface AssessmentSyncApiService {

    @POST("assessments/practical-scores")
    suspend fun uploadPracticalScore(
        @Body body: PracticalScoreSyncRequestDto,
    ): Response<PracticalScoreSyncEnvelopeDto>

    @POST("assessments/project-scores")
    suspend fun uploadProjectScore(
        @Body body: ProjectScoreSyncRequestDto,
    ): Response<ProjectScoreSyncEnvelopeDto>
}
