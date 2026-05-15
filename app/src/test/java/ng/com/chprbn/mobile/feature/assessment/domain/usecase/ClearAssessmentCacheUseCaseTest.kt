package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentScheduleRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class ClearAssessmentCacheUseCaseTest {

    private val repository = mockk<AssessmentScheduleRepository>()
    private val useCase = ClearAssessmentCacheUseCase(repository)

    @Test
    fun `null schedule id clears all schedules`() = runTest {
        coEvery { repository.clearCache(null) } returns SaveResult.Success

        val result = useCase(scheduleId = null)

        assertEquals(SaveResult.Success, result)
        coVerify(exactly = 1) { repository.clearCache(null) }
    }

    @Test
    fun `blank schedule id is treated as null - clears all`() = runTest {
        coEvery { repository.clearCache(null) } returns SaveResult.Success

        useCase(scheduleId = "   ")

        coVerify(exactly = 1) { repository.clearCache(null) }
    }

    @Test
    fun `trimmed schedule id is forwarded to repository`() = runTest {
        coEvery { repository.clearCache("PE-2024") } returns SaveResult.Success

        useCase(scheduleId = "  PE-2024  ")

        coVerify(exactly = 1) { repository.clearCache("PE-2024") }
    }
}
