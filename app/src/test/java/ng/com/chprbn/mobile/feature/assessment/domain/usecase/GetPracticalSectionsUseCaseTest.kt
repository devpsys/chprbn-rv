package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSection
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionStatus
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionSummary
import ng.com.chprbn.mobile.feature.assessment.domain.repository.PracticalScoringRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetPracticalSectionsUseCaseTest {

    private val repository = mockk<PracticalScoringRepository>()
    private val useCase = GetPracticalSectionsUseCase(repository)

    @Test
    fun `blank schedule or candidate returns empty list`() = runTest {
        assertTrue(useCase("", "C1").isEmpty())
        assertTrue(useCase("S1", "  ").isEmpty())
        coVerify(exactly = 0) { repository.getSections(any(), any()) }
    }

    @Test
    fun `trimmed inputs forwarded and summary list returned`() = runTest {
        val summary = PracticalSectionSummary(
            section = PracticalSection(
                id = "A",
                scheduleId = "PE",
                title = "Section A",
                subtitle = "Patient Assessment",
                ordering = 0,
            ),
            status = PracticalSectionStatus.Incomplete,
            scoredCount = 3,
            totalCount = 6,
            lastUpdatedAt = 1_730_000_000_000L,
        )
        coEvery { repository.getSections("PE", "C1") } returns listOf(summary)

        val result = useCase(" PE ", " C1 ")

        assertEquals(listOf(summary), result)
        coVerify(exactly = 1) { repository.getSections("PE", "C1") }
    }
}
