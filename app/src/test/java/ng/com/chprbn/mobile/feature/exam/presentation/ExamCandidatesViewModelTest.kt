package ng.com.chprbn.mobile.feature.exam.presentation

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceFilter
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamCandidateRow
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamCandidatesUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExamCandidatesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCandidates = mockk<GetExamCandidatesUseCase>()

    @Test
    fun `empty cohort keeps the placeholder candidates so the screen isn't blank`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns emptyList()

        val viewModel = ExamCandidatesViewModel(getCandidates)

        // Initial placeholder data, untouched.
        assertEquals(ExamCandidatesUiState.placeholder().candidates, viewModel.uiState.value.candidates)
    }

    @Test
    fun `populated cohort maps domain rows into Signed In or Pending pills`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns listOf(
            row("c1", "Jane Doe", AttendanceStatus.SignedIn),
            row("c2", "Bob Jones", attendance = null, remarkCount = 1),
            row("c3", "Mia Smith", AttendanceStatus.Flagged),
        )

        val viewModel = ExamCandidatesViewModel(getCandidates)

        val byName = viewModel.uiState.value.candidates.associateBy { it.name }
        assertEquals("Signed In", byName.getValue("Jane Doe").statusPillLabel)
        assertEquals("1 Remark", byName.getValue("Bob Jones").statusPillLabel)
        assertEquals("Flagged", byName.getValue("Mia Smith").statusPillLabel)
    }

    @Test
    fun `onFilterChange forwards the mapped AttendanceFilter to the use case`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns emptyList()
        val viewModel = ExamCandidatesViewModel(getCandidates)

        viewModel.onFilterChange("Flagged")

        // The filter label is reflected in state; the actual SQL-side
        // filter was forwarded as AttendanceFilter.Flagged (verified
        // indirectly via the use case being invoked).
        assertEquals("Flagged", viewModel.uiState.value.activeFilterLabel)
        io.mockk.coVerify {
            getCandidates(any(), AttendanceFilter.Flagged, any())
        }
    }

    @Test
    fun `placeholder filter labels include All and Flagged`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns emptyList()
        val state = ExamCandidatesViewModel(getCandidates).uiState.value

        assertTrue("All" in state.filterLabels)
        assertTrue("Flagged" in state.filterLabels)
        assertEquals("All", state.activeFilterLabel)
    }

    private fun row(
        id: String,
        fullName: String,
        attendance: AttendanceStatus? = null,
        remarkCount: Int = 0,
    ) = ExamCandidateRow(
        candidate = Candidate(id = id, examNumber = "EX-$id", fullName = fullName),
        attendance = attendance?.let {
            Attendance(
                paperId = "p1",
                candidateId = id,
                status = it,
                markedAt = 0L,
                syncStatus = SyncStatus.Pending,
            )
        },
        remarkCount = remarkCount,
    )
}
