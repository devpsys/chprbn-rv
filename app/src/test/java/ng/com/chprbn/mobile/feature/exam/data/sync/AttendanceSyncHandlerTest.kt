package ng.com.chprbn.mobile.feature.exam.data.sync

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceDao
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceEntity
import ng.com.chprbn.mobile.feature.exam.data.source.ExamSyncRemoteSource
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AttendanceSyncHandlerTest {

    private val now = 1_700_000_000_000L
    private val clock = Clock { now }
    private val dao = mockk<AttendanceDao>(relaxUnitFun = true) {
        coEvery { updateSyncMetadata(any(), any(), any(), any(), any()) } returns 1
    }
    private val remote = mockk<ExamSyncRemoteSource>()
    private val handler = AttendanceSyncHandler(dao, remote, clock)

    @Test
    fun `malformed key returns Failure without touching dao or remote`() = runTest {
        val outcome = handler.upload("malformed")

        assertTrue(outcome is SyncOutcome.Failure)
        coVerify(exactly = 0) { dao.getOne(any(), any()) }
        coVerify(exactly = 0) { remote.uploadAttendance(any()) }
    }

    @Test
    fun `missing local row returns Failure`() = runTest {
        coEvery { dao.getOne("p1", "c1") } returns null

        val outcome = handler.upload("p1/c1")

        assertTrue(outcome is SyncOutcome.Failure)
        coVerify(exactly = 0) { remote.uploadAttendance(any()) }
    }

    @Test
    fun `successful upload flips row to Synced with current clock time`() = runTest {
        coEvery { dao.getOne("p1", "c1") } returns attendance()
        coEvery { remote.uploadAttendance(any()) } returns Result.success(Unit)

        val outcome = handler.upload("p1/c1")

        assertEquals(SyncOutcome.Success, outcome)
        coVerify(exactly = 1) {
            dao.updateSyncMetadata(
                paperId = "p1",
                candidateId = "c1",
                syncStatus = SyncStatus.Synced.name,
                syncError = null,
                lastSyncAttemptAt = now,
            )
        }
    }

    @Test
    fun `failed upload flips row to Failed with error message`() = runTest {
        coEvery { dao.getOne("p1", "c1") } returns attendance()
        coEvery { remote.uploadAttendance(any()) } returns Result.failure(IOException("offline"))

        val outcome = handler.upload("p1/c1")

        assertTrue(outcome is SyncOutcome.Failure)
        assertEquals("offline", (outcome as SyncOutcome.Failure).message)
        coVerify(exactly = 1) {
            dao.updateSyncMetadata(
                paperId = "p1",
                candidateId = "c1",
                syncStatus = SyncStatus.Failed.name,
                syncError = "offline",
                lastSyncAttemptAt = now,
            )
        }
    }

    private fun attendance() = AttendanceEntity(
        paperId = "p1",
        candidateId = "c1",
        status = AttendanceStatus.SignedIn.name,
        markedAt = 0L,
        syncStatus = SyncStatus.Pending.name,
    )
}
