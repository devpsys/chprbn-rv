package ng.com.chprbn.mobile.feature.exam.data.dto

import com.google.gson.annotations.SerializedName

/**
 * **SPECULATIVE.** Per-row attendance upload, modelled on
 * `VerifiedSyncRequestDto`. One row per HTTP request (matches the only
 * sync template the backend currently documents).
 *
 * Idempotency must be enforced on `(paper_id, candidate_id)` — calling
 * the endpoint twice with the same composite key REPLACES. Backend
 * contract: TBD (plan §12, C1).
 */
data class AttendanceSyncRequestDto(
    @SerializedName("paper_id") val paperId: String,
    @SerializedName("candidate_id") val candidateId: String,
    /**
     * Wire string for [ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus]
     * — sent as `"signed_in"` / `"signed_out"` / `"flagged"` so the server
     * doesn't need to know about the client's Kotlin enum naming.
     */
    @SerializedName("status") val status: String,
    /** Epoch millis (UTC). */
    @SerializedName("marked_at") val markedAt: Long,
)

data class AttendanceSyncEnvelopeDto(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: AttendanceSyncResponseDto? = null,
)

data class AttendanceSyncResponseDto(
    @SerializedName("accepted") val accepted: Boolean = false,
    @SerializedName("server_id") val serverId: String? = null,
)
