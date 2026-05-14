package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.ProjectScoringRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordProjectScoreUseCaseTest {

    private val now = 1_700_000_000_000L
    private val clock = Clock { now }
    private val repository = mockk<ProjectScoringRepository>()
    private val useCase = RecordProjectScoreUseCase(repository, clock)

    @Test
    fun `rejects blank ids`() = runTest {
        val result = useCase("", "c1", score = 5.0, maxScore = 10)

        assertEquals("Schedule and candidate are required.", (result as SaveResult.Error).message)
    }

    @Test
    fun `rejects NaN and Infinity`() = runTest {
        assertEquals(
            "Score must be a real number.",
            (useCase("s1", "c1", score = Double.NaN, maxScore = 10) as SaveResult.Error).message,
        )
        assertEquals(
            "Score must be a real number.",
            (useCase("s1", "c1", score = Double.POSITIVE_INFINITY, maxScore = 10) as SaveResult.Error).message,
        )
    }

    @Test
    fun `rejects negative score`() = runTest {
        val result = useCase("s1", "c1", score = -0.5, maxScore = 10)

        assertEquals("Score must be between 0 and 10.", (result as SaveResult.Error).message)
    }

    @Test
    fun `rejects score above maxScore`() = runTest {
        val result = useCase("s1", "c1", score = 10.01, maxScore = 10)

        assertEquals("Score must be between 0 and 10.", (result as SaveResult.Error).message)
    }

    @Test
    fun `rejects more than two decimal places`() = runTest {
        val result = useCase("s1", "c1", score = 5.234, maxScore = 10)

        assertEquals(
            "Score may have at most two decimal places.",
            (result as SaveResult.Error).message,
        )
    }

    @Test
    fun `accepts boundary values 0_0 and maxScore`() = runTest {
        coEvery { repository.recordProjectScore(any()) } returns SaveResult.Success

        assertTrue(useCase("s1", "c1", score = 0.0, maxScore = 10) is SaveResult.Success)
        assertTrue(useCase("s1", "c1", score = 10.0, maxScore = 10) is SaveResult.Success)
    }

    @Test
    fun `accepts up to two decimal places`() = runTest {
        coEvery { repository.recordProjectScore(any()) } returns SaveResult.Success

        assertTrue(useCase("s1", "c1", score = 7.5, maxScore = 10) is SaveResult.Success)
        assertTrue(useCase("s1", "c1", score = 7.55, maxScore = 10) is SaveResult.Success)
    }

    @Test
    fun `stamps Pending sync status and clock time on persisted row`() = runTest {
        val captured = slot<ProjectScore>()
        coEvery { repository.recordProjectScore(capture(captured)) } returns SaveResult.Success

        useCase(" s1 ", " c1 ", score = 8.5, maxScore = 10)

        assertEquals(now, captured.captured.scoredAt)
        assertEquals(SyncStatus.Pending, captured.captured.syncStatus)
        assertEquals("s1", captured.captured.scheduleId)
        assertEquals("c1", captured.captured.candidateId)
        assertEquals(8.5, captured.captured.score, 0.0)
        assertEquals(10, captured.captured.maxScore)
        coVerify(exactly = 1) { repository.recordProjectScore(any()) }
    }
}
