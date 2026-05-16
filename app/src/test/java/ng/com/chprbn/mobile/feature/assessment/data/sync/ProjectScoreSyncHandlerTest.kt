package ng.com.chprbn.mobile.feature.assessment.data.sync

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreEntity
import ng.com.chprbn.mobile.feature.assessment.data.repository.AssessmentScheduleSyncStatusUpdater
import ng.com.chprbn.mobile.feature.assessment.data.source.AssessmentSyncRemoteSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ProjectScoreSyncHandlerTest {

    private val dao = mockk<ProjectScoreDao>(relaxUnitFun = true) {
        coEvery { updateSyncMetadata(any(), any(), any(), any()) } returns 1
    }
    private val remote = mockk<AssessmentSyncRemoteSource>()
    private val statusUpdater = mockk<AssessmentScheduleSyncStatusUpdater>(relaxUnitFun = true)
    private val handler = ProjectScoreSyncHandler(dao, remote, statusUpdater)

    @Test
    fun `malformed key produces per-key Failure without touching dao or remote`() = runTest {
        val outcomes = handler.uploadBatch(listOf("only-one-part"))

        assertTrue(outcomes["only-one-part"] is SyncOutcome.Failure)
        coVerify(exactly = 0) { dao.getOne(any(), any()) }
        coVerify(exactly = 0) { remote.uploadProjectScoreBatch(any()) }
    }

    @Test
    fun `missing local row produces per-key Failure`() = runTest {
        coEvery { dao.getOne("s", "c") } returns null

        val outcomes = handler.uploadBatch(listOf("s/c"))

        assertTrue(outcomes["s/c"] is SyncOutcome.Failure)
        coVerify(exactly = 0) { remote.uploadProjectScoreBatch(any()) }
    }

    @Test
    fun `successful upload flips score to Synced and refreshes schedule once`() = runTest {
        coEvery { dao.getOne("s", "c") } returns scoreEntity()
        coEvery { remote.uploadProjectScoreBatch(any()) } returns mapOf(
            "s:c" to Result.success(Unit),
        )

        val outcomes = handler.uploadBatch(listOf("s/c"))

        assertEquals(SyncOutcome.Success, outcomes["s/c"])
        coVerify(exactly = 1) {
            dao.updateSyncMetadata(
                scheduleId = "s",
                candidateId = "c",
                syncStatus = SyncStatus.Synced.name,
                syncError = null,
            )
        }
        coVerify(exactly = 1) { statusUpdater.refresh("s") }
    }

    @Test
    fun `failed upload flips score to Failed with error message and refreshes schedule`() = runTest {
        coEvery { dao.getOne("s", "c") } returns scoreEntity()
        coEvery { remote.uploadProjectScoreBatch(any()) } returns mapOf(
            "s:c" to Result.failure(IOException("offline")),
        )

        val outcomes = handler.uploadBatch(listOf("s/c"))

        assertTrue(outcomes["s/c"] is SyncOutcome.Failure)
        assertEquals("offline", (outcomes["s/c"] as SyncOutcome.Failure).message)
        coVerify(exactly = 1) {
            dao.updateSyncMetadata(
                scheduleId = "s",
                candidateId = "c",
                syncStatus = SyncStatus.Failed.name,
                syncError = "offline",
            )
        }
        coVerify(exactly = 1) { statusUpdater.refresh("s") }
    }

    private fun scoreEntity() = ProjectScoreEntity(
        scheduleId = "s",
        candidateId = "c",
        score = 7.5,
        maxScore = 10,
        scoredAt = 0L,
        syncStatus = SyncStatus.Pending.name,
    )
}
