package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaper
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaperDetailResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.Facility
import ng.com.chprbn.mobile.feature.assessment.domain.model.Hall
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentScheduleRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAssessmentPaperDetailUseCaseTest {

    private val repository = mockk<AssessmentScheduleRepository>()
    private val useCase = GetAssessmentPaperDetailUseCase(repository)

    @Test
    fun `blank schedule returns validation error`() = runTest {
        val result = useCase("  ")

        assertEquals(
            "Schedule id is required.",
            (result as AssessmentPaperDetailResult.Error).message,
        )
        coVerify(exactly = 0) { repository.getPaperDetail(any()) }
    }

    @Test
    fun `trimmed schedule is forwarded and result returned`() = runTest {
        val expected = AssessmentPaperDetailResult.Success(
            AssessmentPaper(
                scheduleId = "PE-2024",
                title = "Pharmacology",
                statusLabel = "Active",
                facility = Facility("F1", "Addr"),
                hall = Hall("H1", "Addr"),
                heroImageUrl = null,
            ),
        )
        coEvery { repository.getPaperDetail("PE-2024") } returns expected

        val result = useCase("  PE-2024 ")

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.getPaperDetail("PE-2024") }
    }
}
