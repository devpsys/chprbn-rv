package ng.com.chprbn.mobile.feature.accreditation.data.api

import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationAssignmentsEnvelopeDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationCurrentAssessmentEnvelopeDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationDecisionEnvelopeDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationDraftSyncRequestDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationInstitutionEnvelopeDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationSubmitEnvelopeDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationSyncEnvelopeDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationUpsertEnvelopeDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Accreditation mobile API under `/api/v1/mobile/accreditation/…`.
 * Bearer auth; role `accreditation`. Backend status: TO BE IMPLEMENTED —
 * see `docs/api/full-api-documentation.md` §10.
 */
interface AccreditationApiService {

    @GET("accreditation/assignments")
    suspend fun getAssignments(): Response<AccreditationAssignmentsEnvelopeDto>

    @GET("accreditation/institutions/{id}")
    suspend fun getInstitution(
        @Path("id") institutionId: String,
    ): Response<AccreditationInstitutionEnvelopeDto>

    @GET("accreditation/institutions/{id}/assessments/current")
    suspend fun getCurrentAssessment(
        @Path("id") institutionId: String,
    ): Response<AccreditationCurrentAssessmentEnvelopeDto>

    @PUT("accreditation/assessments/{id}/sections/{key}")
    suspend fun upsertSection(
        @Path("id") assessmentId: String,
        @Path("key") sectionKey: String,
        @Body body: RequestBody,
    ): Response<AccreditationUpsertEnvelopeDto>

    @PUT("accreditation/assessments/{id}/criteria")
    suspend fun upsertCriteria(
        @Path("id") assessmentId: String,
        @Body body: RequestBody,
    ): Response<AccreditationUpsertEnvelopeDto>

    @PUT("accreditation/assessments/{id}/scores")
    suspend fun upsertScores(
        @Path("id") assessmentId: String,
        @Body body: RequestBody,
    ): Response<AccreditationUpsertEnvelopeDto>

    @Multipart
    @POST("accreditation/assessments/{id}/attachments")
    suspend fun uploadAttachment(
        @Path("id") assessmentId: String,
        @Part("parent_key") parentKey: RequestBody,
        @Part("title") title: RequestBody,
        @Part("location_label") locationLabel: RequestBody?,
        @Part file: MultipartBody.Part,
    ): Response<AccreditationUpsertEnvelopeDto>

    @DELETE("accreditation/assessments/{id}/attachments/{attachmentId}")
    suspend fun deleteAttachment(
        @Path("id") assessmentId: String,
        @Path("attachmentId") attachmentId: String,
    ): Response<AccreditationUpsertEnvelopeDto>

    @POST("accreditation/assessments/{id}/sync")
    suspend fun syncDraft(
        @Path("id") assessmentId: String,
        @Body body: AccreditationDraftSyncRequestDto,
    ): Response<AccreditationSyncEnvelopeDto>

    @POST("accreditation/assessments/{id}/submit")
    suspend fun submitAssessment(
        @Path("id") assessmentId: String,
    ): Response<AccreditationSubmitEnvelopeDto>

    @GET("accreditation/decisions/{decisionId}")
    suspend fun getDecision(
        @Path("decisionId") decisionId: String,
    ): Response<AccreditationDecisionEnvelopeDto>

    @GET("accreditation/institutions/{id}/documents")
    suspend fun getInstitutionDocuments(
        @Path("id") institutionId: String,
    ): Response<AccreditationUpsertEnvelopeDto>

    @GET("accreditation/decisions/{decisionId}/certificate")
    suspend fun getCertificatePdf(
        @Path("decisionId") decisionId: String,
    ): Response<ResponseBody>
}
