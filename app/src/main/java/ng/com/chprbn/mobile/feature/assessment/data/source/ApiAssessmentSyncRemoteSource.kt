package ng.com.chprbn.mobile.feature.assessment.data.source

import ng.com.chprbn.mobile.feature.assessment.data.api.AssessmentSyncApiService
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalPushItemDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalPushRequestDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ProjectPushItemDto
import ng.com.chprbn.mobile.feature.assessment.data.mappers.wireQuestionId
import retrofit2.Response
import javax.inject.Inject

/**
 * Retrofit-backed batched sync source matching `docs/mobile-api-guide.html`
 * §6 / §7. Same whole-batch outcome model as attendance push: the server
 * returns no per-row results.
 */
class ApiAssessmentSyncRemoteSource @Inject constructor(
    private val api: AssessmentSyncApiService,
) : AssessmentSyncRemoteSource {

    override suspend fun uploadPracticalScoreBatch(
        rows: List<PracticalScoreUploadRow>,
    ): Map<String, Result<Unit>> {
        if (rows.isEmpty()) return emptyMap()

        val outcomes = LinkedHashMap<String, Result<Unit>>(rows.size)
        val items = mutableListOf<PracticalPushItemDto>()
        val itemKeys = mutableListOf<String>()

        for (row in rows) {
            val dto = row.toPushItemDtoOrNull()
            if (dto == null) {
                outcomes[row.clientId] = Result.failure(
                    IllegalStateException(
                        "Cannot push practical score for candidate ${row.candidateId}: " +
                            "missing/non-numeric scheduledCandidateId, scheduleId, candidateId, " +
                            "paperId, or questionId.",
                    ),
                )
                continue
            }
            items.add(dto)
            itemKeys.add(row.clientId)
        }

        if (items.isEmpty()) return outcomes

        val transportOutcome: Result<Unit> = runCatching {
            val response = api.uploadPracticalScoreBatch(
                PracticalPushRequestDto(practicals = items, projects = emptyList()),
            )
            response.requireSuccessOrThrow()
            val envelope = response.body()
                ?: error("Practical-score batch: empty response body.")
            if (!envelope.status) {
                error(envelope.message ?: "Practical-score batch rejected by server.")
            }
        }

        transportOutcome.fold(
            onSuccess = { itemKeys.forEach { outcomes[it] = Result.success(Unit) } },
            onFailure = { t -> itemKeys.forEach { outcomes[it] = Result.failure(t) } },
        )
        return outcomes
    }

    override suspend fun uploadProjectScoreBatch(
        rows: List<ProjectScoreUploadRow>,
    ): Map<String, Result<Unit>> {
        if (rows.isEmpty()) return emptyMap()

        val outcomes = LinkedHashMap<String, Result<Unit>>(rows.size)
        val items = mutableListOf<ProjectPushItemDto>()
        val itemKeys = mutableListOf<String>()

        for (row in rows) {
            val dto = row.toPushItemDtoOrNull()
            if (dto == null) {
                outcomes[row.clientId] = Result.failure(
                    IllegalStateException(
                        "Cannot push project score for candidate ${row.candidateId}: " +
                            "missing/non-numeric scheduledCandidateId, scheduleId, candidateId, or paperId.",
                    ),
                )
                continue
            }
            items.add(dto)
            itemKeys.add(row.clientId)
        }

        if (items.isEmpty()) return outcomes

        val transportOutcome: Result<Unit> = runCatching {
            val response = api.uploadProjectScoreBatch(items)
            response.requireSuccessOrThrow()
            val envelope = response.body()
                ?: error("Project-score batch: empty response body.")
            if (!envelope.status) {
                error(envelope.message ?: "Project-score batch rejected by server.")
            }
        }

        transportOutcome.fold(
            onSuccess = { itemKeys.forEach { outcomes[it] = Result.success(Unit) } },
            onFailure = { t -> itemKeys.forEach { outcomes[it] = Result.failure(t) } },
        )
        return outcomes
    }

    private fun PracticalScoreUploadRow.toPushItemDtoOrNull(): PracticalPushItemDto? {
        val scheduledCandidateIdLong = scheduledCandidateId.toLongOrNull() ?: return null
        val candidateIdLong = candidateId.toLongOrNull() ?: return null
        val paperIdLong = paperId.toLongOrNull() ?: return null
        val questionIdLong = wireQuestionId(questionId) ?: return null
        val scheduleIdLong = scheduleId.toLongOrNull() ?: return null
        return PracticalPushItemDto(
            scheduledCandidateId = scheduledCandidateIdLong,
            candidateId = candidateIdLong,
            paperId = paperIdLong,
            questionId = questionIdLong,
            scheduleId = scheduleIdLong,
            score = score.toDouble(),
        )
    }

    private fun ProjectScoreUploadRow.toPushItemDtoOrNull(): ProjectPushItemDto? {
        val scheduledCandidateIdLong = scheduledCandidateId.toLongOrNull() ?: return null
        val candidateIdLong = candidateId.toLongOrNull() ?: return null
        val paperIdLong = paperId.toLongOrNull() ?: return null
        val scheduleIdLong = scheduleId.toLongOrNull() ?: return null
        return ProjectPushItemDto(
            scheduledCandidateId = scheduledCandidateIdLong,
            scheduleId = scheduleIdLong,
            candidateId = candidateIdLong,
            paperId = paperIdLong,
            score = score,
        )
    }

    private fun Response<*>.requireSuccessOrThrow() {
        if (!isSuccessful) {
            error("Upload failed: HTTP ${code()} ${message()}")
        }
    }
}
