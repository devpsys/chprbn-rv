package ng.com.chprbn.mobile.feature.exam.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.exam.domain.model.Center
import ng.com.chprbn.mobile.feature.exam.domain.model.DownloadDossierResult
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamDashboardResult
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamDashboardSummary
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamTaskSummary
import ng.com.chprbn.mobile.feature.exam.domain.model.OfficerSession
import ng.com.chprbn.mobile.feature.exam.domain.usecase.DownloadExamDossierUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamDashboardUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExamDashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getDashboard = mockk<GetExamDashboardUseCase>()
    private val downloadDossier = mockk<DownloadExamDossierUseCase>()

    @Test
    fun `Error result keeps the placeholder state`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Error("offline")

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier)

        assertEquals(ExamDashboardUiState.placeholder(), viewModel.uiState.value)
    }

    @Test
    fun `Loading result keeps the placeholder state`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Loading

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier)

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

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier)

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

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier)

        val placeholder = ExamDashboardUiState.placeholder()
        val state = viewModel.uiState.value
        assertEquals(placeholder.heroImageUrl, state.heroImageUrl)
        assertEquals(placeholder.attendanceTask.primaryActionLabel, state.attendanceTask.primaryActionLabel)
        assertEquals(placeholder.practicalTask.primaryActionLabel, state.practicalTask.primaryActionLabel)
    }

    @Test
    fun `download flow Idle to WarningShown on click`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Loading
        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier)

        assertEquals(DownloadDossierUiState.Idle, viewModel.downloadState.value)

        viewModel.onDownloadDossierClicked()

        assertEquals(DownloadDossierUiState.WarningShown, viewModel.downloadState.value)
    }

    @Test
    fun `download flow WarningShown to Success on confirm refreshes dashboard`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Loading
        coEvery { downloadDossier() } returns DownloadDossierResult.Success(
            papersCount = 3,
            candidatesCount = 120,
        )

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier)
        viewModel.onDownloadDossierClicked()
        viewModel.onDownloadConfirmed()

        val terminal = viewModel.downloadState.value
        assertTrue("expected Success terminal state, was $terminal", terminal is DownloadDossierUiState.Success)
        val success = terminal as DownloadDossierUiState.Success
        assertEquals(3, success.papersCount)
        assertEquals(120, success.candidatesCount)
        // init + post-download refresh
        coVerify(exactly = 2) { getDashboard() }
    }

    @Test
    fun `download flow Error surfaces the use case message`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Loading
        coEvery { downloadDossier() } returns DownloadDossierResult.Error("network down")

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier)
        viewModel.onDownloadDossierClicked()
        viewModel.onDownloadConfirmed()

        val terminal = viewModel.downloadState.value
        assertTrue("expected Error terminal state, was $terminal", terminal is DownloadDossierUiState.Error)
        assertEquals("network down", (terminal as DownloadDossierUiState.Error).message)
    }

    @Test
    fun `download flow dismiss falls back to Idle from WarningShown`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Loading
        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier)

        viewModel.onDownloadDossierClicked()
        viewModel.onDownloadDismissed()

        assertEquals(DownloadDossierUiState.Idle, viewModel.downloadState.value)
    }
}
