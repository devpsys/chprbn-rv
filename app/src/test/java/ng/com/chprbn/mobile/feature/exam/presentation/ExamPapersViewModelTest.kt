package ng.com.chprbn.mobile.feature.exam.presentation

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.exam.domain.model.Paper
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamPapersUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class ExamPapersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPapers = mockk<GetExamPapersUseCase>()

    @Test
    fun `empty use case result keeps the placeholder state`() = runTest {
        coEvery { getPapers() } returns emptyList()

        val viewModel = ExamPapersViewModel(getPapers)

        assertEquals(ExamPapersUiState.placeholder(), viewModel.uiState.value)
    }

    @Test
    fun `first paper is marked Active with Mark Attendance action`() = runTest {
        coEvery { getPapers() } returns listOf(
            paper("p1", "Paper I", PaperKind.Theory),
            paper("p2", "Paper II", PaperKind.Practical),
        )

        val viewModel = ExamPapersViewModel(getPapers)

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

        val viewModel = ExamPapersViewModel(getPapers)

        val byId = viewModel.uiState.value.papers.associateBy { it.id }
        assertEquals(ExamPaperIconKind.Description, byId.getValue("p1").iconKind)
        assertEquals(ExamPaperIconKind.Science, byId.getValue("p2").iconKind)
        assertEquals(ExamPaperIconKind.EditNote, byId.getValue("p3").iconKind)
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
