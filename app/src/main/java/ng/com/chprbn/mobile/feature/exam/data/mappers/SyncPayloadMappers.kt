package ng.com.chprbn.mobile.feature.exam.data.mappers

import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncRequestDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncRequestDto
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity

/**
 * Domain → request DTO for the sync engine. No `syncStatus` / `syncError`
 * cross the wire — those are mobile-only bookkeeping written to local
 * rows after a server response arrives.
 *
 * Wire-formatted enum values use lowercase snake_case (`"signed_in"`,
 * `"warning"`) per the API spec convention. The bridge functions here
 * are the authoritative mapping; the server contract for these strings
 * is captured in the DTO KDoc.
 */
internal fun Attendance.toSyncRequestDto(): AttendanceSyncRequestDto =
    AttendanceSyncRequestDto(
        paperId = paperId,
        candidateId = candidateId,
        status = status.toWireString(),
        markedAt = markedAt,
    )

internal fun Remark.toSyncRequestDto(): RemarkSyncRequestDto =
    RemarkSyncRequestDto(
        id = id,
        candidateId = candidateId,
        paperId = paperId,
        body = body,
        severity = severity.toWireString(),
        createdAt = createdAt,
    )

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
