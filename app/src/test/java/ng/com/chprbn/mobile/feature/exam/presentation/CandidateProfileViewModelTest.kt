package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity
import ng.com.chprbn.mobile.feature.exam.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.exam.domain.usecase.ClearRemarksForCandidateUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetAttendanceStatusUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetCandidateByIdUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetRemarksForCandidateUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CandidateProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCandidateById = mockk<GetCandidateByIdUseCase>()
    private val getRemarks = mockk<GetRemarksForCandidateUseCase>()
    private val getAttendanceStatus = mockk<GetAttendanceStatusUseCase>().also {
        coEvery { it(any(), any()) } returns null
    }
    private val clearRemarks = mockk<ClearRemarksForCandidateUseCase>()
    private val savedState = SavedStateHandle(mapOf("candidateId" to "c1", "paperId" to "p1"))

    private fun viewModel() = CandidateProfileViewModel(
        savedState,
        getCandidateById,
        getRemarks,
        getAttendanceStatus,
        clearRemarks,
    )

    @Test
    fun `loads candidate identity and maps remarks newest-first as provided by the use case`() = runTest {
        coEvery { getCandidateById("c1") } returns Candidate(
            id = "c1",
            examNumber = "EX-1",
            fullName = "Jane Doe",
        )
        coEvery { getRemarks("c1") } returns listOf(
            Remark(
                id = "r1",
                candidateId = "c1",
                body = "Absenteeism",
                severity = RemarkSeverity.Info,
                createdAt = 1_700_000_000_000L,
                syncStatus = SyncStatus.Pending,
            ),
        )

        val viewModel = viewModel()

        val state = viewModel.uiState.value
        assertTrue(state.hasLoaded)
        assertEquals(false, state.notFound)
        assertEquals("Jane Doe", state.candidateName)
        assertEquals("ID: EX-1", state.examNumberLabel)
        assertEquals(1, state.remarks.size)
        assertEquals("Absenteeism", state.remarks.single().body)
        assertEquals(RemarkSeverity.Info, state.remarks.single().severity)
        assertTrue(state.remarks.single().createdAtLabel.isNotBlank())
    }

    @Test
    fun `defaults to Pending when the candidate has no attendance row for the paper`() = runTest {
        coEvery { getCandidateById("c1") } returns Candidate("c1", "EX-1", "Jane Doe")
        coEvery { getRemarks("c1") } returns emptyList()

        val viewModel = viewModel()

        assertEquals("Pending", viewModel.uiState.value.statusPillLabel)
        coVerify(exactly = 1) { getAttendanceStatus("p1", "c1") }
    }

    @Test
    fun `maps each AttendanceStatus to the roster's pill vocabulary`() = runTest {
        coEvery { getCandidateById("c1") } returns Candidate("c1", "EX-1", "Jane Doe")
        coEvery { getRemarks("c1") } returns emptyList()
        coEvery { getAttendanceStatus("p1", "c1") } returns AttendanceStatus.SignedIn

        val viewModel = viewModel()

        assertEquals("Signed In", viewModel.uiState.value.statusPillLabel)
    }

    @Test
    fun `unresolved candidateId surfaces notFound instead of the placeholder forever`() = runTest {
        coEvery { getCandidateById("c1") } returns null

        val viewModel = viewModel()

        val state = viewModel.uiState.value
        assertTrue(state.hasLoaded)
        assertTrue(state.notFound)
        assertEquals(emptyList<RemarkRowUiState>(), state.remarks)
        coVerify(exactly = 0) { getRemarks(any()) }
        coVerify(exactly = 0) { getAttendanceStatus(any(), any()) }
    }

    @Test
    fun `onClearAllClicked shows the warning`() = runTest {
        coEvery { getCandidateById("c1") } returns Candidate("c1", "EX-1", "Jane Doe")
        coEvery { getRemarks("c1") } returns emptyList()
        val viewModel = viewModel()

        viewModel.onClearAllClicked()

        assertEquals(ClearRemarksUiState.WarningShown, viewModel.clearRemarksState.value)
    }

    @Test
    fun `onClearAllConfirmed clears, refreshes, and reports Success`() = runTest {
        coEvery { getCandidateById("c1") } returns Candidate("c1", "EX-1", "Jane Doe")
        coEvery { getRemarks("c1") } returns emptyList()
        coEvery { clearRemarks("c1") } returns SaveResult.Success
        val viewModel = viewModel()
        viewModel.onClearAllClicked()

        viewModel.onClearAllConfirmed()

        assertEquals(ClearRemarksUiState.Success, viewModel.clearRemarksState.value)
        coVerify(exactly = 1) { clearRemarks("c1") }
        // refresh() re-runs both use cases: once at init, once after clearing.
        coVerify(exactly = 2) { getCandidateById("c1") }
    }

    @Test
    fun `onClearAllConfirmed surfaces the use case's error`() = runTest {
        coEvery { getCandidateById("c1") } returns Candidate("c1", "EX-1", "Jane Doe")
        coEvery { getRemarks("c1") } returns emptyList()
        coEvery { clearRemarks("c1") } returns SaveResult.Error("disk full")
        val viewModel = viewModel()
        viewModel.onClearAllClicked()

        viewModel.onClearAllConfirmed()

        val state = viewModel.clearRemarksState.value
        assertTrue(state is ClearRemarksUiState.Error)
        assertEquals("disk full", (state as ClearRemarksUiState.Error).message)
    }

    @Test
    fun `onClearAllDismissed resets to Idle`() = runTest {
        coEvery { getCandidateById("c1") } returns Candidate("c1", "EX-1", "Jane Doe")
        coEvery { getRemarks("c1") } returns emptyList()
        val viewModel = viewModel()
        viewModel.onClearAllClicked()

        viewModel.onClearAllDismissed()

        assertEquals(ClearRemarksUiState.Idle, viewModel.clearRemarksState.value)
    }
}
