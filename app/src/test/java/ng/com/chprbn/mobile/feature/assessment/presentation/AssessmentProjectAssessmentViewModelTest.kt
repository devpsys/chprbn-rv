package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetProjectScoreUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.LookupAssessmentCandidateUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.RecordProjectScoreUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AssessmentProjectAssessmentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lookup = mockk<LookupAssessmentCandidateUseCase>()
    private val getProjectScore = mockk<GetProjectScoreUseCase> {
        coEvery { this@mockk(any(), any()) } returns null
    }
    private val recordProjectScore = mockk<RecordProjectScoreUseCase>(relaxed = true) {
        coEvery {
            this@mockk(any(), any(), any(), any())
        } returns SaveResult.Success
    }

    private val savedState = SavedStateHandle(
        mapOf("scheduleId" to "PE-2024", "candidateId" to "c1"),
    )

    private fun makeVm(
        handle: SavedStateHandle = savedState,
    ) = AssessmentProjectAssessmentViewModel(
        handle, lookup, getProjectScore, recordProjectScore,
    )

    @Test
    fun `init populates candidate profile`() = runTest {
        coEvery { lookup("PE-2024", "c1") } returns Candidate(
            id = "c1",
            examNumber = "EX-2024-0092",
            fullName = "Johnathan Doe",
            photoUrl = "https://x/1.jpg",
        )

        val state = makeVm().uiState.value
        assertEquals("Johnathan Doe", state.candidateName)
        assertEquals("EX-2024-0092", state.examId)
        assertEquals("https://x/1.jpg", state.photoUrl)
        assertEquals("", state.scoreText)
    }

    @Test
    fun `init seeds the input from an existing project score`() = runTest {
        coEvery { lookup("PE-2024", "c1") } returns Candidate("c1", "EX-1", "X")
        coEvery { getProjectScore("PE-2024", "c1") } returns ProjectScore(
            scheduleId = "PE-2024",
            candidateId = "c1",
            score = 8.5,
            maxScore = 10,
            scoredAt = 1L,
            syncStatus = SyncStatus.Synced,
        )

        assertEquals("8.5", makeVm().uiState.value.scoreText)
        assertEquals(10, makeVm().uiState.value.maxScore)
    }

    @Test
    fun `init formats a whole-number existing score without a trailing decimal`() = runTest {
        coEvery { lookup("PE-2024", "c1") } returns Candidate("c1", "EX-1", "X")
        coEvery { getProjectScore("PE-2024", "c1") } returns ProjectScore(
            scheduleId = "PE-2024",
            candidateId = "c1",
            score = 8.0,
            maxScore = 10,
            scoredAt = 1L,
            syncStatus = SyncStatus.Pending,
        )

        assertEquals("8", makeVm().uiState.value.scoreText)
    }

    @Test
    fun `empty input clears the score text and skips persistence`() = runTest {
        coEvery { lookup(any(), any()) } returns Candidate("c1", "EX-1", "X")
        val vm = makeVm()

        vm.onScoreChange("8")
        vm.onScoreChange("")

        assertEquals("", vm.uiState.value.scoreText)
        coVerify(exactly = 0) { recordProjectScore(any(), any(), any(), any()) }
    }

    @Test
    fun `parseable input updates the field without persisting`() = runTest {
        coEvery { lookup(any(), any()) } returns Candidate("c1", "EX-1", "X")
        val vm = makeVm()

        vm.onScoreChange("8")

        assertEquals("8", vm.uiState.value.scoreText)
        coVerify(exactly = 0) { recordProjectScore(any(), any(), any(), any()) }
    }

    @Test
    fun `trailing-decimal input is kept locally`() = runTest {
        coEvery { lookup(any(), any()) } returns Candidate("c1", "EX-1", "X")
        val vm = makeVm()

        vm.onScoreChange("8.")

        assertEquals("8.", vm.uiState.value.scoreText)
        coVerify(exactly = 0) { recordProjectScore(any(), any(), any(), any()) }
    }

    @Test
    fun `regex-rejecting input is dropped without state change`() = runTest {
        coEvery { lookup(any(), any()) } returns Candidate("c1", "EX-1", "X")
        val vm = makeVm()

        vm.onScoreChange("8")
        vm.onScoreChange("8.55") // regex allows max one decimal digit → rejected

        assertEquals("rejected input must not overwrite previous", "8", vm.uiState.value.scoreText)
    }

    @Test
    fun `out-of-range input is dropped`() = runTest {
        coEvery { lookup(any(), any()) } returns Candidate("c1", "EX-1", "X")
        val vm = makeVm()

        vm.onScoreChange("11") // > maxScore (10) → rejected

        assertEquals("", vm.uiState.value.scoreText)
    }

    @Test
    fun `onSaveScore persists the parsed value then emits scoreSaved`() = runTest {
        coEvery { lookup(any(), any()) } returns Candidate("c1", "EX-1", "X")
        val vm = makeVm()
        vm.onScoreChange("8.5")

        var emitted = false
        val job = launch { vm.scoreSaved.collect { emitted = true } }
        advanceUntilIdle()

        vm.onSaveScore()
        advanceUntilIdle()

        assertTrue(emitted)
        assertTrue(vm.uiState.value.isSaving)
        coVerify(exactly = 1) {
            recordProjectScore(
                scheduleId = "PE-2024",
                candidateId = "c1",
                score = 8.5,
                maxScore = 10,
            )
        }
        job.cancel()
    }

    @Test
    fun `onSaveScore failure clears the spinner and does not emit`() = runTest {
        coEvery { lookup(any(), any()) } returns Candidate("c1", "EX-1", "X")
        coEvery { recordProjectScore(any(), any(), any(), any()) } returns
            SaveResult.Error("disk full")
        val vm = makeVm()
        vm.onScoreChange("8")

        var emitted = false
        val job = launch { vm.scoreSaved.collect { emitted = true } }
        advanceUntilIdle()

        vm.onSaveScore()
        advanceUntilIdle()

        assertFalse(emitted)
        assertFalse(vm.uiState.value.isSaving)
        job.cancel()
    }

    @Test
    fun `onSaveScore is a no-op when the field is empty`() = runTest {
        coEvery { lookup(any(), any()) } returns Candidate("c1", "EX-1", "X")
        val vm = makeVm()

        vm.onSaveScore()

        coVerify(exactly = 0) { recordProjectScore(any(), any(), any(), any()) }
        assertFalse(vm.uiState.value.isSaving)
    }

    @Test
    fun `unresolved scan payload never persists a score`() = runTest {
        val unresolvedState = SavedStateHandle(
            mapOf("scheduleId" to "PE-2024", "candidateId" to "UNKNOWN-EX"),
        )
        coEvery { lookup("PE-2024", "UNKNOWN-EX") } returns null

        val vm = makeVm(unresolvedState)
        vm.onScoreChange("8")
        vm.onSaveScore()

        coVerify(exactly = 0) { recordProjectScore(any(), any(), any(), any()) }
        coVerify(exactly = 0) { getProjectScore(any(), any()) }
    }
}
