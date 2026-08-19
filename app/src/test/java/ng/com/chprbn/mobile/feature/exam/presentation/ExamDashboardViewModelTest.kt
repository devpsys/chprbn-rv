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
import ng.com.chprbn.mobile.feature.profile.domain.usecase.LogoutUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExamDashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getDashboard = mockk<GetExamDashboardUseCase>()
    private val downloadDossier = mockk<DownloadExamDossierUseCase>()
    private val logoutUseCase = mockk<LogoutUseCase>()

    @Test
    fun `Error result keeps the placeholder state`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Error("offline")

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

        assertEquals(ExamDashboardUiState.placeholder(), viewModel.uiState.value)
    }

    @Test
    fun `isLoading flips to false once the first refresh resolves, regardless of outcome`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Error("offline")

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `isLoading stays false across a resume-triggered refresh`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Empty

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)
        assertEquals(false, viewModel.uiState.value.isLoading)

        viewModel.refresh()

        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `Loading result keeps the placeholder state`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Loading

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

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
                papersCount = 2,
            ),
        )

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

        val state = viewModel.uiState.value
        assertEquals("Kano Centre", state.institutionName)
        assertEquals("#KAN", state.institutionCode)
        assertEquals("Kano", state.institutionLocation)
        assertEquals("Closed Session", state.attendanceTask.chipSecondaryLabel)
        assertEquals("Ready to Grade", state.practicalTask.chipSecondaryLabel)
        assertTrue(state.hasDownloadedData)
        assertTrue(state.hasSchedules)
    }

    @Test
    fun `Success with zero papers sets hasSchedules false`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Success(
            ExamDashboardSummary(
                session = OfficerSession("o1", "c1", "2026-06-12"),
                center = Center("c1", "Kano Centre", "KAN", "Kano"),
                attendanceCard = ExamTaskSummary("No Session", "0 / 0 checked in"),
                practicalCard = ExamTaskSummary("Pending Grading", "0 papers"),
                papersCount = 0,
            ),
        )

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

        val state = viewModel.uiState.value
        assertTrue(state.hasDownloadedData)
        assertEquals(false, state.hasSchedules)
    }

    @Test
    fun `Success overrides heroImageUrl when the centre provides one`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Success(
            ExamDashboardSummary(
                session = OfficerSession("o1", "c1", "2026-06-12"),
                center = Center(
                    id = "c1",
                    name = "Kano Centre",
                    code = "KAN",
                    location = "Kano",
                    heroImageUrl = "https://example.com/kano.jpg",
                ),
                attendanceCard = ExamTaskSummary("Active", "x"),
                practicalCard = ExamTaskSummary("Pending", "y"),
            ),
        )

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

        assertEquals("https://example.com/kano.jpg", viewModel.uiState.value.heroImageUrl)
    }

    @Test
    fun `Empty result marks hasDownloadedData false`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Empty

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

        assertEquals(false, viewModel.uiState.value.hasDownloadedData)
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

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

        val placeholder = ExamDashboardUiState.placeholder()
        val state = viewModel.uiState.value
        assertEquals(placeholder.heroImageUrl, state.heroImageUrl)
        assertEquals(placeholder.attendanceTask.primaryActionLabel, state.attendanceTask.primaryActionLabel)
        assertEquals(placeholder.practicalTask.primaryActionLabel, state.practicalTask.primaryActionLabel)
    }

    @Test
    fun `download flow Idle to WarningShown flags an initial download when no dossier cached`() = runTest {
        // Empty puts hasDownloadedData at false — the officer has never
        // pulled a dossier, so the initial-download dialog copy applies.
        coEvery { getDashboard() } returns ExamDashboardResult.Empty
        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

        assertEquals(DownloadDossierUiState.Idle, viewModel.downloadState.value)

        viewModel.onDownloadDossierClicked()

        assertEquals(
            DownloadDossierUiState.WarningShown(isInitialDownload = true),
            viewModel.downloadState.value,
        )
    }

    @Test
    fun `WarningShown flags a refresh when a dossier is already cached`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Success(
            ExamDashboardSummary(
                session = OfficerSession("o1", "c1", "2026-06-12"),
                center = Center("c1", "Kano Centre", "KAN", "Kano"),
                attendanceCard = ExamTaskSummary("Active", "x"),
                practicalCard = ExamTaskSummary("Pending", "y"),
                papersCount = 1,
            ),
        )
        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

        viewModel.onDownloadDossierClicked()

        assertEquals(
            DownloadDossierUiState.WarningShown(isInitialDownload = false),
            viewModel.downloadState.value,
        )
    }

    @Test
    fun `download flow WarningShown to Success on confirm refreshes dashboard`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Loading
        coEvery { downloadDossier() } returns DownloadDossierResult.Success(
            papersCount = 3,
            candidatesCount = 120,
            newCandidatesCount = 5,
            skippedCandidatesCount = 115,
        )

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)
        viewModel.onDownloadDossierClicked()
        viewModel.onDownloadConfirmed()

        val terminal = viewModel.downloadState.value
        assertTrue("expected Success terminal state, was $terminal", terminal is DownloadDossierUiState.Success)
        val success = terminal as DownloadDossierUiState.Success
        assertEquals(3, success.papersCount)
        assertEquals(5, success.newCandidatesCount)
        assertEquals(115, success.skippedCandidatesCount)
        // init + post-download refresh
        coVerify(exactly = 2) { getDashboard() }
    }

    @Test
    fun `download flow Error surfaces the use case message`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Loading
        coEvery { downloadDossier() } returns DownloadDossierResult.Error("network down")

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)
        viewModel.onDownloadDossierClicked()
        viewModel.onDownloadConfirmed()

        val terminal = viewModel.downloadState.value
        assertTrue("expected Error terminal state, was $terminal", terminal is DownloadDossierUiState.Error)
        assertEquals("network down", (terminal as DownloadDossierUiState.Error).message)
    }

    @Test
    fun `download flow dismiss falls back to Idle from WarningShown`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Loading
        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

        viewModel.onDownloadDossierClicked()
        viewModel.onDownloadDismissed()

        assertEquals(DownloadDossierUiState.Idle, viewModel.downloadState.value)
    }

    @Test
    fun `Success with sections sets hasPracticalAssessment true`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Success(
            ExamDashboardSummary(
                session = OfficerSession("o1", "c1", "2026-06-12"),
                center = Center("c1", "Kano Centre", "KAN", "Kano", hasSections = true),
                attendanceCard = ExamTaskSummary("Active", "x"),
                practicalCard = ExamTaskSummary("Pending", "y"),
                papersCount = 1,
                hasPracticalAssessment = true,
            ),
        )

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

        assertTrue(viewModel.uiState.value.hasPracticalAssessment)
    }

    @Test
    fun `Success without sections sets hasPracticalAssessment false`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Success(
            ExamDashboardSummary(
                session = OfficerSession("o1", "c1", "2026-06-12"),
                center = Center("c1", "Kano Centre", "KAN", "Kano", hasSections = false),
                attendanceCard = ExamTaskSummary("Active", "x"),
                practicalCard = ExamTaskSummary("Pending", "y"),
                papersCount = 1,
                hasPracticalAssessment = false,
            ),
        )

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

        assertEquals(false, viewModel.uiState.value.hasPracticalAssessment)
    }

    @Test
    fun `Success with Theory paper sets hasAttendanceCard true`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Success(
            ExamDashboardSummary(
                session = OfficerSession("o1", "c1", "2026-06-12"),
                center = Center("c1", "Kano Centre", "KAN", "Kano"),
                attendanceCard = ExamTaskSummary("Active", "x"),
                practicalCard = ExamTaskSummary("Pending", "y"),
                papersCount = 1,
                hasAttendancePapers = true,
            ),
        )

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

        assertTrue(viewModel.uiState.value.hasAttendanceCard)
    }

    @Test
    fun `Success with only PE_PA papers sets hasAttendanceCard false`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Success(
            ExamDashboardSummary(
                session = OfficerSession("o1", "c1", "2026-06-12"),
                center = Center("c1", "Kano Centre", "KAN", "Kano"),
                attendanceCard = ExamTaskSummary("Active", "x"),
                practicalCard = ExamTaskSummary("Pending", "y"),
                papersCount = 1,
                hasAttendancePapers = false,
            ),
        )

        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

        assertEquals(false, viewModel.uiState.value.hasAttendanceCard)
    }

    @Test
    fun `onLogoutClicked emits loggedOut on success`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Loading
        coEvery { logoutUseCase() } returns Unit
        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

        assertEquals(false, viewModel.loggedOut.value)

        viewModel.onLogoutClicked()

        assertTrue(viewModel.loggedOut.value)
    }

    @Test
    fun `onLogoutClicked does not emit loggedOut on failure`() = runTest {
        coEvery { getDashboard() } returns ExamDashboardResult.Loading
        coEvery { logoutUseCase() } throws IllegalStateException("db locked")
        val viewModel = ExamDashboardViewModel(getDashboard, downloadDossier, logoutUseCase)

        viewModel.onLogoutClicked()

        assertEquals(false, viewModel.loggedOut.value)
    }
}
