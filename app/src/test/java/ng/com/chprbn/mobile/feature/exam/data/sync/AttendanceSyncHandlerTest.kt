package ng.com.chprbn.mobile.feature.exam.data.sync

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceDao
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceEntity
import ng.com.chprbn.mobile.feature.exam.data.source.ExamSyncRemoteSource
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
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
    fun `malformed key produces per-key Failure without touching dao or remote`() = runTest {
        val outcomes = handler.uploadBatch(listOf("malformed"))

        assertTrue(outcomes["malformed"] is SyncOutcome.Failure)
        coVerify(exactly = 0) { dao.getOne(any(), any()) }
        coVerify(exactly = 0) { remote.uploadAttendanceBatch(any()) }
    }

    @Test
    fun `missing local row produces per-key Failure`() = runTest {
        coEvery { dao.getOne("p1", "c1") } returns null

        val outcomes = handler.uploadBatch(listOf("p1/c1"))

        assertTrue(outcomes["p1/c1"] is SyncOutcome.Failure)
        coVerify(exactly = 0) { remote.uploadAttendanceBatch(any()) }
    }

    @Test
    fun `successful batch flips rows to Synced with current clock time`() = runTest {
        coEvery { dao.getOne("p1", "c1") } returns attendance(paper = "p1", candidate = "c1")
        coEvery { dao.getOne("p1", "c2") } returns attendance(paper = "p1", candidate = "c2")
        coEvery { remote.uploadAttendanceBatch(any()) } returns mapOf(
            "p1:c1" to Result.success(Unit),
            "p1:c2" to Result.success(Unit),
        )

        val outcomes = handler.uploadBatch(listOf("p1/c1", "p1/c2"))

        assertEquals(SyncOutcome.Success, outcomes["p1/c1"])
        assertEquals(SyncOutcome.Success, outcomes["p1/c2"])
        coVerify(exactly = 1) {
            dao.updateSyncMetadata(
                paperId = "p1", candidateId = "c1",
                syncStatus = SyncStatus.Synced.name,
                syncError = null,
                lastSyncAttemptAt = now,
            )
        }
        coVerify(exactly = 1) {
            dao.updateSyncMetadata(
                paperId = "p1", candidateId = "c2",
                syncStatus = SyncStatus.Synced.name,
                syncError = null,
                lastSyncAttemptAt = now,
            )
        }
    }

    @Test
    fun `per-row failure flips that row to Failed with error message`() = runTest {
        coEvery { dao.getOne("p1", "c1") } returns attendance(paper = "p1", candidate = "c1")
        coEvery { remote.uploadAttendanceBatch(any()) } returns mapOf(
            "p1:c1" to Result.failure(IOException("offline")),
        )

        val outcomes = handler.uploadBatch(listOf("p1/c1"))

        assertTrue(outcomes["p1/c1"] is SyncOutcome.Failure)
        assertEquals("offline", (outcomes["p1/c1"] as SyncOutcome.Failure).message)
        coVerify(exactly = 1) {
            dao.updateSyncMetadata(
                paperId = "p1", candidateId = "c1",
                syncStatus = SyncStatus.Failed.name,
                syncError = "offline",
                lastSyncAttemptAt = now,
            )
        }
    }

    @Test
    fun `single-call batch sends one HTTP call covering every valid row`() = runTest {
        coEvery { dao.getOne("p1", "c1") } returns attendance(paper = "p1", candidate = "c1")
        coEvery { dao.getOne("p1", "c2") } returns attendance(paper = "p1", candidate = "c2")
        val captured = slot<List<Attendance>>()
        coEvery { remote.uploadAttendanceBatch(capture(captured)) } returns mapOf(
            "p1:c1" to Result.success(Unit),
            "p1:c2" to Result.success(Unit),
        )

        handler.uploadBatch(listOf("p1/c1", "p1/c2"))

        coVerify(exactly = 1) { remote.uploadAttendanceBatch(any()) }
        assertEquals(2, captured.captured.size)
    }

    @Test
    fun `mixed batch — valid rows uploaded, missing keys still get Failure`() = runTest {
        coEvery { dao.getOne("p1", "c1") } returns attendance(paper = "p1", candidate = "c1")
        coEvery { dao.getOne("p1", "missing") } returns null
        coEvery { remote.uploadAttendanceBatch(any()) } returns mapOf(
            "p1:c1" to Result.success(Unit),
        )

        val outcomes = handler.uploadBatch(listOf("p1/c1", "p1/missing"))

        assertEquals(SyncOutcome.Success, outcomes["p1/c1"])
        assertTrue(outcomes["p1/missing"] is SyncOutcome.Failure)
    }

    private fun attendance(paper: String, candidate: String) = AttendanceEntity(
        paperId = paper,
        candidateId = candidate,
        status = AttendanceStatus.SignedIn.name,
        markedAt = 0L,
        syncStatus = SyncStatus.Pending.name,
    )
}
