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
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalSectionDao
import ng.com.chprbn.mobile.feature.assessment.data.local.SectionQuestionDao
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `recordScore` is the most consequential write path in the assessment
 * feature — every stepper tap goes through it. We assert the full chain:
 * upsert, enqueue, schedule-status refresh, work scheduling.
 */
class PracticalScoringRepositoryImplTest {

    private val sectionDao = mockk<PracticalSectionDao>()
    private val questionDao = mockk<SectionQuestionDao>()
    private val practicalScoreDao = mockk<PracticalScoreDao>(relaxUnitFun = true) {
        coEvery { upsert(any()) } returns 1L
    }
    private val syncJobDao = mockk<SyncJobDao>(relaxUnitFun = true) {
        coEvery { enqueue(any()) } returns 1L
    }
    private val workScheduler = mockk<SyncWorkScheduler>(relaxUnitFun = true)
    private val statusUpdater = mockk<AssessmentScheduleSyncStatusUpdater>(relaxUnitFun = true)

    private val repository = PracticalScoringRepositoryImpl(
        sectionDao, questionDao, practicalScoreDao, syncJobDao, workScheduler, statusUpdater,
    )

    @Test
    fun `recordScore upserts, enqueues a sync job with composite key, refreshes status, and schedules work`() = runTest {
        val captured = slot<SyncJobEntity>()
        coEvery { syncJobDao.enqueue(capture(captured)) } returns 1L

        val score = PracticalScore(
            scheduleId = "PE-2024",
            candidateId = "c1",
            questionId = "q1",
            score = 7,
            scoredAt = 1_700_000_000_000L,
            syncStatus = SyncStatus.Pending,
        )

        val result = repository.recordScore(score)

        assertEquals(SaveResult.Success, result)

        coVerify(exactly = 1) { practicalScoreDao.upsert(any()) }
        coVerify(exactly = 1) { statusUpdater.refresh("PE-2024") }
        coVerify(exactly = 1) { workScheduler.scheduleSyncWork() }

        val job = captured.captured
        assertEquals(SyncEntityType.PracticalScore.name, job.entityType)
        assertEquals("PE-2024/c1/q1", job.entityKey)
        assertEquals(SyncStatus.Pending.name, job.status)
        assertEquals(1_700_000_000_000L, job.enqueuedAt)
    }

    @Test
    fun `recordScore reports failure with the throwable message`() = runTest {
        coEvery { practicalScoreDao.upsert(any()) } throws RuntimeException("disk full")

        val result = repository.recordScore(
            PracticalScore(
                scheduleId = "s", candidateId = "c", questionId = "q",
                score = 1, scoredAt = 0L,
            ),
        )

        assertTrue(result is SaveResult.Error)
        assertEquals("disk full", (result as SaveResult.Error).message)
        coVerify(exactly = 0) { syncJobDao.enqueue(any()) }
        coVerify(exactly = 0) { workScheduler.scheduleSyncWork() }
    }

    @Test
    fun `commitSection just re-schedules sync work`() = runTest {
        val result = repository.commitSection("s", "c", "secA")

        assertEquals(SaveResult.Success, result)
        coVerify(exactly = 1) { workScheduler.scheduleSyncWork() }
    }
}
