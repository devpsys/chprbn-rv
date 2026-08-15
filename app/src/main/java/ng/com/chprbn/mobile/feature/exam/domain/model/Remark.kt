package ng.com.chprbn.mobile.feature.exam.domain.model

import ng.com.chprbn.mobile.core.domain.model.SyncStatus

/**
 * One remark per candidate — [candidateId] is the storage key, so a new
 * "Add Remark" replaces whatever was already on file rather than
 * appending. Each replace still gets a fresh client-generated UUID [id]
 * until the server assigns one.
 *
 * `paperId` is optional so the remark can represent a centre-wide note
 * not tied to a specific paper. [code] is the fixed CHPRBN remark code
 * (e.g. `"AE"`) the officer picked — feeds `attendance/push-record`'s
 * `remark` field; [body] is the human-readable label shown in the UI and
 * sent to the (speculative) `attendance-remarks` write endpoint.
 */
data class Remark(
    val id: String,
    val candidateId: String,
    val paperId: String? = null,
    val code: String = "",
    val body: String,
    val severity: RemarkSeverity = RemarkSeverity.Info,
    val createdAt: Long,
    val syncStatus: SyncStatus = SyncStatus.Pending,
    val syncError: String? = null,
)
