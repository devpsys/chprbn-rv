package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSchedule
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentScheduleRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class GetExaminationSchedulesUseCaseTest {

    private val repository = mockk<AssessmentScheduleRepository>()
    private val useCase = GetExaminationSchedulesUseCase(repository)

    @Test
    fun `delegates to repository and returns its list`() = runTest {
        val expected = listOf(
            AssessmentSchedule(
                id = "PE-2024-1",
                title = "Pharmacology",
                date = 1_730_000_000_000L,
                paperKind = PaperKind.Practical,
                centerId = "C-1",
                syncStatus = SyncStatus.Synced,
            ),
        )
        coEvery { repository.getSchedules() } returns expected

        val result = useCase()

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.getSchedules() }
    }

    @Test
    fun `empty repository result propagates`() = runTest {
        coEvery { repository.getSchedules() } returns emptyList()

        val result = useCase()

        assertEquals(emptyList<AssessmentSchedule>(), result)
    }
}
