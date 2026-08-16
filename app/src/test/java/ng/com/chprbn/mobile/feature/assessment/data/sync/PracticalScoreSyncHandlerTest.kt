package ng.com.chprbn.mobile.feature.assessment.data.sync

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreEntity
import ng.com.chprbn.mobile.feature.assessment.data.source.AssessmentSyncRemoteSource
import ng.com.chprbn.mobile.feature.exam.data.local.CandidateDao
import ng.com.chprbn.mobile.feature.exam.data.local.PaperCandidateAssignmentEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class PracticalScoreSyncHandlerTest {

    private val dao = mockk<PracticalScoreDao>(relaxUnitFun = true) {
        coEvery { updateSyncMetadata(any(), any(), any(), any(), any()) } returns 1
    }
    private val candidateDao = mockk<CandidateDao> {
        coEvery { getAssignment("s", "c") } returns PaperCandidateAssignmentEntity(
            paperId = "s",
            candidateId = "c",
            scheduledCandidateId = "501",
            scheduleId = "45",
        )
    }
    private val remote = mockk<AssessmentSyncRemoteSource>()
    private val handler = PracticalScoreSyncHandler(dao, candidateDao, remote)

    @Test
    fun `malformed key produces per-key Failure without touching dao or remote`() = runTest {
        val outcomes = handler.uploadBatch(listOf("malformed"))

        assertTrue(outcomes["malformed"] is SyncOutcome.Failure)
        coVerify(exactly = 0) { dao.getOne(any(), any(), any()) }
        coVerify(exactly = 0) { remote.uploadPracticalScoreBatch(any()) }
    }

    @Test
    fun `missing local row is Drop, not Failure — self-cleans the ghost job`() = runTest {
        coEvery { dao.getOne("s", "c", "q") } returns null

        val outcomes = handler.uploadBatch(listOf("s/c/q"))

        assertEquals(SyncOutcome.Drop, outcomes["s/c/q"])
        coVerify(exactly = 0) { remote.uploadPracticalScoreBatch(any()) }
    }

    @Test
    fun `missing assignment fails the row without an HTTP call`() = runTest {
        coEvery { dao.getOne("s", "c", "q") } returns scoreEntity()
        coEvery { candidateDao.getAssignment("s", "c") } returns null

        val outcomes = handler.uploadBatch(listOf("s/c/q"))

        assertTrue(outcomes["s/c/q"] is SyncOutcome.Failure)
        coVerify(exactly = 0) { remote.uploadPracticalScoreBatch(any()) }
    }

    @Test
    fun `successful upload flips score to Synced`() = runTest {
        coEvery { dao.getOne("s", "c", "q") } returns scoreEntity()
        coEvery { remote.uploadPracticalScoreBatch(any()) } returns mapOf(
            "s:c:q" to Result.success(Unit),
        )

        val outcomes = handler.uploadBatch(listOf("s/c/q"))

        assertEquals(SyncOutcome.Success, outcomes["s/c/q"])
        coVerify(exactly = 1) {
            dao.updateSyncMetadata(
                scheduleId = "s",
                candidateId = "c",
                questionId = "q",
                syncStatus = SyncStatus.Synced.name,
                syncError = null,
            )
        }
    }

    @Test
    fun `failed upload flips score to Failed with error message`() = runTest {
        coEvery { dao.getOne("s", "c", "q") } returns scoreEntity()
        coEvery { remote.uploadPracticalScoreBatch(any()) } returns mapOf(
            "s:c:q" to Result.failure(IOException("offline")),
        )

        val outcomes = handler.uploadBatch(listOf("s/c/q"))

        assertTrue(outcomes["s/c/q"] is SyncOutcome.Failure)
        assertEquals("offline", (outcomes["s/c/q"] as SyncOutcome.Failure).message)
        coVerify(exactly = 1) {
            dao.updateSyncMetadata(
                scheduleId = "s",
                candidateId = "c",
                questionId = "q",
                syncStatus = SyncStatus.Failed.name,
                syncError = "offline",
            )
        }
    }

    private fun scoreEntity(questionId: String = "q") = PracticalScoreEntity(
        scheduleId = "s",
        candidateId = "c",
        questionId = questionId,
        score = 7,
        scoredAt = 0L,
        syncStatus = SyncStatus.Pending.name,
    )
}
