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
    fun `malformed key returns Failure without touching dao or remote`() = runTest {
        val outcome = handler.upload("malformed")

        assertTrue(outcome is SyncOutcome.Failure)
        coVerify(exactly = 0) { dao.getOne(any(), any(), any()) }
        coVerify(exactly = 0) { remote.uploadPracticalScore(any()) }
    }

    @Test
    fun `missing local row returns Failure`() = runTest {
        coEvery { dao.getOne("s", "c", "q") } returns null

        val outcome = handler.upload("s/c/q")

        assertTrue(outcome is SyncOutcome.Failure)
        coVerify(exactly = 0) { remote.uploadPracticalScore(any()) }
    }

    @Test
    fun `successful upload flips score to Synced and refreshes schedule`() = runTest {
        coEvery { dao.getOne("s", "c", "q") } returns scoreEntity()
        coEvery { remote.uploadPracticalScore(any()) } returns Result.success(Unit)

        val outcome = handler.upload("s/c/q")

        assertEquals(SyncOutcome.Success, outcome)
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
        coEvery { remote.uploadPracticalScore(any()) } returns
            Result.failure(IOException("offline"))

        val outcome = handler.upload("s/c/q")

        assertTrue(outcome is SyncOutcome.Failure)
        assertEquals("offline", (outcome as SyncOutcome.Failure).message)
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

    private fun scoreEntity() = PracticalScoreEntity(
        scheduleId = "s",
        candidateId = "c",
        questionId = "q",
        score = 7,
        scoredAt = 0L,
        syncStatus = SyncStatus.Pending.name,
    )
}
