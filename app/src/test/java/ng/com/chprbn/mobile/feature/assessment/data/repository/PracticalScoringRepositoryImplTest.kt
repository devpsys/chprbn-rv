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
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreEntity
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalSectionDao
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalSectionEntity
import ng.com.chprbn.mobile.feature.assessment.data.local.SectionQuestionDao
import ng.com.chprbn.mobile.feature.assessment.data.local.SectionQuestionEntity
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionStatus
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `recordScore` is the most consequential write path in the assessment
 * feature — every stepper tap goes through it. We assert the full chain:
 * upsert, enqueue, schedule-status refresh, work scheduling.
 *
 * `getSections` is the second-most consequential read — it joins three
 * tables in the JVM (sections × questions × scores) and derives status +
 * lastUpdatedAt.
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

    @Test
    fun `getSections derives Complete when all questions in a section are scored`() = runTest {
        seedTwoSections(
            sectionAQuestions = listOf("q1", "q2"),
            sectionBQuestions = listOf("q3"),
            scores = listOf(
                practicalScore("q1", scoredAt = 100L),
                practicalScore("q2", scoredAt = 200L),
            ),
        )

        val sections = repository.getSections("PE-2024", "c1")

        val a = sections.first { it.section.id == "sec-A" }
        assertEquals(PracticalSectionStatus.Complete, a.status)
        assertEquals(2, a.scoredCount)
        assertEquals(2, a.totalCount)
        assertEquals(200L, a.lastUpdatedAt)
    }

    @Test
    fun `getSections derives Incomplete when only some questions are scored`() = runTest {
        seedTwoSections(
            sectionAQuestions = listOf("q1", "q2", "q3"),
            sectionBQuestions = emptyList(),
            scores = listOf(practicalScore("q1", scoredAt = 100L)),
        )

        val a = repository.getSections("PE-2024", "c1").first { it.section.id == "sec-A" }
        assertEquals(PracticalSectionStatus.Incomplete, a.status)
        assertEquals(1, a.scoredCount)
        assertEquals(3, a.totalCount)
        assertEquals(100L, a.lastUpdatedAt)
    }

    @Test
    fun `getSections derives NotStarted when no questions scored`() = runTest {
        seedTwoSections(
            sectionAQuestions = listOf("q1", "q2"),
            sectionBQuestions = emptyList(),
            scores = emptyList(),
        )

        val a = repository.getSections("PE-2024", "c1").first { it.section.id == "sec-A" }
        assertEquals(PracticalSectionStatus.NotStarted, a.status)
        assertEquals(0, a.scoredCount)
        assertEquals(2, a.totalCount)
        assertNull(a.lastUpdatedAt)
    }

    private fun seedTwoSections(
        sectionAQuestions: List<String>,
        sectionBQuestions: List<String>,
        scores: List<PracticalScoreEntity>,
    ) {
        coEvery { sectionDao.getByScheduleId("PE-2024") } returns listOf(
            PracticalSectionEntity("sec-A", "PE-2024", "A", "Vitals", 1),
            PracticalSectionEntity("sec-B", "PE-2024", "B", "Diagnosis", 2),
        )
        coEvery { questionDao.getByScheduleId("PE-2024") } returns
            sectionAQuestions.map { question(it, "sec-A") } +
                sectionBQuestions.map { question(it, "sec-B") }
        coEvery { practicalScoreDao.getForCandidate("PE-2024", "c1") } returns scores
    }

    private fun question(id: String, sectionId: String) = SectionQuestionEntity(
        id = id,
        sectionId = sectionId,
        number = 1,
        prompt = "x",
        imageUrl = null,
        maxScore = 10,
    )

    private fun practicalScore(questionId: String, scoredAt: Long) = PracticalScoreEntity(
        scheduleId = "PE-2024",
        candidateId = "c1",
        questionId = questionId,
        score = 5,
        scoredAt = scoredAt,
        syncStatus = SyncStatus.Synced.name,
    )
}
