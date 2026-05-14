package ng.com.chprbn.mobile.feature.assessment.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import ng.com.chprbn.mobile.core.sync.SyncJobEntity
import ng.com.chprbn.mobile.core.sync.SyncWorkScheduler
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreDao
import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectScoringRepositoryImplTest {

    private val projectScoreDao = mockk<ProjectScoreDao>(relaxUnitFun = true) {
        coEvery { upsert(any()) } returns 1L
    }
    private val syncJobDao = mockk<SyncJobDao>(relaxUnitFun = true) {
        coEvery { enqueue(any()) } returns 1L
    }
    private val workScheduler = mockk<SyncWorkScheduler>(relaxUnitFun = true)
    private val statusUpdater = mockk<AssessmentScheduleSyncStatusUpdater>(relaxUnitFun = true)

    private val repository = ProjectScoringRepositoryImpl(
        projectScoreDao, syncJobDao, workScheduler, statusUpdater,
    )

    @Test
    fun `recordProjectScore upserts, enqueues schedule and candidate keyed job, refreshes status, and schedules work`() = runTest {
        val captured = slot<SyncJobEntity>()
        coEvery { syncJobDao.enqueue(capture(captured)) } returns 1L

        val score = ProjectScore(
            scheduleId = "PE-2024",
            candidateId = "c1",
            score = 8.5,
            maxScore = 10,
            scoredAt = 1_700_000_000_000L,
            syncStatus = SyncStatus.Pending,
        )

        val result = repository.recordProjectScore(score)

        assertEquals(SaveResult.Success, result)
        coVerify(exactly = 1) { projectScoreDao.upsert(any()) }
        coVerify(exactly = 1) { statusUpdater.refresh("PE-2024") }
        coVerify(exactly = 1) { workScheduler.scheduleSyncWork() }

        val job = captured.captured
        assertEquals(SyncEntityType.ProjectScore.name, job.entityType)
        assertEquals("PE-2024/c1", job.entityKey)
        assertEquals(SyncStatus.Pending.name, job.status)
    }

    @Test
    fun `recordProjectScore reports failure when DAO throws`() = runTest {
        coEvery { projectScoreDao.upsert(any()) } throws RuntimeException("disk full")

        val result = repository.recordProjectScore(
            ProjectScore(
                scheduleId = "s", candidateId = "c",
                score = 5.0, maxScore = 10, scoredAt = 0L,
            ),
        )

        assertTrue(result is SaveResult.Error)
        assertEquals("disk full", (result as SaveResult.Error).message)
        coVerify(exactly = 0) { syncJobDao.enqueue(any()) }
        coVerify(exactly = 0) { workScheduler.scheduleSyncWork() }
    }
}
