package ng.com.chprbn.mobile.feature.assessment.data.sync

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreEntity
import ng.com.chprbn.mobile.feature.assessment.data.repository.AssessmentScheduleSyncStatusUpdater
import ng.com.chprbn.mobile.feature.assessment.data.source.AssessmentSyncRemoteSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class PracticalScoreSyncHandlerTest {

    private val dao = mockk<PracticalScoreDao>(relaxUnitFun = true) {
        coEvery { updateSyncMetadata(any(), any(), any(), any(), any()) } returns 1
    }
    private val remote = mockk<AssessmentSyncRemoteSource>()
    private val statusUpdater = mockk<AssessmentScheduleSyncStatusUpdater>(relaxUnitFun = true)
    private val handler = PracticalScoreSyncHandler(dao, remote, statusUpdater)

    @Test
    fun `malformed key produces per-key Failure without touching dao or remote`() = runTest {
        val outcomes = handler.uploadBatch(listOf("malformed"))

        assertTrue(outcomes["malformed"] is SyncOutcome.Failure)
        coVerify(exactly = 0) { dao.getOne(any(), any(), any()) }
        coVerify(exactly = 0) { remote.uploadPracticalScoreBatch(any()) }
    }

    @Test
    fun `missing local row produces per-key Failure`() = runTest {
        coEvery { dao.getOne("s", "c", "q") } returns null

        val outcomes = handler.uploadBatch(listOf("s/c/q"))

        assertTrue(outcomes["s/c/q"] is SyncOutcome.Failure)
        coVerify(exactly = 0) { remote.uploadPracticalScoreBatch(any()) }
    }

    @Test
    fun `successful upload flips score to Synced and refreshes schedule once`() = runTest {
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
        coVerify(exactly = 1) { statusUpdater.refresh("s") }
    }

    @Test
    fun `failed upload flips score to Failed with error message and refreshes schedule`() = runTest {
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
        coVerify(exactly = 1) { statusUpdater.refresh("s") }
    }

    @Test
    fun `batch with two rows from same schedule refreshes status updater exactly once`() = runTest {
        coEvery { dao.getOne("s", "c", "q1") } returns scoreEntity(questionId = "q1")
        coEvery { dao.getOne("s", "c", "q2") } returns scoreEntity(questionId = "q2")
        coEvery { remote.uploadPracticalScoreBatch(any()) } returns mapOf(
            "s:c:q1" to Result.success(Unit),
            "s:c:q2" to Result.success(Unit),
        )

        handler.uploadBatch(listOf("s/c/q1", "s/c/q2"))

        coVerify(exactly = 1) { statusUpdater.refresh("s") }
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
