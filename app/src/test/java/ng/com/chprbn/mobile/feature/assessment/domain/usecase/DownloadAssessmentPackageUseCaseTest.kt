package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.assessment.domain.model.DownloadAssessmentPackageResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentScheduleRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadAssessmentPackageUseCaseTest {

    private val repository = mockk<AssessmentScheduleRepository>()
    private val useCase = DownloadAssessmentPackageUseCase(repository)

    @Test
    fun `rejects blank schedule id without touching repository`() = runTest {
        val result = useCase("   ")

        assertEquals(
            "Schedule id is required.",
            (result as DownloadAssessmentPackageResult.Error).message,
        )
        coVerify(exactly = 0) { repository.downloadPackage(any()) }
    }

    @Test
    fun `trims schedule id and forwards to repository`() = runTest {
        val expected = DownloadAssessmentPackageResult.Success(
            scheduleId = "PE-2024",
            candidatesCount = 150,
            sectionsCount = 3,
            questionsCount = 18,
        )
        coEvery { repository.downloadPackage("PE-2024") } returns expected

        val result = useCase("  PE-2024  ")

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.downloadPackage("PE-2024") }
    }
}
