package ng.com.chprbn.mobile.feature.exam.domain.repository

import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.MarkAttendanceResult

/**
 * Single write surface for attendance. The repository:
 * - upserts the local row (PK `(paperId, candidateId)`),
 * - enqueues a `SyncJobEntity` keyed by the same pair,
 * - asks the [ng.com.chprbn.mobile.core.sync.SyncWorkScheduler] to flush.
 */
interface AttendanceRepository {

    suspend fun markAttendance(
        paperId: String,
        candidateId: String,
        status: AttendanceStatus,
    ): MarkAttendanceResult
}
