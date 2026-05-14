package ng.com.chprbn.mobile.feature.exam.domain.model

import ng.com.chprbn.mobile.core.domain.model.SyncStatus

/**
 * Append-only candidate remark. Unlike attendance, multiple remarks per
 * `(candidateId, paperId)` coexist — each tap of "Add Remark" inserts a
 * fresh row with a client-generated UUID until the server assigns one.
 *
 * `paperId` is optional so the same row can represent a centre-wide
 * remark not tied to a specific paper.
 */
data class Remark(
    val id: String,
    val candidateId: String,
    val paperId: String? = null,
    val body: String,
    val severity: RemarkSeverity = RemarkSeverity.Info,
    val createdAt: Long,
    val syncStatus: SyncStatus = SyncStatus.Pending,
    val syncError: String? = null,
)
