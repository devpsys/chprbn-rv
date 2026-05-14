package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.PracticalScoringRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordPracticalScoreUseCaseTest {

    private val now = 1_700_000_000_000L
    private val clock = Clock { now }
    private val repository = mockk<PracticalScoringRepository>()
    private val useCase = RecordPracticalScoreUseCase(repository, clock)

    @Test
    fun `rejects blank schedule id`() = runTest {
        val result = useCase("", "c1", "q1", score = 1, maxScore = 10)

        assertTrue(result is SaveResult.Error)
        assertEquals(
            "Schedule, candidate, and question are required.",
            (result as SaveResult.Error).message,
        )
    }

    @Test
    fun `rejects negative score`() = runTest {
        val result = useCase("s1", "c1", "q1", score = -1, maxScore = 10)

        assertEquals("Score must be between 0 and 10.", (result as SaveResult.Error).message)
    }

    @Test
    fun `rejects score above maxScore`() = runTest {
        val result = useCase("s1", "c1", "q1", score = 11, maxScore = 10)

        assertEquals("Score must be between 0 and 10.", (result as SaveResult.Error).message)
    }

    @Test
    fun `accepts boundary scores 0 and maxScore`() = runTest {
        coEvery { repository.recordScore(any()) } returns SaveResult.Success

        assertTrue(useCase("s1", "c1", "q1", score = 0, maxScore = 10) is SaveResult.Success)
        assertTrue(useCase("s1", "c1", "q1", score = 10, maxScore = 10) is SaveResult.Success)
    }

    @Test
    fun `stamps Pending sync status and clock time on persisted row`() = runTest {
        val captured = slot<PracticalScore>()
        coEvery { repository.recordScore(capture(captured)) } returns SaveResult.Success

        useCase("  s1 ", "  c1 ", " q1 ", score = 7, maxScore = 10)

        assertEquals(now, captured.captured.scoredAt)
        assertEquals(SyncStatus.Pending, captured.captured.syncStatus)
        assertEquals("s1", captured.captured.scheduleId)
        assertEquals("c1", captured.captured.candidateId)
        assertEquals("q1", captured.captured.questionId)
        assertEquals(7, captured.captured.score)
        coVerify(exactly = 1) { repository.recordScore(any()) }
    }
}
