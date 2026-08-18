package ng.com.chprbn.mobile.feature.exam.data.dto

import com.google.gson.annotations.SerializedName
import ng.com.chprbn.mobile.core.sync.dto.AssessorDto

/**
 * `POST /attendance/push-record` request body (confirmed live per
 * `docs/mobile-api-guide.html` §5). The wire wrapper is an **object** with
 * an `attendances` array and an `assessor` block — the older bare-array
 * body is gone. Missing the `assessor` block returns:
 *
 * ```
 * { "status": false, "message": "Failed",
 *   "data": "Assessor is required. Include an \"assessor\" object with id and username." }
 * ```
 */
data class AttendancePushRequestDto(
    @SerializedName("attendances") val attendances: List<AttendanceSyncItemDto>,
    @SerializedName("assessor") val assessor: AssessorDto,
)

/**
 * One `attendances[]` row inside [AttendancePushRequestDto]. Must NOT
 * carry `client_id`, `status`, or `marked_at`; the docs call out that
 * sending those shapes causes a server-side failure
 * (`Attempt to read property "candidate_id" on array"`).
 *
 * The server's upsert key is `(scheduled_candidate_id, paper_id, year)`,
 * not `(paper_id, candidate_id)` — [scheduledCandidateId] is required
 * (sourced from `attendance/fetch-record` → `paper_candidates[].scheduled_candidate_id`,
 * resolved at sync time by `AttendanceSyncHandler`).
 */
data class AttendanceSyncItemDto(
    @SerializedName("scheduled_candidate_id") val scheduledCandidateId: Long,
    @SerializedName("schedule_id") val scheduleId: Long,
    @SerializedName("candidate_id") val candidateId: Long,
    @SerializedName("paper_id") val paperId: Long,
    /** 0 or 1. */
    @SerializedName("sign_in") val signIn: Int,
    /** 0 or 1. */
    @SerializedName("sign_out") val signOut: Int,
    /** Prefer a code from `GET attendance-remarks`, e.g. `"AE"`. Nullable. */
    @SerializedName("remark") val remark: String? = null,
    @SerializedName("year") val year: Int,
)

/**
 * The response carries no per-row results — just the whole envelope's
 * `status` plus the list of `candidate_id`s the server processed. There
 * is no way to tell which specific row failed in a partial rejection;
 * `ApiExamSyncRemoteSource` treats the whole batch as one outcome.
 */
data class AttendanceSyncResponseDto(
    @SerializedName(value = "status", alternate = ["success"]) val status: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<Long>? = null,
)
