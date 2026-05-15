package ng.com.chprbn.mobile.feature.exam.presentation

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.exam.domain.model.Center
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamDashboardResult
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamDashboardSummary
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamTaskSummary
import ng.com.chprbn.mobile.feature.exam.domain.model.OfficerSession
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamDashboardUseCase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ExamDashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getDashboard = mockk<GetExamDashboardUseCase>()

    @Test
    fun `Error result keeps the placeholder state`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Error("offline")

        val viewModel = ExamDashboardViewModel(getDashboard)

        assertEquals(ExamDashboardUiState.placeholder(), viewModel.uiState.value)
    }

    @Test
    fun `Loading result keeps the placeholder state`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Loading

        val viewModel = ExamDashboardViewModel(getDashboard)

        assertEquals(ExamDashboardUiState.placeholder(), viewModel.uiState.value)
    }

    @Test
    fun `Success overrides centre fields and chip secondary labels`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Success(
            ExamDashboardSummary(
                session = OfficerSession("o1", "c1", "2026-06-12"),
                center = Center(
                    id = "c1",
                    name = "Kano Centre",
                    code = "KAN",
                    location = "Kano",
                ),
                attendanceCard = ExamTaskSummary("Closed Session", "0 / 0 checked in"),
                practicalCard = ExamTaskSummary("Ready to Grade", "3 papers"),
            ),
        )

        val viewModel = ExamDashboardViewModel(getDashboard)

        val state = viewModel.uiState.value
        assertEquals("Kano Centre", state.institutionName)
        assertEquals("#KAN", state.institutionCode)
        assertEquals("Kano", state.institutionLocation)
        assertEquals("Closed Session", state.attendanceTask.chipSecondaryLabel)
        assertEquals("Ready to Grade", state.practicalTask.chipSecondaryLabel)
    }

    @Test
    fun `Success preserves placeholder static fields (hero, action labels)`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Success(
            ExamDashboardSummary(
                session = OfficerSession("o1", "c1", "2026-06-12"),
                center = Center("c1", "Kano Centre", "KAN", "Kano"),
                attendanceCard = ExamTaskSummary("Active", "x"),
                practicalCard = ExamTaskSummary("Pending", "y"),
            ),
        )

        val viewModel = ExamDashboardViewModel(getDashboard)

        val placeholder = ExamDashboardUiState.placeholder()
        val state = viewModel.uiState.value
        assertEquals(placeholder.heroImageUrl, state.heroImageUrl)
        assertEquals(placeholder.attendanceTask.primaryActionLabel, state.attendanceTask.primaryActionLabel)
        assertEquals(placeholder.practicalTask.primaryActionLabel, state.practicalTask.primaryActionLabel)
    }
}
