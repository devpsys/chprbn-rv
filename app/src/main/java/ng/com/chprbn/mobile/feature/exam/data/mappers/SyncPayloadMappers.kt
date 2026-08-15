package ng.com.chprbn.mobile.feature.exam.data.mappers

import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncItemDto
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity

/**
 * Domain → batch-item DTO for the sync engine. No `syncStatus` / `syncError`
 * cross the wire — those are mobile-only bookkeeping written to local
 * rows after a server response arrives.
 *
 * Attendance's domain → wire mapping lives in `ApiExamSyncRemoteSource`
 * instead of here — `attendance/push-record`'s payload needs
 * `scheduledCandidateId`/`scheduleId`/`year`, which aren't stored on
 * [Attendance] itself (see `AttendanceSyncHandler`'s sync-time lookup).
 */
internal fun Remark.toSyncItemDto(): RemarkSyncItemDto =
    RemarkSyncItemDto(
        clientId = id,
        id = id,
        candidateId = candidateId,
        paperId = paperId,
        body = body,
        severity = severity.toWireString(),
        createdAt = createdAt,
    )

/** Stable client-side correlation key for an attendance row — internal bookkeeping only, never sent on the wire. */
internal fun attendanceClientId(paperId: String, candidateId: String): String =
    "$paperId:$candidateId"

private fun RemarkSeverity.toWireString(): String = when (this) {
    RemarkSeverity.Info -> "info"
    RemarkSeverity.Warning -> "warning"
    RemarkSeverity.Critical -> "critical"
}
