package ng.com.chprbn.mobile.feature.exam.data.sync

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.exam.data.local.RemarkDao
import ng.com.chprbn.mobile.feature.exam.data.local.RemarkEntity
import ng.com.chprbn.mobile.feature.exam.data.source.ExamSyncRemoteSource
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class RemarkSyncHandlerTest {

    private val now = 1_700_000_000_000L
    private val clock = Clock { now }
    private val dao = mockk<RemarkDao>(relaxUnitFun = true) {
        coEvery { updateSyncMetadata(any(), any(), any(), any()) } returns 1
    }
    private val remote = mockk<ExamSyncRemoteSource>()
    private val handler = RemarkSyncHandler(dao, remote, clock)

    @Test
    fun `blank key produces per-key Failure without touching dao or remote`() = runTest {
        val outcomes = handler.uploadBatch(listOf(""))

        assertTrue(outcomes[""] is SyncOutcome.Failure)
        coVerify(exactly = 0) { dao.getById(any()) }
        coVerify(exactly = 0) { remote.uploadRemarkBatch(any()) }
    }

    @Test
    fun `missing local row produces per-key Failure`() = runTest {
        coEvery { dao.getById("r1") } returns null

        val outcomes = handler.uploadBatch(listOf("r1"))

        assertTrue(outcomes["r1"] is SyncOutcome.Failure)
        coVerify(exactly = 0) { remote.uploadRemarkBatch(any()) }
    }

    @Test
    fun `successful upload flips row to Synced with current clock time`() = runTest {
        coEvery { dao.getById("r1") } returns remark()
        coEvery { remote.uploadRemarkBatch(any()) } returns mapOf(
            "r1" to Result.success(Unit),
        )

        val outcomes = handler.uploadBatch(listOf("r1"))

        assertEquals(SyncOutcome.Success, outcomes["r1"])
        coVerify(exactly = 1) {
            dao.updateSyncMetadata(
                id = "r1",
                syncStatus = SyncStatus.Synced.name,
                syncError = null,
                lastSyncAttemptAt = now,
            )
        }
    }

    @Test
    fun `failed upload flips row to Failed with error message`() = runTest {
        coEvery { dao.getById("r1") } returns remark()
        coEvery { remote.uploadRemarkBatch(any()) } returns mapOf(
            "r1" to Result.failure(IOException("offline")),
        )

        val outcomes = handler.uploadBatch(listOf("r1"))

        assertTrue(outcomes["r1"] is SyncOutcome.Failure)
        assertEquals("offline", (outcomes["r1"] as SyncOutcome.Failure).message)
        coVerify(exactly = 1) {
            dao.updateSyncMetadata(
                id = "r1",
                syncStatus = SyncStatus.Failed.name,
                syncError = "offline",
                lastSyncAttemptAt = now,
            )
        }
    }

    private fun remark() = RemarkEntity(
        id = "r1",
        candidateId = "c1",
        paperId = "p1",
        body = "Arrived late",
        severity = RemarkSeverity.Info.name,
        createdAt = 0L,
        syncStatus = SyncStatus.Pending.name,
    )
}
