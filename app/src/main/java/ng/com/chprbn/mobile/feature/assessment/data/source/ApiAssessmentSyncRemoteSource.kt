package ng.com.chprbn.mobile.feature.assessment.data.source

import ng.com.chprbn.mobile.feature.assessment.data.api.AssessmentSyncApiService
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ProjectScoreSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toSyncItemDto
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore
import retrofit2.Response
import javax.inject.Inject

/**
 * Retrofit-backed sync source. Wire format is batch
 * (`POST /assessments/.../batch`) — see `ApiExamSyncRemoteSource` for the
 * same rationale + per-row → batch-of-one bridge until the handler
 * contract (#3) catches up.
 */
class ApiAssessmentSyncRemoteSource @Inject constructor(
    private val api: AssessmentSyncApiService,
) : AssessmentSyncRemoteSource {

    override suspend fun uploadPracticalScore(score: PracticalScore): Result<Unit> =
        runCatching {
            val item = score.toSyncItemDto()
            val response = api.uploadPracticalScoreBatch(
                PracticalScoreSyncBatchRequestDto(items = listOf(item)),
            )
            response.requireSuccess()
            val resultForRow = response.body()?.data?.results?.firstOrNull { it.clientId == item.clientId }
                ?: response.body()?.data?.results?.firstOrNull()
            if (resultForRow != null && !resultForRow.accepted) {
                error(resultForRow.error ?: "Server rejected practical-score row.")
            }
        }

    override suspend fun uploadProjectScore(score: ProjectScore): Result<Unit> =
        runCatching {
            val item = score.toSyncItemDto()
            val response = api.uploadProjectScoreBatch(
                ProjectScoreSyncBatchRequestDto(items = listOf(item)),
            )
            response.requireSuccess()
            val resultForRow = response.body()?.data?.results?.firstOrNull { it.clientId == item.clientId }
                ?: response.body()?.data?.results?.firstOrNull()
            if (resultForRow != null && !resultForRow.accepted) {
                error(resultForRow.error ?: "Server rejected project-score row.")
            }
        }

    private fun Response<*>.requireSuccess() {
        if (!isSuccessful) {
            error("Upload failed: HTTP ${code()} ${message()}")
        }
    }
}
