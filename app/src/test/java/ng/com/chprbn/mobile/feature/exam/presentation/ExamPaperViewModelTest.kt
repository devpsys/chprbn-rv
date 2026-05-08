package ng.com.chprbn.mobile.feature.exam.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamPaperViewModelTest {

    @Test
    fun `initial state matches placeholder`() {
        val viewModel = ExamPaperViewModel()

        assertEquals(ExamPaperUiState.placeholder(), viewModel.uiState.value)
    }

    @Test
    fun `placeholder hero image URL points at the exam paper hero asset`() {
        val state = ExamPaperViewModel().uiState.value

        assertEquals(EXAM_PAPER_HERO_IMAGE_URL, state.institutionHeroImageUrl)
    }

    @Test
    fun `placeholder progress fraction is between zero and one`() {
        val state = ExamPaperViewModel().uiState.value

        assertTrue(
            "Progress fraction must be a valid 0..1 ratio",
            state.attendanceProgressFraction in 0f..1f
        )
    }
}
