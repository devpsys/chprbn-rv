package ng.com.chprbn.mobile.feature.exam.domain.model

import ng.com.chprbn.mobile.core.domain.model.SyncStatus

/**
 * One attendance row per `(paperId, candidateId)`. Local-write; re-marking
 * a candidate REPLACES the row (no append-only history). Carries the
 * same `syncStatus` / `syncError` shape as score rows so the shared
 * [ng.com.chprbn.mobile.core.sync.SyncWorker] dispatches it uniformly.
 */
data class Attendance(
    val paperId: String,
    val candidateId: String,
    val status: AttendanceStatus,
    val markedAt: Long,
    val syncStatus: SyncStatus = SyncStatus.Pending,
    val syncError: String? = null,
)
