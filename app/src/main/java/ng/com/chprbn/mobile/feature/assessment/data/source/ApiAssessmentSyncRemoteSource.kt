package ng.com.chprbn.mobile.feature.assessment.data.source

import ng.com.chprbn.mobile.feature.assessment.data.api.AssessmentSyncApiService
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncItemDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncResultDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ProjectScoreSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ProjectScoreSyncItemDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ProjectScoreSyncResultDto
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toSyncItemDto
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore
import retrofit2.Response
import javax.inject.Inject

/**
 * Retrofit-backed batched sync source for the assessment feature. Same
 * shape as `ApiExamSyncRemoteSource` — one batched HTTP request per
 * call, response is a per-row results array keyed by `client_id`.
 */
class ApiAssessmentSyncRemoteSource @Inject constructor(
    private val api: AssessmentSyncApiService,
) : AssessmentSyncRemoteSource {

    override suspend fun uploadPracticalScoreBatch(
        rows: List<PracticalScore>,
    ): Map<String, Result<Unit>> {
        if (rows.isEmpty()) return emptyMap()
        val items: List<PracticalScoreSyncItemDto> = rows.map { it.toSyncItemDto() }

        val transportOutcome: Result<List<PracticalScoreSyncResultDto>> = runCatching {
            val response = api.uploadPracticalScoreBatch(
                PracticalScoreSyncBatchRequestDto(items = items),
            )
            response.requireSuccessOrThrow()
            response.body()?.data?.results.orEmpty()
        }

        return foldBatchResults(
            clientIds = items.map { it.clientId },
            transportOutcome = transportOutcome,
            acceptedOf = { it.accepted },
            errorOf = { it.error },
            clientIdOf = { it.clientId },
        )
    }

    override suspend fun uploadProjectScoreBatch(
        rows: List<ProjectScore>,
    ): Map<String, Result<Unit>> {
        if (rows.isEmpty()) return emptyMap()
        val items: List<ProjectScoreSyncItemDto> = rows.map { it.toSyncItemDto() }

        val transportOutcome: Result<List<ProjectScoreSyncResultDto>> = runCatching {
            val response = api.uploadProjectScoreBatch(
                ProjectScoreSyncBatchRequestDto(items = items),
            )
            response.requireSuccessOrThrow()
            response.body()?.data?.results.orEmpty()
        }

        return foldBatchResults(
            clientIds = items.map { it.clientId },
            transportOutcome = transportOutcome,
            acceptedOf = { it.accepted },
            errorOf = { it.error },
            clientIdOf = { it.clientId },
        )
    }

    private fun Response<*>.requireSuccessOrThrow() {
        if (!isSuccessful) {
            error("Upload failed: HTTP ${code()} ${message()}")
        }
    }
}

/**
 * Shared batch-result fold (copy of the exam-side helper — same logic,
 * separate file so each feature's data layer compiles independently).
 */
private fun <R> foldBatchResults(
    clientIds: List<String>,
    transportOutcome: Result<List<R>>,
    acceptedOf: (R) -> Boolean,
    errorOf: (R) -> String?,
    clientIdOf: (R) -> String?,
): Map<String, Result<Unit>> = transportOutcome.fold(
    onSuccess = { rows ->
        val byClientId: Map<String, R> = rows.associateBy { clientIdOf(it).orEmpty() }
        clientIds.associateWith { id ->
            val r = byClientId[id]
            when {
                r == null -> Result.failure(
                    IllegalStateException("Server returned no result for $id"),
                )
                acceptedOf(r) -> Result.success(Unit)
                else -> Result.failure(
                    IllegalStateException(errorOf(r) ?: "Server rejected row."),
                )
            }
        }
    },
    onFailure = { t ->
        clientIds.associateWith { Result.failure<Unit>(t) }
    },
)
