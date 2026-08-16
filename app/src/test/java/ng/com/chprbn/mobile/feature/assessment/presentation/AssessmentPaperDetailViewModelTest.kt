package ng.com.chprbn.mobile.feature.assessment.presentation

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentCandidateRow
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaper
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaperDetailResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSyncStats
import ng.com.chprbn.mobile.feature.assessment.domain.model.Facility
import ng.com.chprbn.mobile.feature.assessment.domain.model.Hall
import ng.com.chprbn.mobile.feature.assessment.domain.model.ScoreLevel
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentCandidateRepository
import ng.com.chprbn.mobile.feature.assessment.domain.repository.PracticalScoringRepository
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetAssessmentCandidatesUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetAssessmentPaperDetailUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetAssessmentSyncStatsUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.SyncAssessmentScoresUseCase
import ng.com.chprbn.mobile.feature.exam.presentation.SyncOperationUiState
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
    private val getSyncStats = mockk<GetAssessmentSyncStatsUseCase>()
    private val syncScores = mockk<SyncAssessmentScoresUseCase>()
    private val candidateRepository = mockk<AssessmentCandidateRepository>()
    private val practicalScoringRepository = mockk<PracticalScoringRepository>()
    private val savedState = SavedStateHandle(mapOf("scheduleId" to "PE-2024"))
    private val context = mockk<Context>(relaxed = true) {
        every { getString(R.string.assessment_paper_detail_no_data_yet) } returns "No data yet"
        every { getString(R.string.assessment_paper_detail_sync_status_cloud_synced) } returns "Cloud Synced"
        every { getString(R.string.assessment_paper_detail_pending_sync_format, 5) } returns
            "Pending Sync (5)"
    }

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
        getSyncStats,
        syncScores,
        candidateRepository,
        practicalScoringRepository,
        context,
    )

    @Before
    fun defaultStubs() {
        stubProgress(assigned = 0, started = 0)
        coEvery { getSyncStats("PE-2024") } returns AssessmentSyncStats(0, null)
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
        assertEquals("No data yet", state.lastUpdatedLabel)
        assertEquals("Cloud Synced", state.syncStatusLabel)
    }

    @Test
    fun `pending sync count surfaces on the progress chrome`() = runTest {
        coEvery { getPaperDetail("PE-2024") } returns AssessmentPaperDetailResult.Success(paper())
        coEvery { getCandidates("PE-2024", "") } returns emptyList()
        coEvery { getSyncStats("PE-2024") } returns AssessmentSyncStats(pendingSyncCount = 5, lastSyncAt = null)

        val vm = makeViewModel()
        advanceUntilIdle()

        assertEquals("Pending Sync (5)", vm.uiState.value.syncStatusLabel)
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
        advanceUntilIdle()

        assertEquals("C", vm.uiState.value.candidates.single().initials)
    }

    @Test
    fun `onSyncData runs sync, refreshes, and surfaces the sync result`() = runTest {
        coEvery { getPaperDetail("PE-2024") } returns AssessmentPaperDetailResult.Success(paper())
        coEvery { getCandidates("PE-2024", "") } returns emptyList()
        coEvery { syncScores() } returns SyncBatchResult(attempted = 3, succeeded = 2, failed = 1)

        val vm = makeViewModel()
        advanceUntilIdle()
        vm.onSyncData()
        advanceUntilIdle()

        assertEquals(
            SyncOperationUiState.Result(succeeded = 2, failed = 1),
            vm.syncState.value,
        )
        coVerify(exactly = 1) { syncScores() }
        coVerify(exactly = 2) { getPaperDetail("PE-2024") }
    }

    @Test
    fun `onSyncResultDismissed resets sync state to Idle`() = runTest {
        coEvery { getPaperDetail("PE-2024") } returns AssessmentPaperDetailResult.Success(paper())
        coEvery { getCandidates("PE-2024", "") } returns emptyList()
        coEvery { syncScores() } returns SyncBatchResult.Empty

        val vm = makeViewModel()
        advanceUntilIdle()
        vm.onSyncData()
        advanceUntilIdle()
        vm.onSyncResultDismissed()

        assertEquals(SyncOperationUiState.Idle, vm.syncState.value)
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
