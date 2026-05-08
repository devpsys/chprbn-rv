package ng.com.chprbn.mobile.feature.exam.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamCandidatesViewModelTest {

    @Test
    fun `initial state matches placeholder`() {
        val viewModel = ExamCandidatesViewModel()

        assertEquals(ExamCandidatesUiState.placeholder(), viewModel.uiState.value)
    }

    @Test
    fun `placeholder filter labels include All and a Flagged option`() {
        val state = ExamCandidatesViewModel().uiState.value

        assertTrue("All" in state.filterLabels)
        assertTrue("Flagged" in state.filterLabels)
        assertEquals("All", state.activeFilterLabel)
    }

    @Test
    fun `placeholder candidates each carry an avatar URL and ID label`() {
        val state = ExamCandidatesViewModel().uiState.value

        assertTrue(state.candidates.isNotEmpty())
        state.candidates.forEach { c ->
            assertTrue("avatar URL must not be blank for ${c.name}", c.avatarUrl.isNotBlank())
            assertTrue(
                "ID label must follow the EX-YYYY-NNNN convention but was '${c.idLabel}'",
                c.idLabel.startsWith("ID: EX-")
            )
        }
    }
}
