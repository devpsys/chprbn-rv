package ng.com.chprbn.mobile.feature.assessment.data.dto

import com.google.gson.annotations.SerializedName
import ng.com.chprbn.mobile.core.sync.dto.AssessorDto

/**
 * `POST /project/push-record` request body (confirmed live per
 * `docs/mobile-api-guide.html` §6). Now an **object** wrapping a
 * `projects` array + an [assessor] block — the older bare-array body is
 * gone. Missing the assessor yields:
 *
 * ```
 * { "status": false, "message": "Failed",
 *   "data": "Assessor is required. Include an \"assessor\" object with id and username." }
 * ```
 */
data class ProjectPushRequestDto(
    @SerializedName("projects") val projects: List<ProjectPushItemDto>,
    @SerializedName("assessor") val assessor: AssessorDto,
)

/**
 * One `projects[]` row inside [ProjectPushRequestDto] (and the
 * `projects` field of `PracticalPushRequestDto`, which shares this
 * shape). Upsert key: `(scheduled_candidate_id, candidate_id, paper_id, schedule_id)`.
 */
data class ProjectPushItemDto(
    @SerializedName("scheduled_candidate_id") val scheduledCandidateId: Long,
    @SerializedName("schedule_id") val scheduleId: Long,
    @SerializedName("candidate_id") val candidateId: Long,
    @SerializedName("paper_id") val paperId: Long,
    @SerializedName("score") val score: Double,
)
