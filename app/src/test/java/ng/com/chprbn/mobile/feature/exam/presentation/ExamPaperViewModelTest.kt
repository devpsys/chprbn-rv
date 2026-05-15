package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.exam.domain.model.Center
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamPaperDetail
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamPaperDetailResult
import ng.com.chprbn.mobile.feature.exam.domain.model.Paper
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamPaperDetailUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.SyncExamRecordsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExamPaperViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPaperDetail = mockk<GetExamPaperDetailUseCase>()
    private val syncExamRecords = mockk<SyncExamRecordsUseCase>()
    private val savedState = SavedStateHandle(mapOf("paperId" to "p1"))

    @Test
    fun `NotFound keeps the placeholder state`() = runTest {
        coEvery { getPaperDetail("p1") } returns ExamPaperDetailResult.NotFound

        val viewModel = ExamPaperViewModel(savedState, getPaperDetail, syncExamRecords)

        assertEquals(ExamPaperUiState.placeholder(), viewModel.uiState.value)
    }

    @Test
    fun `Error keeps the placeholder state`() = runTest {
        coEvery { getPaperDetail("p1") } returns ExamPaperDetailResult.Error("boom")

        val viewModel = ExamPaperViewModel(savedState, getPaperDetail, syncExamRecords)

        assertEquals(ExamPaperUiState.placeholder(), viewModel.uiState.value)
    }

    @Test
    fun `Success populates institution and progress fields`() = runTest {
        coEvery { getPaperDetail("p1") } returns ExamPaperDetailResult.Success(
            ExamPaperDetail(
                paper = Paper(
                    id = "p1",
                    centerId = "C-1",
                    title = "Mathematics — Paper II",
                    subtitle = "Algebra",
                    paperKind = PaperKind.Theory,
                    startAt = 0L,
                    endAt = 0L,
                    hall = "Main Hall A",
                    totalCandidates = 120,
                ),
                center = Center(
                    id = "C-1",
                    name = "Lagos Centre",
                    code = "LAG-001",
                    location = "Marina Rd",
                    heroImageUrl = null,
                ),
                totalCandidates = 120,
                checkedInCount = 84,
                lastSyncAt = null,
                pendingSyncCount = 3,
            ),
        )

        val viewModel = ExamPaperViewModel(savedState, getPaperDetail, syncExamRecords)

        val state = viewModel.uiState.value
        assertEquals("Mathematics — Paper II", state.paperTitle)
        assertEquals("Lagos Centre", state.institutionName)
        assertEquals("LAG-001", state.institutionShortCode)
        assertEquals("120", state.totalCandidates)
        assertEquals("84", state.verifiedPresent)
        assertEquals("70%", state.attendancePercentLabel)
        assertTrue("expected pending suffix on sync label, was: ${state.syncStatusLabel}", state.syncStatusLabel.contains("Pending Sync"))
        assertTrue(state.attendanceProgressFraction in 0f..1f)
    }

    @Test
    fun `onSyncData runs sync and re-fetches paper detail`() = runTest {
        coEvery { getPaperDetail("p1") } returns ExamPaperDetailResult.NotFound
        coEvery { syncExamRecords() } returns SyncBatchResult.Empty

        val viewModel = ExamPaperViewModel(savedState, getPaperDetail, syncExamRecords)

        viewModel.onSyncData()

        assertEquals(SyncOperationUiState.Idle, viewModel.syncState.value)
        coVerify(exactly = 1) { syncExamRecords() }
        coVerify(exactly = 2) { getPaperDetail("p1") }
    }
}
