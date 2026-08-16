package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentCandidateRow
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetAssessmentCandidatesUseCase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AssessmentCandidatesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCandidates = mockk<GetAssessmentCandidatesUseCase>()
    private val savedState = SavedStateHandle(mapOf("scheduleId" to "PE-2024"))

    private fun viewModel() =
        AssessmentCandidatesViewModel(savedState, getCandidates)

    @Test
    fun `init maps aggregate scores onto the cards`() = runTest {
        coEvery { getCandidates("PE-2024", "") } returns listOf(
            row("c1", "Jane Doe", score = 82),
            row("c2", "John Smith", score = 40),
        )

        val state = viewModel().uiState.value

        assertEquals(2, state.totalCount)
        assertEquals(82, state.candidates[0].score)
        assertEquals(ScoreLevel.Normal, state.candidates[0].level)
        assertEquals(40, state.candidates[1].score)
        assertEquals(ScoreLevel.Low, state.candidates[1].level)
    }

    @Test
    fun `refresh re-reads scores after scoring elsewhere`() = runTest {
        coEvery { getCandidates("PE-2024", "") } returns listOf(row("c1", "Jane", score = 0))
        val vm = viewModel()
        assertEquals(0, vm.uiState.value.candidates.single().score)

        coEvery { getCandidates("PE-2024", "") } returns listOf(row("c1", "Jane", score = 16))
        vm.refresh()

        assertEquals(16, vm.uiState.value.candidates.single().score)
    }

    private fun row(
        id: String,
        fullName: String,
        score: Int = 72,
    ) = AssessmentCandidateRow(
        candidate = Candidate(id = id, examNumber = "EX-$id", fullName = fullName),
        aggregateScore = score,
        level = ng.com.chprbn.mobile.feature.assessment.domain.model.ScoreLevel.fromScore(score),
        scoredQuestions = 1,
        totalQuestions = 8,
        syncStatus = SyncStatus.Pending,
    )
}
