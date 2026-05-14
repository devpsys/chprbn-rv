package ng.com.chprbn.mobile.feature.exam.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import ng.com.chprbn.mobile.core.sync.SyncJobEntity
import ng.com.chprbn.mobile.core.sync.SyncWorkScheduler
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceDao
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.MarkAttendanceResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AttendanceRepositoryImplTest {

    private val now = 1_700_000_000_000L
    private val clock = Clock { now }
    private val attendanceDao = mockk<AttendanceDao>(relaxUnitFun = true) {
        coEvery { upsert(any()) } returns 1L
    }
    private val syncJobDao = mockk<SyncJobDao>(relaxUnitFun = true) {
        coEvery { enqueue(any()) } returns 1L
    }
    private val workScheduler = mockk<SyncWorkScheduler>(relaxUnitFun = true)

    private val repository = AttendanceRepositoryImpl(
        attendanceDao, syncJobDao, workScheduler, clock,
    )

    @Test
    fun `markAttendance upserts, enqueues a job with composite key, stamps time, and schedules work`() = runTest {
        val captured = slot<SyncJobEntity>()
        coEvery { syncJobDao.enqueue(capture(captured)) } returns 1L

        val result = repository.markAttendance("p1", "c1", AttendanceStatus.SignedIn)

        assertTrue(result is MarkAttendanceResult.Success)
        val saved = (result as MarkAttendanceResult.Success).attendance
        assertEquals(AttendanceStatus.SignedIn, saved.status)
        assertEquals(now, saved.markedAt)
        assertEquals(SyncStatus.Pending, saved.syncStatus)

        coVerify(exactly = 1) { attendanceDao.upsert(any()) }
        coVerify(exactly = 1) { workScheduler.scheduleSyncWork() }

        val job = captured.captured
        assertEquals(SyncEntityType.Attendance.name, job.entityType)
        assertEquals("p1/c1", job.entityKey)
        assertEquals(now, job.enqueuedAt)
        assertEquals(SyncStatus.Pending.name, job.status)
    }

    @Test
    fun `DAO throw is mapped to Error and no sync work scheduled`() = runTest {
        coEvery { attendanceDao.upsert(any()) } throws RuntimeException("disk full")

        val result = repository.markAttendance("p1", "c1", AttendanceStatus.SignedIn)

        assertTrue(result is MarkAttendanceResult.Error)
        assertEquals("disk full", (result as MarkAttendanceResult.Error).message)
        coVerify(exactly = 0) { syncJobDao.enqueue(any()) }
        coVerify(exactly = 0) { workScheduler.scheduleSyncWork() }
    }
}
