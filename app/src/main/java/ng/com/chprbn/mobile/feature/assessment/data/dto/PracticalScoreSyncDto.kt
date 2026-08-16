package ng.com.chprbn.mobile.feature.assessment.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Confirmed live per `docs/mobile-api-guide.html` §7.
 * `POST /practical/push-record` body is an **object** with `practicals`
 * and `projects` arrays — never a bare list, and never `{ "items": [...] }`.
 * Either array may be empty. This feature's practical handler always
 * sends `projects: []`; project scores go to `/project/push-record`.
 */
data class PracticalPushRequestDto(
    @SerializedName("practicals") val practicals: List<PracticalPushItemDto>,
    @SerializedName("projects") val projects: List<ProjectPushItemDto> = emptyList(),
)

/**
 * One practical row. Mobile field `question_id` is stored server-side as
 * `practical_question_id`. Upsert key:
 * `(scheduled_candidate_id, practical_question_id, paper_id, schedule_id)`.
 */
data class PracticalPushItemDto(
    @SerializedName("scheduled_candidate_id") val scheduledCandidateId: Long,
    @SerializedName("candidate_id") val candidateId: Long,
    @SerializedName("paper_id") val paperId: Long,
    @SerializedName("question_id") val questionId: Long,
    @SerializedName("schedule_id") val scheduleId: Long,
    @SerializedName("score") val score: Double,
)

/**
 * Shared envelope for both score-push endpoints. Success `data` is a list
 * of `scheduled_candidate_id` values — no per-row client_id results — so
 * the remote source treats the whole batch as one outcome.
 */
data class ScorePushResponseDto(
    @SerializedName(value = "status", alternate = ["success"]) val status: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<Long>? = null,
)
