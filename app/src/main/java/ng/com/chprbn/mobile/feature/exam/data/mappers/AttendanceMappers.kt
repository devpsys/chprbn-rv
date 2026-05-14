package ng.com.chprbn.mobile.feature.exam.data.mappers

import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceEntity
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance

internal fun AttendanceEntity.toDomain(): Attendance = Attendance(
    paperId = paperId,
    candidateId = candidateId,
    status = status.toAttendanceStatus(),
    markedAt = markedAt,
    syncStatus = syncStatus.toSyncStatus(),
    syncError = syncError,
)

internal fun Attendance.toEntity(): AttendanceEntity = AttendanceEntity(
    paperId = paperId,
    candidateId = candidateId,
    status = status.toDbValue(),
    markedAt = markedAt,
    syncStatus = syncStatus.toDbValue(),
    syncError = syncError,
    lastSyncAttemptAt = null,
)
