package ng.com.chprbn.mobile.feature.assessment.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Confirmed live per `docs/mobile-api-guide.html` §6.
 * `POST /project/push-record` body is a **top-level JSON array** of these.
 * Upsert key: `(scheduled_candidate_id, candidate_id, paper_id, schedule_id)`.
 */
data class ProjectPushItemDto(
    @SerializedName("scheduled_candidate_id") val scheduledCandidateId: Long,
    @SerializedName("schedule_id") val scheduleId: Long,
    @SerializedName("candidate_id") val candidateId: Long,
    @SerializedName("paper_id") val paperId: Long,
    @SerializedName("score") val score: Double,
)
