package ng.com.chprbn.mobile.feature.assessment.data.dto

import com.google.gson.annotations.SerializedName

/**
 * **SPECULATIVE.** Per-candidate project-score upload. Idempotency
 * enforced on `(schedule_id, candidate_id)`. Backend contract: TBD
 * (plan §12, C1).
 */
data class ProjectScoreSyncRequestDto(
    @SerializedName("schedule_id") val scheduleId: String,
    @SerializedName("candidate_id") val candidateId: String,
    @SerializedName("score") val score: Double,
    @SerializedName("max_score") val maxScore: Int,
    /** Epoch millis (UTC). */
    @SerializedName("scored_at") val scoredAt: Long,
)

data class ProjectScoreSyncEnvelopeDto(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ProjectScoreSyncResponseDto? = null,
)

data class ProjectScoreSyncResponseDto(
    @SerializedName("accepted") val accepted: Boolean = false,
    @SerializedName("server_id") val serverId: String? = null,
)
