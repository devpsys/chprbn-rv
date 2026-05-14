package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.LookupAssessmentCandidateUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.RecordProjectScoreUseCase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AssessmentProjectAssessmentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lookup = mockk<LookupAssessmentCandidateUseCase>()
    private val recordProjectScore = mockk<RecordProjectScoreUseCase>(relaxed = true) {
        coEvery {
            this@mockk(any(), any(), any(), any())
        } returns SaveResult.Success
    }

    private val savedState = SavedStateHandle(
        mapOf("scheduleId" to "PE-2024", "candidateId" to "c1"),
    )

    @Test
    fun `init populates candidate profile`() = runTest {
        coEvery { lookup("PE-2024", "c1") } returns Candidate(
            id = "c1",
            examNumber = "EX-2024-0092",
            fullName = "Johnathan Doe",
            photoUrl = "https://x/1.jpg",
        )

        val vm = AssessmentProjectAssessmentViewModel(savedState, lookup, recordProjectScore)

        val state = vm.uiState.value
        assertEquals("Johnathan Doe", state.candidateName)
        assertEquals("EX-2024-0092", state.examId)
        assertEquals("https://x/1.jpg", state.photoUrl)
    }

    @Test
    fun `empty input clears the score text and skips persistence`() = runTest {
        coEvery { lookup(any(), any()) } returns Candidate("c1", "EX-1", "X")
        val vm = AssessmentProjectAssessmentViewModel(savedState, lookup, recordProjectScore)

        vm.onScoreChange("")

        assertEquals("", vm.uiState.value.scoreText)
        coVerify(exactly = 0) { recordProjectScore(any(), any(), any(), any()) }
    }

    @Test
    fun `parseable input persists immediately`() = runTest {
        coEvery { lookup(any(), any()) } returns Candidate("c1", "EX-1", "X")
        val vm = AssessmentProjectAssessmentViewModel(savedState, lookup, recordProjectScore)

        vm.onScoreChange("8")

        assertEquals("8", vm.uiState.value.scoreText)
        coVerify(exactly = 1) {
            recordProjectScore(
                scheduleId = "PE-2024",
                candidateId = "c1",
                score = 8.0,
                maxScore = 10,
            )
        }
    }

    @Test
    fun `trailing-decimal input persists as the integer Double`() = runTest {
        coEvery { lookup(any(), any()) } returns Candidate("c1", "EX-1", "X")
        val vm = AssessmentProjectAssessmentViewModel(savedState, lookup, recordProjectScore)

        // Java's Double.parseDouble accepts "8." as 8.0, so the value persists
        // even though the user is conceptually mid-entry. Acceptable: the next
        // keystroke (e.g. "8.5") REPLACES the prior write on a stable PK.
        vm.onScoreChange("8.")

        assertEquals("8.", vm.uiState.value.scoreText)
        coVerify(exactly = 1) {
            recordProjectScore(
                scheduleId = "PE-2024",
                candidateId = "c1",
                score = 8.0,
                maxScore = 10,
            )
        }
    }

    @Test
    fun `regex-rejecting input is dropped without state change`() = runTest {
        coEvery { lookup(any(), any()) } returns Candidate("c1", "EX-1", "X")
        val vm = AssessmentProjectAssessmentViewModel(savedState, lookup, recordProjectScore)

        vm.onScoreChange("8") // baseline
        vm.onScoreChange("8.55") // regex allows max one decimal digit → rejected

        assertEquals("rejected input must not overwrite previous", "8", vm.uiState.value.scoreText)
        coVerify(exactly = 1) { recordProjectScore(any(), any(), any(), any()) }
    }

    @Test
    fun `out-of-range input is dropped`() = runTest {
        coEvery { lookup(any(), any()) } returns Candidate("c1", "EX-1", "X")
        val vm = AssessmentProjectAssessmentViewModel(savedState, lookup, recordProjectScore)

        vm.onScoreChange("11") // > maxScore (10) → rejected

        assertEquals("", vm.uiState.value.scoreText)
        coVerify(exactly = 0) { recordProjectScore(any(), any(), any(), any()) }
    }
}
