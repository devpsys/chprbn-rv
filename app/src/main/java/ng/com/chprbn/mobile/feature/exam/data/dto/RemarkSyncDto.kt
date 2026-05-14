package ng.com.chprbn.mobile.feature.exam.data.dto

import com.google.gson.annotations.SerializedName

/**
 * **SPECULATIVE.** Append-only remark upload. The client-generated
 * [id] (UUID) is sent so the server can detect retries and dedupe;
 * a second POST with the same id REPLACES rather than appends.
 * Backend contract: TBD (plan §12, C1).
 */
data class RemarkSyncRequestDto(
    @SerializedName("id") val id: String,
    @SerializedName("candidate_id") val candidateId: String,
    @SerializedName("paper_id") val paperId: String? = null,
    @SerializedName("body") val body: String,
    /**
     * Wire string for [ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity]
     * — sent as `"info"` / `"warning"` / `"critical"`.
     */
    @SerializedName("severity") val severity: String,
    @SerializedName("created_at") val createdAt: Long,
)

data class RemarkSyncEnvelopeDto(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: RemarkSyncResponseDto? = null,
)

data class RemarkSyncResponseDto(
    @SerializedName("accepted") val accepted: Boolean = false,
    @SerializedName("server_id") val serverId: String? = null,
)
