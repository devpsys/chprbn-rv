package ng.com.chprbn.mobile.feature.exam.data.dto

import com.google.gson.annotations.SerializedName

/**
 * **SPECULATIVE.** Batched remark upload payload. Idempotency is keyed
 * on the client-generated remark `id` (UUID v4) — a second send with the
 * same `id` REPLACES.
 *
 * Backend contract: TBD (plan §12, C1).
 */
data class RemarkSyncBatchRequestDto(
    @SerializedName("items") val items: List<RemarkSyncItemDto>,
)

/**
 * One remark row inside a batch.
 *
 * [clientId] mirrors [id] for remarks — the entity already carries its
 * own UUID, so we reuse it as the response-correlation key rather than
 * inventing a second identifier.
 */
data class RemarkSyncItemDto(
    @SerializedName("client_id") val clientId: String,
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

data class RemarkSyncBatchEnvelopeDto(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: RemarkSyncBatchResultsDto? = null,
)

data class RemarkSyncBatchResultsDto(
    @SerializedName("results") val results: List<RemarkSyncResultDto>? = null,
)

data class RemarkSyncResultDto(
    @SerializedName("client_id") val clientId: String? = null,
    @SerializedName("accepted") val accepted: Boolean = false,
    @SerializedName("server_id") val serverId: String? = null,
    @SerializedName("error") val error: String? = null,
)
