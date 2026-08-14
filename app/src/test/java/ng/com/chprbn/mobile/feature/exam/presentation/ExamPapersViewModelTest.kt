package ng.com.chprbn.mobile.feature.exam.presentation

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.exam.domain.model.Paper
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamPapersUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.SyncExamRecordsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ExamPapersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPapers = mockk<GetExamPapersUseCase>()
    private val syncExamRecords = mockk<SyncExamRecordsUseCase>()
    // Clock is stubbed at a time when every sample paper's window is either
    // unset (0L, falls back to the first-item-Active heuristic) or bracketed
    // by the existing test fixtures, so behaviour matches pre-E9 expectations.
    private val clock = Clock { 1_700_000_000_000L }
    private val context = mockk<Context> {
        every { getString(R.string.exam_papers_daily_overview_title) } returns "Daily Overview"
        every { getString(R.string.exam_papers_status_pill_in_progress) } returns "In Progress"
        every { getString(R.string.exam_papers_action_mark_attendance) } returns "Mark Attendance"
    }

    @Test
    fun `empty use case result surfaces a real empty state, not the placeholder forever`() = runTest {
        coEvery { getPapers() } returns emptyList()

        val viewModel = ExamPapersViewModel(getPapers, syncExamRecords, clock, context)

        val state = viewModel.uiState.value
        assertTrue(state.hasDownloadedData)
        assertEquals(emptyList<ExamPaperCardUiState>(), state.papers)
        assertEquals("00", state.totalPapersLabel)
        assertEquals("0", state.studentsLabel)
    }

    @Test
    fun `dailyDateLabel reflects the injected clock, not a paper's startAt`() = runTest {
        // The fixture paper's startAt is 1_730_000_000_000L — deliberately
        // different from the clock below, so a passing assertion proves
        // the date comes from Clock.nowMillis(), not firstOrNull()?.startAt
        // (the bug this pinned: the summary used to show whatever epoch
        // millis the first cached paper happened to carry, not today).
        coEvery { getPapers() } returns listOf(paper("p1", "Paper I", PaperKind.Theory))
        val fixedClock = Clock { 1_700_000_000_000L }
        val expectedDate = DateTimeFormatter
            .ofPattern("EEEE, MMMM d", Locale.US)
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(1_700_000_000_000L))

        val viewModel = ExamPapersViewModel(getPapers, syncExamRecords, fixedClock, context)

        assertEquals(expectedDate, viewModel.uiState.value.dailyDateLabel)
    }

    @Test
    fun `first paper is marked Active with Mark Attendance action`() = runTest {
        coEvery { getPapers() } returns listOf(
            paper("p1", "Paper I", PaperKind.Theory),
            paper("p2", "Paper II", PaperKind.Practical),
        )

        val viewModel = ExamPapersViewModel(getPapers, syncExamRecords, clock, context)

        val cards = viewModel.uiState.value.papers
        assertEquals(2, cards.size)
        val first = cards.first()
        assertEquals(ExamPaperAttendanceStatus.Active, first.status)
        assertEquals("Mark Attendance", first.primaryActionLabel)
        assertNull(cards.last().primaryActionLabel)
    }

    @Test
    fun `paper kind maps to icon kind`() = runTest {
        coEvery { getPapers() } returns listOf(
            paper("p1", "Theory", PaperKind.Theory),
            paper("p2", "Practical", PaperKind.Practical),
            paper("p3", "Project", PaperKind.Project),
        )

        val viewModel = ExamPapersViewModel(getPapers, syncExamRecords, clock, context)

        val byId = viewModel.uiState.value.papers.associateBy { it.id }
        assertEquals(ExamPaperIconKind.Description, byId.getValue("p1").iconKind)
        assertEquals(ExamPaperIconKind.Science, byId.getValue("p2").iconKind)
        assertEquals(ExamPaperIconKind.EditNote, byId.getValue("p3").iconKind)
    }

    @Test
    fun `onSyncNow toggles sync state and re-runs getPapers`() = runTest {
        coEvery { getPapers() } returns emptyList()
        coEvery { syncExamRecords() } returns SyncBatchResult.Empty

        val viewModel = ExamPapersViewModel(getPapers, syncExamRecords, clock, context)

        assertEquals(SyncOperationUiState.Idle, viewModel.syncState.value)

        viewModel.onSyncNow()

        assertEquals(SyncOperationUiState.Idle, viewModel.syncState.value)
        coVerify(exactly = 1) { syncExamRecords() }
        coVerify(exactly = 2) { getPapers() }
    }

    private fun paper(id: String, title: String, kind: PaperKind) = Paper(
        id = id,
        centerId = "C-1",
        title = title,
        subtitle = "subtitle",
        paperKind = kind,
        startAt = 1_730_000_000_000L,
        endAt = 1_730_003_600_000L,
        hall = "Hall A",
        totalCandidates = 42,
    )
}
