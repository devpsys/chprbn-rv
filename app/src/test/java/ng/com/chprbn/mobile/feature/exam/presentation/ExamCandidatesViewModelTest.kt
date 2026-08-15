package ng.com.chprbn.mobile.feature.exam.presentation

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamCandidateRow
import ng.com.chprbn.mobile.feature.exam.domain.model.AddRemarkResult
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import ng.com.chprbn.mobile.feature.exam.domain.usecase.AddRemarkUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamCandidatesUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExamCandidatesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCandidates = mockk<GetExamCandidatesUseCase>()
    private val addRemark = mockk<AddRemarkUseCase>()
    private val savedState = SavedStateHandle(mapOf("paperId" to "p1"))
    private val context = mockk<Context> {
        every {
            getString(R.string.exam_candidates_remark_type_absenteeism)
        } returns "Absenteeism"
        every {
            getString(R.string.exam_candidates_remark_dialog_error_default)
        } returns "Unable to save remark. Please try again."
    }

    private fun viewModel() = ExamCandidatesViewModel(savedState, getCandidates, addRemark, context)

    @Test
    fun `reads paperId from the nav arg and forwards it to the use case`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns emptyList()

        viewModel()

        io.mockk.coVerify { getCandidates("p1", any(), any()) }
    }

    @Test
    fun `refresh re-runs the use case and picks up updated remark counts`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns listOf(
            row("c1", "Jane Doe", remarkCount = 2),
        )
        val viewModel = viewModel()
        assertEquals(2, viewModel.uiState.value.candidates.single().remarkCount)
        coEvery { getCandidates(any(), any(), any()) } returns listOf(
            row("c1", "Jane Doe", remarkCount = 0),
        )

        viewModel.refresh()

        assertEquals(0, viewModel.uiState.value.candidates.single().remarkCount)
        io.mockk.coVerify(exactly = 2) { getCandidates(any(), any(), any()) }
    }

    @Test
    fun `empty cohort surfaces a real empty roster, not the placeholder forever`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns emptyList()

        val viewModel = viewModel()

        assertEquals(emptyList<ExamCandidateUiState>(), viewModel.uiState.value.candidates)
    }

    @Test
    fun `populated cohort maps attendance to the pill and keeps remark count separate`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns listOf(
            row("c1", "Jane Doe", AttendanceStatus.SignedIn),
            row("c2", "Bob Jones", attendance = null, remarkCount = 1),
            row("c3", "Mia Smith", AttendanceStatus.Flagged),
        )

        val viewModel = viewModel()

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
    fun `populated cohort carries the real candidateId, not the display idLabel`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns listOf(row("c1", "Jane Doe"))

        val viewModel = viewModel()

        assertEquals("c1", viewModel.uiState.value.candidates.single().candidateId)
    }

    @Test
    fun `onFilterChange narrows the visible list client-side without re-querying the use case`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns listOf(
            row("c1", "Jane Doe", AttendanceStatus.SignedIn),
            row("c2", "Bob Jones", attendance = null),
            row("c3", "Mia Smith", AttendanceStatus.Flagged),
        )
        val viewModel = viewModel()

        viewModel.onFilterChange("Flagged")

        assertEquals("Flagged", viewModel.uiState.value.activeFilterLabel)
        val visibleNames = viewModel.uiState.value.candidates.map { it.name }
        assertEquals(listOf("Mia Smith"), visibleNames)
        // Regression: the pre-fix VM re-called the use case with an
        // AttendanceFilter and wiped the visible list when the DAO returned
        // empty. We now filter the in-memory source client-side, so the use
        // case is invoked exactly once (in init).
        io.mockk.coVerify(exactly = 1) { getCandidates(any(), any(), any()) }
    }

    @Test
    fun `onFilterChange All restores the full source after a narrowing filter`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns listOf(
            row("c1", "Jane Doe", AttendanceStatus.SignedIn),
            row("c2", "Bob Jones", AttendanceStatus.SignedOut),
        )
        val viewModel = viewModel()

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
        val viewModel = viewModel()

        viewModel.onQueryChange("JONES")

        assertEquals(listOf("Bob Jones"), viewModel.uiState.value.candidates.map { it.name })
    }

    @Test
    fun `placeholder filter labels include All and Flagged`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns emptyList()
        val state = viewModel().uiState.value

        assertTrue("All" in state.filterLabels)
        assertTrue("Flagged" in state.filterLabels)
        assertEquals("All", state.activeFilterLabel)
    }

    @Test
    fun `onAddRemarkClicked opens the dialog with the candidate's name and no selection`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns listOf(row("c1", "Jane Doe"))
        val viewModel = viewModel()

        viewModel.onAddRemarkClicked("c1")

        val state = viewModel.remarkDialogState.value
        assertTrue(state is AddRemarkUiState.Open)
        state as AddRemarkUiState.Open
        assertEquals("c1", state.candidateId)
        assertEquals("Jane Doe", state.candidateName)
        assertNull(state.selectedType)
    }

    @Test
    fun `onSelectRemarkType updates the open dialog's selection`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns listOf(row("c1", "Jane Doe"))
        val viewModel = viewModel()
        viewModel.onAddRemarkClicked("c1")

        viewModel.onSelectRemarkType(RemarkType.Absenteeism)

        val state = viewModel.remarkDialogState.value as AddRemarkUiState.Open
        assertEquals(RemarkType.Absenteeism, state.selectedType)
    }

    @Test
    fun `onDismissRemarkDialog closes the dialog`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns listOf(row("c1", "Jane Doe"))
        val viewModel = viewModel()
        viewModel.onAddRemarkClicked("c1")

        viewModel.onDismissRemarkDialog()

        assertEquals(AddRemarkUiState.Closed, viewModel.remarkDialogState.value)
    }

    @Test
    fun `onSaveRemark calls the use case with the resolved label and severity, then closes and bumps the count`() =
        runTest {
            coEvery { getCandidates(any(), any(), any()) } returns listOf(row("c1", "Jane Doe"))
            coEvery {
                addRemark("c1", "p1", "Absenteeism", RemarkType.Absenteeism.severity)
            } returns AddRemarkResult.Success(
                Remark(
                    id = "r1",
                    candidateId = "c1",
                    paperId = "p1",
                    body = "Absenteeism",
                    createdAt = 0L,
                ),
            )
            val viewModel = viewModel()
            viewModel.onAddRemarkClicked("c1")
            viewModel.onSelectRemarkType(RemarkType.Absenteeism)

            viewModel.onSaveRemark()

            assertEquals(AddRemarkUiState.Closed, viewModel.remarkDialogState.value)
            assertEquals(1, viewModel.uiState.value.candidates.single().remarkCount)
        }

    @Test
    fun `onSaveRemark surfaces the use case's error and keeps the dialog open for retry`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns listOf(row("c1", "Jane Doe"))
        coEvery { addRemark(any(), any(), any(), any()) } returns
            AddRemarkResult.Error("Remark cannot be empty.")
        val viewModel = viewModel()
        viewModel.onAddRemarkClicked("c1")
        viewModel.onSelectRemarkType(RemarkType.Absenteeism)

        viewModel.onSaveRemark()

        val state = viewModel.remarkDialogState.value as AddRemarkUiState.Open
        assertEquals("Remark cannot be empty.", state.errorMessage)
        assertEquals(false, state.isSaving)
        assertEquals(0, viewModel.uiState.value.candidates.single().remarkCount)
    }

    @Test
    fun `onSaveRemark without a selected type is a no-op`() = runTest {
        coEvery { getCandidates(any(), any(), any()) } returns listOf(row("c1", "Jane Doe"))
        val viewModel = viewModel()
        viewModel.onAddRemarkClicked("c1")

        viewModel.onSaveRemark()

        assertTrue(viewModel.remarkDialogState.value is AddRemarkUiState.Open)
        io.mockk.coVerify(exactly = 0) { addRemark(any(), any(), any(), any()) }
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
