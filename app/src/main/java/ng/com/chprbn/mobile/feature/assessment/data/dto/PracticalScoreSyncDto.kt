package ng.com.chprbn.mobile.feature.assessment.data.dto

import com.google.gson.annotations.SerializedName

/**
 * **SPECULATIVE.** Per-row practical-score upload, modelled on
 * `VerifiedSyncRequestDto`. One row per HTTP request (matches the only
 * sync template the backend currently documents).
 *
 * Idempotency must be enforced on `(schedule_id, candidate_id, question_id)`
 * — calling the endpoint twice with the same composite key REPLACES.
 * Backend contract: TBD (plan §12, C1).
 */
data class PracticalScoreSyncRequestDto(
    @SerializedName("schedule_id") val scheduleId: String,
    @SerializedName("candidate_id") val candidateId: String,
    @SerializedName("question_id") val questionId: String,
    @SerializedName("score") val score: Int,
    /** Epoch millis (UTC). */
    @SerializedName("scored_at") val scoredAt: Long,
)

data class PracticalScoreSyncEnvelopeDto(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: PracticalScoreSyncResponseDto? = null,
)

data class PracticalScoreSyncResponseDto(
    @SerializedName("accepted") val accepted: Boolean = false,
    @SerializedName("server_id") val serverId: String? = null,
)
