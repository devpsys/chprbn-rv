package ng.com.chprbn.mobile.feature.exam.presentation

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.R
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
    private val context = mockk<Context> {
        every { getString(R.string.exam_paper_institution_code_label) } returns "Institution Code"
        every { getString(R.string.exam_paper_sync_status_cloud_synced) } returns "Cloud Synced"
        every { getString(R.string.exam_paper_info_title_verification_ongoing) } returns "Verification ongoing"
        every { getString(R.string.exam_paper_no_data_yet) } returns "No data yet"
        every { getString(R.string.exam_paper_session_label_today) } returns "Today's Session"
        every { getString(R.string.exam_paper_info_message_verification_ongoing) } returns "Please ensure..."
        every { getString(R.string.exam_paper_error_not_found) } returns "This paper isn't cached yet."
    }

    @Test
    fun `NotFound keeps existing content but sets an error banner message`() = runTest {
        coEvery { getPaperDetail("p1") } returns ExamPaperDetailResult.NotFound

        val viewModel = ExamPaperViewModel(savedState, getPaperDetail, syncExamRecords, context)

        val state = viewModel.uiState.value
        assertEquals("This paper isn't cached yet.", state.errorMessage)
        // Content underneath the banner is untouched — still the placeholder,
        // since Success never ran.
        assertEquals(ExamPaperUiState.placeholder(), state.copy(errorMessage = null))
    }

    @Test
    fun `Error keeps existing content but surfaces the use case message as the banner`() = runTest {
        coEvery { getPaperDetail("p1") } returns ExamPaperDetailResult.Error("boom")

        val viewModel = ExamPaperViewModel(savedState, getPaperDetail, syncExamRecords, context)

        val state = viewModel.uiState.value
        assertEquals("boom", state.errorMessage)
        assertEquals(ExamPaperUiState.placeholder(), state.copy(errorMessage = null))
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

        val viewModel = ExamPaperViewModel(savedState, getPaperDetail, syncExamRecords, context)

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
    fun `onSyncData runs sync, re-fetches paper detail, and surfaces the sync result`() = runTest {
        coEvery { getPaperDetail("p1") } returns ExamPaperDetailResult.NotFound
        coEvery { syncExamRecords() } returns SyncBatchResult(attempted = 3, succeeded = 2, failed = 1)

        val viewModel = ExamPaperViewModel(savedState, getPaperDetail, syncExamRecords, context)

        viewModel.onSyncData()

        assertEquals(
            SyncOperationUiState.Result(succeeded = 2, failed = 1),
            viewModel.syncState.value,
        )
        coVerify(exactly = 1) { syncExamRecords() }
        coVerify(exactly = 2) { getPaperDetail("p1") }
    }

    @Test
    fun `onSyncResultDismissed resets sync state to Idle`() = runTest {
        coEvery { getPaperDetail("p1") } returns ExamPaperDetailResult.NotFound
        coEvery { syncExamRecords() } returns SyncBatchResult.Empty
        val viewModel = ExamPaperViewModel(savedState, getPaperDetail, syncExamRecords, context)
        viewModel.onSyncData()

        viewModel.onSyncResultDismissed()

        assertEquals(SyncOperationUiState.Idle, viewModel.syncState.value)
    }
}
