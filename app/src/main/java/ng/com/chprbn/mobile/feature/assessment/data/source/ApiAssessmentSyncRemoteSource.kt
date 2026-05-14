package ng.com.chprbn.mobile.feature.assessment.data.source

import ng.com.chprbn.mobile.feature.assessment.data.api.AssessmentSyncApiService
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toSyncRequestDto
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore
import retrofit2.Response
import javax.inject.Inject

/**
 * Retrofit-backed sync source. Wraps each upload in `runCatching` so the
 * sync handler never sees a thrown exception — failures arrive as
 * `Result.failure(IOException(...))` or similar with a human-readable
 * message.
 *
 * Acceptance is `response.isSuccessful` (any 2xx) — matches verification's
 * deliberate flexibility on whether the server returns an enveloped body
 * or 204 No Content.
 */
class ApiAssessmentSyncRemoteSource @Inject constructor(
    private val api: AssessmentSyncApiService,
) : AssessmentSyncRemoteSource {

    override suspend fun uploadPracticalScore(score: PracticalScore): Result<Unit> =
        runCatching {
            val response = api.uploadPracticalScore(score.toSyncRequestDto())
            response.requireSuccess()
        }

    override suspend fun uploadProjectScore(score: ProjectScore): Result<Unit> =
        runCatching {
            val response = api.uploadProjectScore(score.toSyncRequestDto())
            response.requireSuccess()
        }

    private fun Response<*>.requireSuccess() {
        if (!isSuccessful) {
            error("Upload failed: HTTP ${code()} ${message()}")
        }
    }
}
