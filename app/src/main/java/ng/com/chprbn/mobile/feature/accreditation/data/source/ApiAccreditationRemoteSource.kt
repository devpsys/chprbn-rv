package ng.com.chprbn.mobile.feature.accreditation.data.source

import com.google.gson.Gson
import ng.com.chprbn.mobile.feature.accreditation.data.api.AccreditationApiService
import ng.com.chprbn.mobile.feature.accreditation.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.accreditation.data.mappers.toRequestDto
import ng.com.chprbn.mobile.feature.accreditation.data.mappers.toSeedBundle
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AccreditationHubData
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssessmentDecision
import ng.com.chprbn.mobile.feature.accreditation.domain.model.InstitutionProfile
import ng.com.chprbn.mobile.feature.accreditation.domain.model.LocalDraftListItem
import ng.com.chprbn.mobile.feature.auth.data.dto.ApiErrorDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Live Retrofit remotes for accreditation. Returns null / false / throws so
 * [CompositeAccreditationRemoteSources] can fall back to Fake when the backend
 * is still TO BE IMPLEMENTED.
 */
@Singleton
class ApiAccreditationRemoteSource @Inject constructor(
    private val api: AccreditationApiService,
    private val gson: Gson,
) : AccreditationAssignmentRemoteSource,
    InstitutionRemoteSource,
    AccreditationDraftRemoteSource,
    AccreditationSyncRemoteSource,
    AssessmentSubmissionRemoteSource {

    override suspend fun getHub(): AccreditationHubData {
        val response = api.getAssignments()
        if (!response.isSuccessful) error(httpError(response.code(), response.errorBody()?.string()))
        val envelope = response.body() ?: error("Empty assignments response")
        if (!envelope.success) error(envelope.message ?: "Assignments rejected")
        return envelope.data?.toDomain() ?: error("Assignments data missing")
    }

    override suspend fun getProfile(institutionId: String): InstitutionProfile? {
        val trimmed = institutionId.trim()
        if (trimmed.isEmpty()) return null
        val response = runCatching { api.getInstitution(trimmed) }.getOrElse { return null }
        if (!response.isSuccessful || response.code() == 404) return null
        val envelope = response.body() ?: return null
        if (!envelope.success) return null
        return envelope.data?.toDomain()
    }

    override suspend fun getSeedBundle(institutionId: String): AssessmentSeedBundle? {
        val trimmed = institutionId.trim()
        if (trimmed.isEmpty()) return null
        val response = runCatching { api.getCurrentAssessment(trimmed) }.getOrElse { return null }
        if (!response.isSuccessful || response.code() == 404) return null
        val envelope = response.body() ?: return null
        if (!envelope.success) return null
        return envelope.data?.toSeedBundle()
    }

    override suspend fun sampleLocalDrafts(): List<LocalDraftListItem> = emptyList()

    override suspend fun uploadDraft(payload: AccreditationDraftSyncPayload): Boolean {
        uploadPendingAttachments(payload)
        val response = api.syncDraft(payload.assessmentId, payload.toRequestDto())
        if (!response.isSuccessful) {
            error(httpError(response.code(), response.errorBody()?.string()))
        }
        val envelope = response.body()
        if (envelope != null && !envelope.success) {
            error(envelope.message ?: "Draft sync rejected")
        }
        return true
    }

    override suspend fun submit(assessmentId: String): String {
        val response = api.submitAssessment(assessmentId)
        if (!response.isSuccessful) {
            error(httpError(response.code(), response.errorBody()?.string()))
        }
        val envelope = response.body() ?: error("Empty submit response")
        if (!envelope.success) error(envelope.message ?: "Submit rejected")
        return envelope.data?.decisionId ?: error("decision_id missing")
    }

    override suspend fun getDecision(decisionId: String): AssessmentDecision? {
        val trimmed = decisionId.trim()
        if (trimmed.isEmpty()) return null
        val response = runCatching { api.getDecision(trimmed) }.getOrElse { return null }
        if (!response.isSuccessful || response.code() == 404) return null
        val envelope = response.body() ?: return null
        if (!envelope.success) return null
        return envelope.data?.toDomain()
    }

    private suspend fun uploadPendingAttachments(payload: AccreditationDraftSyncPayload) {
        for (meta in payload.attachments) {
            val state = meta.uploadState.orEmpty()
            if (state == "Uploaded") continue
            val file = File(meta.localPath)
            if (!file.exists()) continue
            val mime = if (meta.isPhoto) "image/jpeg" else "application/octet-stream"
            val part = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody(mime.toMediaTypeOrNull()),
            )
            val parentKey = meta.parentKey.toPlainBody()
            val title = meta.title.toPlainBody()
            val location = meta.locationLabel?.toPlainBody()
            val response = api.uploadAttachment(
                assessmentId = payload.assessmentId,
                parentKey = parentKey,
                title = title,
                locationLabel = location,
                file = part,
            )
            if (!response.isSuccessful) {
                error(httpError(response.code(), response.errorBody()?.string()))
            }
            val envelope = response.body()
            if (envelope != null && !envelope.success) {
                error(envelope.message ?: "Attachment upload rejected")
            }
        }
    }

    private fun String.toPlainBody() =
        toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())

    private fun httpError(code: Int, raw: String?): String {
        val parsed = raw?.takeIf { it.isNotBlank() }?.let { body ->
            runCatching { gson.fromJson(body, ApiErrorDto::class.java).message }.getOrNull()
                ?: body
        }
        return parsed ?: "HTTP $code"
    }
}
