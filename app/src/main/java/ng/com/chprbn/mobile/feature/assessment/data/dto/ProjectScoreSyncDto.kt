package ng.com.chprbn.mobile.feature.assessment.data.dto

import com.google.gson.annotations.SerializedName

/**
 * **SPECULATIVE.** Batched project-score upload payload. Idempotency on
 * `(schedule_id, candidate_id)` still applies inside the batch.
 *
 * Backend contract: TBD (plan §12, C1).
 */
data class ProjectScoreSyncBatchRequestDto(
    @SerializedName("items") val items: List<ProjectScoreSyncItemDto>,
)

/**
 * One project-score row inside a batch.
 *
 * [clientId] is a stable string keyed off the composite identity
 * (`"$scheduleId:$candidateId"`).
 */
data class ProjectScoreSyncItemDto(
    @SerializedName("client_id") val clientId: String,
    @SerializedName("schedule_id") val scheduleId: String,
    @SerializedName("candidate_id") val candidateId: String,
    @SerializedName("score") val score: Double,
    @SerializedName("max_score") val maxScore: Int,
    /** Epoch millis (UTC). */
    @SerializedName("scored_at") val scoredAt: Long,
)

data class ProjectScoreSyncBatchEnvelopeDto(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ProjectScoreSyncBatchResultsDto? = null,
)

data class ProjectScoreSyncBatchResultsDto(
    @SerializedName("results") val results: List<ProjectScoreSyncResultDto>? = null,
)

data class ProjectScoreSyncResultDto(
    @SerializedName("client_id") val clientId: String? = null,
    @SerializedName("accepted") val accepted: Boolean = false,
    @SerializedName("server_id") val serverId: String? = null,
    @SerializedName("error") val error: String? = null,
)
