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
import ng.com.chprbn.mobile.feature.exam.data.local.CandidateDao
import ng.com.chprbn.mobile.feature.exam.data.local.CenterDao
import ng.com.chprbn.mobile.feature.exam.data.local.CenterEntity
import ng.com.chprbn.mobile.feature.exam.data.local.PaperCandidateAssignmentEntity
import ng.com.chprbn.mobile.feature.exam.data.local.RemarkDao
import ng.com.chprbn.mobile.feature.exam.data.local.RemarkEntity
import ng.com.chprbn.mobile.feature.exam.data.source.AttendanceUploadRow
import ng.com.chprbn.mobile.feature.exam.data.source.ExamSyncRemoteSource
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity
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
    private val candidateDao = mockk<CandidateDao>()
    private val centerDao = mockk<CenterDao> {
        coEvery { getFirst() } returns center()
    }
    private val remarkDao = mockk<RemarkDao> {
        coEvery { getOne(any()) } returns null
    }
    private val remote = mockk<ExamSyncRemoteSource>()
    private val handler = AttendanceSyncHandler(dao, candidateDao, centerDao, remarkDao, remote, clock)

    @Test
    fun `malformed key produces per-key Failure without touching dao or remote`() = runTest {
        val outcomes = handler.uploadBatch(listOf("malformed"))

        assertTrue(outcomes["malformed"] is SyncOutcome.Failure)
        coVerify(exactly = 0) { dao.getOne(any(), any()) }
        coVerify(exactly = 0) { remote.uploadAttendanceBatch(any()) }
    }

    @Test
    fun `missing local row is Drop, not Failure — self-cleans the ghost job`() = runTest {
        coEvery { dao.getOne("p1", "c1") } returns null

        val outcomes = handler.uploadBatch(listOf("p1/c1"))

        assertEquals(SyncOutcome.Drop, outcomes["p1/c1"])
        coVerify(exactly = 0) { remote.uploadAttendanceBatch(any()) }
    }

    @Test
    fun `missing assignment fails with a self-heal message instead of uploading`() = runTest {
        coEvery { dao.getOne("p1", "c1") } returns attendance(paper = "p1", candidate = "c1")
        coEvery { candidateDao.getAssignment("p1", "c1") } returns null

        val outcomes = handler.uploadBatch(listOf("p1/c1"))

        assertTrue(outcomes["p1/c1"] is SyncOutcome.Failure)
        assertTrue(
            (outcomes["p1/c1"] as SyncOutcome.Failure).message.contains("re-download today's dossier"),
        )
        coVerify(exactly = 0) { remote.uploadAttendanceBatch(any()) }
    }

    @Test
    fun `blank scheduledCandidateId on the assignment is treated as missing`() = runTest {
        coEvery { dao.getOne("p1", "c1") } returns attendance(paper = "p1", candidate = "c1")
        coEvery { candidateDao.getAssignment("p1", "c1") } returns assignment(
            paperId = "p1", candidateId = "c1", scheduledCandidateId = "", scheduleId = "45",
        )

        val outcomes = handler.uploadBatch(listOf("p1/c1"))

        assertTrue(outcomes["p1/c1"] is SyncOutcome.Failure)
        coVerify(exactly = 0) { remote.uploadAttendanceBatch(any()) }
    }

    @Test
    fun `missing center year fails with a self-heal message instead of uploading`() = runTest {
        coEvery { dao.getOne("p1", "c1") } returns attendance(paper = "p1", candidate = "c1")
        coEvery { candidateDao.getAssignment("p1", "c1") } returns assignment(
            paperId = "p1", candidateId = "c1",
        )
        coEvery { centerDao.getFirst() } returns center(year = null)

        val outcomes = handler.uploadBatch(listOf("p1/c1"))

        assertTrue(outcomes["p1/c1"] is SyncOutcome.Failure)
        coVerify(exactly = 0) { remote.uploadAttendanceBatch(any()) }
    }

    @Test
    fun `successful batch flips rows to Synced with current clock time`() = runTest {
        coEvery { dao.getOne("p1", "c1") } returns attendance(paper = "p1", candidate = "c1")
        coEvery { dao.getOne("p1", "c2") } returns attendance(paper = "p1", candidate = "c2")
        coEvery { candidateDao.getAssignment("p1", "c1") } returns assignment(paperId = "p1", candidateId = "c1")
        coEvery { candidateDao.getAssignment("p1", "c2") } returns assignment(
            paperId = "p1", candidateId = "c2", scheduledCandidateId = "502",
        )
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
        coEvery { candidateDao.getAssignment("p1", "c1") } returns assignment(paperId = "p1", candidateId = "c1")
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
    fun `single-call batch sends one HTTP call carrying resolved scheduledCandidateId, scheduleId, and year`() =
        runTest {
            coEvery { dao.getOne("p1", "c1") } returns attendance(paper = "p1", candidate = "c1")
            coEvery { dao.getOne("p1", "c2") } returns attendance(paper = "p1", candidate = "c2")
            coEvery { candidateDao.getAssignment("p1", "c1") } returns assignment(paperId = "p1", candidateId = "c1")
            coEvery { candidateDao.getAssignment("p1", "c2") } returns assignment(
                paperId = "p1", candidateId = "c2", scheduledCandidateId = "502",
            )
            val captured = slot<List<AttendanceUploadRow>>()
            coEvery { remote.uploadAttendanceBatch(capture(captured)) } returns mapOf(
                "p1:c1" to Result.success(Unit),
                "p1:c2" to Result.success(Unit),
            )

            handler.uploadBatch(listOf("p1/c1", "p1/c2"))

            coVerify(exactly = 1) { remote.uploadAttendanceBatch(any()) }
            assertEquals(2, captured.captured.size)
            val row1 = captured.captured.single { it.candidateId == "c1" }
            assertEquals("501", row1.scheduledCandidateId)
            assertEquals("45", row1.scheduleId)
            assertEquals(2026, row1.year)
        }

    @Test
    fun `candidate with no remark on file uploads with an empty remark string, not null`() = runTest {
        coEvery { dao.getOne("p1", "c1") } returns attendance(paper = "p1", candidate = "c1")
        coEvery { candidateDao.getAssignment("p1", "c1") } returns assignment(paperId = "p1", candidateId = "c1")
        coEvery { remarkDao.getOne("c1") } returns null
        val captured = slot<List<AttendanceUploadRow>>()
        coEvery { remote.uploadAttendanceBatch(capture(captured)) } returns mapOf(
            "p1:c1" to Result.success(Unit),
        )

        handler.uploadBatch(listOf("p1/c1"))

        assertEquals("", captured.captured.single().remark)
    }

    @Test
    fun `candidate with an on-file remark uploads its code`() = runTest {
        coEvery { dao.getOne("p1", "c1") } returns attendance(paper = "p1", candidate = "c1")
        coEvery { candidateDao.getAssignment("p1", "c1") } returns assignment(paperId = "p1", candidateId = "c1")
        coEvery { remarkDao.getOne("c1") } returns remark(candidateId = "c1", code = "AE")
        val captured = slot<List<AttendanceUploadRow>>()
        coEvery { remote.uploadAttendanceBatch(capture(captured)) } returns mapOf(
            "p1:c1" to Result.success(Unit),
        )

        handler.uploadBatch(listOf("p1/c1"))

        assertEquals("AE", captured.captured.single().remark)
    }

    @Test
    fun `mixed batch — valid rows uploaded, ghost rows still get Drop`() = runTest {
        coEvery { dao.getOne("p1", "c1") } returns attendance(paper = "p1", candidate = "c1")
        coEvery { dao.getOne("p1", "missing") } returns null
        coEvery { candidateDao.getAssignment("p1", "c1") } returns assignment(paperId = "p1", candidateId = "c1")
        coEvery { remote.uploadAttendanceBatch(any()) } returns mapOf(
            "p1:c1" to Result.success(Unit),
        )

        val outcomes = handler.uploadBatch(listOf("p1/c1", "p1/missing"))

        assertEquals(SyncOutcome.Success, outcomes["p1/c1"])
        assertEquals(SyncOutcome.Drop, outcomes["p1/missing"])
    }

    private fun attendance(paper: String, candidate: String) = AttendanceEntity(
        paperId = paper,
        candidateId = candidate,
        status = AttendanceStatus.SignedIn.name,
        markedAt = 0L,
        syncStatus = SyncStatus.Pending.name,
    )

    private fun assignment(
        paperId: String,
        candidateId: String,
        scheduledCandidateId: String = "501",
        scheduleId: String = "45",
    ) = PaperCandidateAssignmentEntity(
        paperId = paperId,
        candidateId = candidateId,
        scheduledCandidateId = scheduledCandidateId,
        scheduleId = scheduleId,
    )

    private fun remark(candidateId: String, code: String) = RemarkEntity(
        candidateId = candidateId,
        id = "r-$candidateId",
        code = code,
        body = "x",
        severity = RemarkSeverity.Info.name,
        createdAt = 0L,
        syncStatus = SyncStatus.Pending.name,
    )

    private fun center(year: Int? = 2026) = CenterEntity(
        id = "C-1",
        name = "Lagos Centre",
        code = "LAG-001",
        location = "10 Marina Rd",
        year = year,
    )
}
