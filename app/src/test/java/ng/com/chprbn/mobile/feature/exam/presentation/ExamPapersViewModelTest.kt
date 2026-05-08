package ng.com.chprbn.mobile.feature.exam.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regression tests for the placeholder state shape. The ViewModel will gain
 * real data sources once the attendance API lands; these tests pin the current
 * stub so accidental UI-layer changes don't silently alter the screen.
 */
class ExamPapersViewModelTest {

    @Test
    fun `initial state matches placeholder`() {
        val viewModel = ExamPapersViewModel()

        assertEquals(ExamPapersUiState.placeholder(), viewModel.uiState.value)
    }

    @Test
    fun `placeholder lists three papers covering all attendance statuses`() {
        val state = ExamPapersViewModel().uiState.value

        assertEquals(3, state.papers.size)
        val statuses = state.papers.map { it.status }.toSet()
        assertEquals(
            setOf(
                ExamPaperAttendanceStatus.Completed,
                ExamPaperAttendanceStatus.Active,
                ExamPaperAttendanceStatus.Upcoming
            ),
            statuses
        )
    }

    @Test
    fun `only the Active paper exposes a Mark Attendance primary action`() {
        val state = ExamPapersViewModel().uiState.value

        val active = state.papers.single { it.status == ExamPaperAttendanceStatus.Active }
        assertEquals("Mark Attendance", active.primaryActionLabel)

        val others = state.papers.filter { it.status != ExamPaperAttendanceStatus.Active }
        others.forEach { assertEquals(null, it.primaryActionLabel) }
    }
}
