package ng.com.chprbn.mobile.feature.exam.data.mappers

import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncItemDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncItemDto
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity

/**
 * Domain → batch-item DTO for the sync engine. No `syncStatus` / `syncError`
 * cross the wire — those are mobile-only bookkeeping written to local
 * rows after a server response arrives.
 *
 * `clientId` is synthesised from the row's composite identity so retries
 * carry the same id and partial-success responses can be reconciled.
 * Wire-formatted enum values use lowercase snake_case (`"signed_in"`,
 * `"warning"`).
 */
internal fun Attendance.toSyncItemDto(): AttendanceSyncItemDto =
    AttendanceSyncItemDto(
        clientId = attendanceClientId(paperId, candidateId),
        paperId = paperId,
        candidateId = candidateId,
        status = status.toWireString(),
        markedAt = markedAt,
    )

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

/** Stable client-side correlation key for an attendance row. */
internal fun attendanceClientId(paperId: String, candidateId: String): String =
    "$paperId:$candidateId"

private fun AttendanceStatus.toWireString(): String = when (this) {
    AttendanceStatus.SignedIn -> "signed_in"
    AttendanceStatus.SignedOut -> "signed_out"
    AttendanceStatus.Flagged -> "flagged"
}

private fun RemarkSeverity.toWireString(): String = when (this) {
    RemarkSeverity.Info -> "info"
    RemarkSeverity.Warning -> "warning"
    RemarkSeverity.Critical -> "critical"
}
