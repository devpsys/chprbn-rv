package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentCandidateRow
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaper
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaperDetailResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.DownloadAssessmentPackageResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.Facility
import ng.com.chprbn.mobile.feature.assessment.domain.model.Hall
import ng.com.chprbn.mobile.feature.assessment.domain.model.ScoreLevel
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentCandidateRepository
import ng.com.chprbn.mobile.feature.assessment.domain.repository.PracticalScoringRepository
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.DownloadAssessmentPackageUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetAssessmentCandidatesUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetAssessmentPaperDetailUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AssessmentPaperDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPaperDetail = mockk<GetAssessmentPaperDetailUseCase>()
    private val getCandidates = mockk<GetAssessmentCandidatesUseCase>()
    private val downloadPackage = mockk<DownloadAssessmentPackageUseCase>()
    private val candidateRepository = mockk<AssessmentCandidateRepository>()
    private val practicalScoringRepository = mockk<PracticalScoringRepository>()
    private val savedState = SavedStateHandle(mapOf("scheduleId" to "PE-2024"))

    /**
     * Set the (assigned, started) counts the progress-observe flow will
     * emit. Tests call this before instantiating the VM. Defaults keep
     * pre-A-S5 behaviour of `totalCount = candidates.size` so the pre-fix
     * assertions still describe a meaningful state.
     */
    private fun stubProgress(assigned: Int, started: Int = 0) {
        every { candidateRepository.observeAssignedCount("PE-2024") } returns flowOf(assigned)
        every {
            practicalScoringRepository.observeStartedCandidateCount("PE-2024")
        } returns flowOf(started)
    }

    private fun makeViewModel() = AssessmentPaperDetailViewModel(
        savedState,
        getPaperDetail,
        getCandidates,
        downloadPackage,
        candidateRepository,
        practicalScoringRepository,
    )

    @Before
    fun defaultStubs() {
        // Default progress emits zeros so init doesn't blow up in tests that
        // don't care about the counts. Overridden in specific tests.
        stubProgress(assigned = 0, started = 0)
    }

    @Test
    fun `Success path populates paper fields and caps candidate preview at 2`() = runTest {
        coEvery { getPaperDetail("PE-2024") } returns AssessmentPaperDetailResult.Success(
            AssessmentPaper(
                scheduleId = "PE-2024",
                title = "Paper A",
                statusLabel = "Active",
                facility = Facility("Lagos", "10 Marina"),
                hall = Hall("Hall B", "Room 12"),
                heroImageUrl = null,
            ),
        )
        coEvery { getCandidates("PE-2024", "") } returns listOf(
            candidateRow("c1", "Jane Doe", SyncStatus.Synced),
            candidateRow("c2", "John Smith", SyncStatus.Pending),
            candidateRow("c3", "Three", SyncStatus.Synced),
            candidateRow("c4", "Four", SyncStatus.Synced),
        )
        // A-S5: `totalCount` / `progressFraction` are driven by the live
        // combine(assigned, started) flow; stub 4 assigned / 4 started to
        // reproduce the pre-fix behaviour of 100% progress on this case.
        stubProgress(assigned = 4, started = 4)

        val vm = makeViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("Paper A", state.paperTitle)
        assertEquals("Active", state.statusLabel)
        assertEquals(4, state.totalCount)
        assertEquals(4, state.checkedInCount)
        assertEquals(1f, state.progressFraction)
        assertEquals("Lagos", state.facilityName)
        assertEquals("Hall B", state.hallName)
        assertEquals("preview capped at 2 rows", 2, state.candidates.size)
        assertEquals("JD", state.candidates[0].initials)
        assertEquals(CandidateSyncStatus.Synced, state.candidates[0].syncStatus)
        assertEquals("JS", state.candidates[1].initials)
        assertEquals(CandidateSyncStatus.Unsynced, state.candidates[1].syncStatus)
    }

    @Test
    fun `NotFound leaves paper fields blank but still surfaces candidate preview`() = runTest {
        coEvery { getPaperDetail("PE-2024") } returns AssessmentPaperDetailResult.NotFound
        coEvery { getCandidates("PE-2024", "") } returns listOf(
            candidateRow("c1", "Jane Doe", SyncStatus.Synced),
        )
        stubProgress(assigned = 1, started = 0)

        val vm = makeViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("", state.paperTitle)
        assertEquals("", state.statusLabel)
        assertEquals(1, state.totalCount)
        assertEquals(1, state.candidates.size)
    }

    @Test
    fun `Error path falls back to empty paper fields`() = runTest {
        coEvery { getPaperDetail("PE-2024") } returns AssessmentPaperDetailResult.Error("boom")
        coEvery { getCandidates("PE-2024", "") } returns emptyList()
        stubProgress(assigned = 0, started = 0)

        val vm = makeViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("", state.paperTitle)
        assertEquals(0, state.totalCount)
        assertTrue(state.candidates.isEmpty())
    }

    @Test
    fun `single-name candidate yields a single-letter initial`() = runTest {
        coEvery { getPaperDetail("PE-2024") } returns AssessmentPaperDetailResult.Success(paper())
        coEvery { getCandidates("PE-2024", "") } returns listOf(
            candidateRow("c1", "Cher", SyncStatus.Synced),
        )

        val vm = makeViewModel()

        assertEquals("C", vm.uiState.value.candidates.single().initials)
    }

    @Test
    fun `download flow Idle to WarningShown on click`() = runTest {
        coEvery { getPaperDetail("PE-2024") } returns AssessmentPaperDetailResult.NotFound
        coEvery { getCandidates("PE-2024", "") } returns emptyList()

        val vm = makeViewModel()
        assertEquals(DownloadPackageUiState.Idle, vm.downloadState.value)

        vm.onDownloadPackageClicked()

        assertEquals(DownloadPackageUiState.WarningShown, vm.downloadState.value)
    }

    @Test
    fun `download flow confirms with Success and refreshes screen`() = runTest {
        coEvery { getPaperDetail("PE-2024") } returns AssessmentPaperDetailResult.NotFound
        coEvery { getCandidates("PE-2024", "") } returns emptyList()
        coEvery { downloadPackage("PE-2024") } returns DownloadAssessmentPackageResult.Success(
            scheduleId = "PE-2024",
            candidatesCount = 42,
            sectionsCount = 3,
            questionsCount = 90,
        )

        val vm = makeViewModel()
        vm.onDownloadPackageClicked()
        vm.onDownloadConfirmed()

        val terminal = vm.downloadState.value
        assertTrue("expected Success terminal state, was $terminal", terminal is DownloadPackageUiState.Success)
        val success = terminal as DownloadPackageUiState.Success
        assertEquals(42, success.candidatesCount)
        assertEquals(3, success.sectionsCount)
        assertEquals(90, success.questionsCount)
        // init + post-download refresh
        coVerify(exactly = 2) { getPaperDetail("PE-2024") }
        coVerify(exactly = 2) { getCandidates("PE-2024", "") }
    }

    @Test
    fun `download flow Error surfaces the use case message`() = runTest {
        coEvery { getPaperDetail("PE-2024") } returns AssessmentPaperDetailResult.NotFound
        coEvery { getCandidates("PE-2024", "") } returns emptyList()
        coEvery { downloadPackage("PE-2024") } returns
            DownloadAssessmentPackageResult.Error("offline")

        val vm = makeViewModel()
        vm.onDownloadPackageClicked()
        vm.onDownloadConfirmed()

        val terminal = vm.downloadState.value
        assertTrue("expected Error terminal state, was $terminal", terminal is DownloadPackageUiState.Error)
        assertEquals("offline", (terminal as DownloadPackageUiState.Error).message)
    }

    @Test
    fun `download flow dismiss falls back to Idle from WarningShown`() = runTest {
        coEvery { getPaperDetail("PE-2024") } returns AssessmentPaperDetailResult.NotFound
        coEvery { getCandidates("PE-2024", "") } returns emptyList()

        val vm = makeViewModel()
        vm.onDownloadPackageClicked()
        vm.onDownloadDismissed()

        assertEquals(DownloadPackageUiState.Idle, vm.downloadState.value)
    }

    private fun candidateRow(id: String, fullName: String, status: SyncStatus) =
        AssessmentCandidateRow(
            candidate = Candidate(id = id, examNumber = "EX-$id", fullName = fullName),
            aggregateScore = 0,
            level = ScoreLevel.Normal,
            scoredQuestions = 0,
            totalQuestions = 0,
            syncStatus = status,
        )

    private fun paper() = AssessmentPaper(
        scheduleId = "PE-2024",
        title = "x",
        statusLabel = "y",
        facility = Facility("f", "a"),
        hall = Hall("h", "a"),
        heroImageUrl = null,
    )
}
