package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore
import ng.com.chprbn.mobile.feature.assessment.domain.repository.ProjectScoringRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetProjectScoreUseCaseTest {

    private val repository = mockk<ProjectScoringRepository>()
    private val useCase = GetProjectScoreUseCase(repository)

    @Test
    fun `blank schedule or candidate short-circuits to null`() = runTest {
        assertNull(useCase("", "c1"))
        assertNull(useCase("PE-2024", "  "))
        coVerify(exactly = 0) { repository.getProjectScore(any(), any()) }
    }

    @Test
    fun `trimmed inputs are forwarded`() = runTest {
        val existing = ProjectScore(
            scheduleId = "PE-2024",
            candidateId = "c1",
            score = 8.5,
            maxScore = 10,
            scoredAt = 0L,
            syncStatus = SyncStatus.Synced,
        )
        coEvery { repository.getProjectScore("PE-2024", "c1") } returns existing

        assertEquals(existing, useCase(" PE-2024 ", " c1 "))
        coVerify(exactly = 1) { repository.getProjectScore("PE-2024", "c1") }
    }
}
