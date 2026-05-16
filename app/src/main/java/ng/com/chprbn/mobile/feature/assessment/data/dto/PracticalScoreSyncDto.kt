package ng.com.chprbn.mobile.feature.assessment.data.dto

import com.google.gson.annotations.SerializedName

/**
 * **SPECULATIVE.** Batched practical-score upload payload. Mobile pushes N
 * `(schedule_id, candidate_id, question_id)` rows in one HTTP request so
 * the server pays one auth check + one DB transaction per batch. Per-row
 * idempotency on the composite key still applies inside the batch —
 * a duplicate row REPLACES.
 *
 * Backend contract: TBD (plan §12, C1).
 */
data class PracticalScoreSyncBatchRequestDto(
    @SerializedName("items") val items: List<PracticalScoreSyncItemDto>,
)

/**
 * One practical-score row inside a batch.
 *
 * [clientId] is a stable string keyed off the composite identity
 * (`"$scheduleId:$candidateId:$questionId"`). The server echoes it so
 * the client can correlate results; it MUST NOT be used for dedup.
 */
data class PracticalScoreSyncItemDto(
    @SerializedName("client_id") val clientId: String,
    @SerializedName("schedule_id") val scheduleId: String,
    @SerializedName("candidate_id") val candidateId: String,
    @SerializedName("question_id") val questionId: String,
    @SerializedName("score") val score: Int,
    /** Epoch millis (UTC). */
    @SerializedName("scored_at") val scoredAt: Long,
)

data class PracticalScoreSyncBatchEnvelopeDto(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: PracticalScoreSyncBatchResultsDto? = null,
)

data class PracticalScoreSyncBatchResultsDto(
    @SerializedName("results") val results: List<PracticalScoreSyncResultDto>? = null,
)

data class PracticalScoreSyncResultDto(
    @SerializedName("client_id") val clientId: String? = null,
    @SerializedName("accepted") val accepted: Boolean = false,
    @SerializedName("server_id") val serverId: String? = null,
    @SerializedName("error") val error: String? = null,
)
