package ng.com.chprbn.mobile.feature.exam.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import ng.com.chprbn.mobile.core.sync.SyncJobEntity
import ng.com.chprbn.mobile.core.sync.SyncWorkScheduler
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceDao
import ng.com.chprbn.mobile.feature.exam.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.exam.data.mappers.toEntity
import ng.com.chprbn.mobile.feature.exam.data.sync.AttendanceKey
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.MarkAttendanceResult
import ng.com.chprbn.mobile.feature.exam.domain.repository.AttendanceRepository
import javax.inject.Inject

/**
 * Single write path for attendance. Every call:
 *
 * 1. Stamps `markedAt` from the [Clock] and builds an [Attendance] at
 *    `syncStatus = Pending`.
 * 2. Upserts the row (composite PK = REPLACE).
 * 3. Enqueues a `SyncJobEntity` keyed by `paperId/candidateId`.
 * 4. Asks [SyncWorkScheduler] to flush (idempotent — KEEP policy).
 */
class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val syncJobDao: SyncJobDao,
    private val workScheduler: SyncWorkScheduler,
    private val clock: Clock,
) : AttendanceRepository {

    override suspend fun markAttendance(
        paperId: String,
        candidateId: String,
        status: AttendanceStatus,
    ): MarkAttendanceResult = withContext(Dispatchers.IO) {
        val markedAt = clock.nowMillis()
        val attendance = Attendance(
            paperId = paperId,
            candidateId = candidateId,
            status = status,
            markedAt = markedAt,
            syncStatus = SyncStatus.Pending,
        )
        // Enqueue first, then upsert. attendance rows live in ExamDatabase and
        // sync jobs in SyncDatabase, so we cannot share a Room transaction.
        // Two failure modes:
        //   - enqueue throws → no local ghost row, UI shows error.
        //   - upsert throws AFTER a successful enqueue → sync job points at a
        //     non-existent local row; the handler treats that as a self-clean
        //     signal (deletes the ghost job — see AttendanceSyncHandler).
        // Doing it in this order keeps user-visible state consistent with the
        // returned Result.
        try {
            syncJobDao.enqueue(
                SyncJobEntity(
                    entityType = SyncEntityType.Attendance.name,
                    entityKey = AttendanceKey.encode(paperId, candidateId),
                    enqueuedAt = markedAt,
                    status = SyncStatus.Pending.name,
                ),
            )
            attendanceDao.upsert(attendance.toEntity())
            workScheduler.scheduleSyncWork()
            MarkAttendanceResult.Success(attendance)
        } catch (t: Throwable) {
            MarkAttendanceResult.Error(t.message ?: "Unable to save attendance.")
        }
    }

    override suspend fun getAttendance(paperId: String, candidateId: String): Attendance? =
        withContext(Dispatchers.IO) {
            attendanceDao.getOne(paperId, candidateId)?.toDomain()
        }
}
