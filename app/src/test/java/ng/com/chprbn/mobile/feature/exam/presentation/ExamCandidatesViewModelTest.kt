package ng.com.chprbn.mobile.feature.exam.presentation

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
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
    fun `populated cohort maps attendance to the pill and keeps remark count separate`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns listOf(
            row("c1", "Jane Doe", AttendanceStatus.SignedIn),
            row("c2", "Bob Jones", attendance = null, remarkCount = 1),
            row("c3", "Mia Smith", AttendanceStatus.Flagged),
        )

        val viewModel = ExamCandidatesViewModel(getCandidates)

        val byName = viewModel.uiState.value.candidates.associateBy { it.name }
        // The pill is now a pure attendance signal — Bob has no attendance,
        // so the pill is "Pending" regardless of his remark count. The
        // "N Remark" state renders on the card button, driven by
        // [ExamCandidateUiState.remarkCount].
        assertEquals("Signed In", byName.getValue("Jane Doe").statusPillLabel)
        assertEquals("Pending", byName.getValue("Bob Jones").statusPillLabel)
        assertEquals(1, byName.getValue("Bob Jones").remarkCount)
        assertEquals("Flagged", byName.getValue("Mia Smith").statusPillLabel)
    }

    @Test
    fun `onFilterChange narrows the visible list client-side without re-querying the use case`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns listOf(
            row("c1", "Jane Doe", AttendanceStatus.SignedIn),
            row("c2", "Bob Jones", attendance = null),
            row("c3", "Mia Smith", AttendanceStatus.Flagged),
        )
        val viewModel = ExamCandidatesViewModel(getCandidates)

        viewModel.onFilterChange("Flagged")

        assertEquals("Flagged", viewModel.uiState.value.activeFilterLabel)
        val visibleNames = viewModel.uiState.value.candidates.map { it.name }
        assertEquals(listOf("Mia Smith"), visibleNames)
        // Regression: the pre-fix VM re-called the use case with an
        // AttendanceFilter and wiped the visible list when the DAO returned
        // empty (E10 audit — paperId is blank until OfficerSession wires it
        // up). We now filter the in-memory source client-side, so the use
        // case is invoked exactly once (in init).
        io.mockk.coVerify(exactly = 1) { getCandidates(any(), any(), any()) }
    }

    @Test
    fun `onFilterChange All restores the full source after a narrowing filter`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns listOf(
            row("c1", "Jane Doe", AttendanceStatus.SignedIn),
            row("c2", "Bob Jones", AttendanceStatus.SignedOut),
        )
        val viewModel = ExamCandidatesViewModel(getCandidates)

        viewModel.onFilterChange("Signed In")
        assertEquals(1, viewModel.uiState.value.candidates.size)

        viewModel.onFilterChange("All")

        assertEquals(2, viewModel.uiState.value.candidates.size)
    }

    @Test
    fun `onQueryChange matches name and idLabel case-insensitively`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns listOf(
            row("c1", "Jane Doe", AttendanceStatus.SignedIn),
            row("c2", "Bob Jones", AttendanceStatus.SignedIn),
        )
        val viewModel = ExamCandidatesViewModel(getCandidates)

        viewModel.onQueryChange("JONES")

        assertEquals(listOf("Bob Jones"), viewModel.uiState.value.candidates.map { it.name })
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
