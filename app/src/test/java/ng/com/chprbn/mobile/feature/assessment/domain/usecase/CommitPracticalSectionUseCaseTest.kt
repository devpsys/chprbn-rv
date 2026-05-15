package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.PracticalScoringRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommitPracticalSectionUseCaseTest {

    private val repository = mockk<PracticalScoringRepository>()
    private val useCase = CommitPracticalSectionUseCase(repository)

    @Test
    fun `blank schedule yields validation error`() = runTest {
        val result = useCase(scheduleId = "  ", candidateId = "C1", sectionId = "A")

        assertTrue(result is SaveResult.Error)
        coVerify(exactly = 0) { repository.commitSection(any(), any(), any()) }
    }

    @Test
    fun `blank candidate yields validation error`() = runTest {
        val result = useCase(scheduleId = "S1", candidateId = "", sectionId = "A")

        assertTrue(result is SaveResult.Error)
    }

    @Test
    fun `blank section yields validation error`() = runTest {
        val result = useCase(scheduleId = "S1", candidateId = "C1", sectionId = "  ")

        assertTrue(result is SaveResult.Error)
    }

    @Test
    fun `valid trimmed inputs are forwarded to repository`() = runTest {
        coEvery {
            repository.commitSection("PE-2024", "C-001", "A")
        } returns SaveResult.Success

        val result = useCase(
            scheduleId = " PE-2024 ",
            candidateId = "  C-001",
            sectionId = "A ",
        )

        assertEquals(SaveResult.Success, result)
        coVerify(exactly = 1) { repository.commitSection("PE-2024", "C-001", "A") }
    }
}
