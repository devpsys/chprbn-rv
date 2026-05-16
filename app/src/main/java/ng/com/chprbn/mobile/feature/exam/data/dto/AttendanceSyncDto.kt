package ng.com.chprbn.mobile.feature.exam.data.dto

import com.google.gson.annotations.SerializedName

/**
 * **SPECULATIVE.** Batched attendance upload payload. Mobile pushes N
 * `(paper_id, candidate_id)` rows in one HTTP request so the server pays
 * one auth check + one DB transaction for the batch instead of one per
 * row. Per-row idempotency on `(paper_id, candidate_id)` still applies
 * inside the batch — a duplicate row REPLACES.
 *
 * Backend contract: TBD (plan §12, C1).
 */
data class AttendanceSyncBatchRequestDto(
    @SerializedName("items") val items: List<AttendanceSyncItemDto>,
)

/**
 * One attendance row inside a batch.
 *
 * [clientId] is a stable string keyed off the row's composite identity
 * (`"$paperId:$candidateId"`). The server echoes it in the response row
 * so the client can map results back to local outbox rows; the server
 * MUST NOT use it for dedup — composite identity is the dedup key.
 */
data class AttendanceSyncItemDto(
    @SerializedName("client_id") val clientId: String,
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

/**
 * Batch response envelope. HTTP 200 even when individual rows fail; only
 * malformed batches return 4xx. Each result carries the [AttendanceSyncItemDto.clientId]
 * the client sent so a partial-success batch can be reconciled.
 */
data class AttendanceSyncBatchEnvelopeDto(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: AttendanceSyncBatchResultsDto? = null,
)

data class AttendanceSyncBatchResultsDto(
    @SerializedName("results") val results: List<AttendanceSyncResultDto>? = null,
)

data class AttendanceSyncResultDto(
    @SerializedName("client_id") val clientId: String? = null,
    @SerializedName("accepted") val accepted: Boolean = false,
    @SerializedName("server_id") val serverId: String? = null,
    @SerializedName("error") val error: String? = null,
)
