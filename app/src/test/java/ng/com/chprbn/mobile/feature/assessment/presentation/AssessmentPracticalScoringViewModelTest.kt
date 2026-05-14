package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.SectionQuestion
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetPracticalQuestionsUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.RecordPracticalScoreUseCase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AssessmentPracticalScoringViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getQuestions = mockk<GetPracticalQuestionsUseCase>()
    private val recordScore = mockk<RecordPracticalScoreUseCase>(relaxed = true) {
        coEvery {
            this@mockk(any(), any(), any(), any(), any())
        } returns SaveResult.Success
    }

    private val savedState = SavedStateHandle(
        mapOf(
            "scheduleId" to "PE-2024",
            "candidateId" to "c1",
            "sectionId" to "PE-2024-sec-A",
        ),
    )

    @Test
    fun `init loads questions and seeds scores from existing PracticalScore rows`() = runTest {
        coEvery { getQuestions("PE-2024", "c1", "PE-2024-sec-A") } returns listOf(
            question("q1", number = 1, prompt = "Take BP") to null,
            question("q2", number = 2, prompt = "Take pulse") to existingScore(score = 7),
        )

        val vm = AssessmentPracticalScoringViewModel(savedState, getQuestions, recordScore)

        val state = vm.uiState.value
        assertEquals(2, state.questions.size)
        assertEquals(0, state.questions.first { it.id == "q1" }.score)
        assertEquals(7, state.questions.first { it.id == "q2" }.score)
    }

    @Test
    fun `onIncrement clamps at maxScore and calls recordScore exactly once per delta`() = runTest {
        coEvery { getQuestions(any(), any(), any()) } returns listOf(
            question("q1", number = 1, prompt = "x", maxScore = 2) to existingScore(score = 1),
        )
        val vm = AssessmentPracticalScoringViewModel(savedState, getQuestions, recordScore)

        vm.onIncrement("q1") // 1 → 2
        vm.onIncrement("q1") // clamped: stays at 2; no recordScore call

        assertEquals(2, vm.uiState.value.questions.single().score)
        coVerify(exactly = 1) {
            recordScore(
                scheduleId = "PE-2024",
                candidateId = "c1",
                questionId = "q1",
                score = 2,
                maxScore = 2,
            )
        }
    }

    @Test
    fun `onDecrement does not go below zero and skips persisting when value unchanged`() = runTest {
        coEvery { getQuestions(any(), any(), any()) } returns listOf(
            question("q1", number = 1, prompt = "x", maxScore = 10) to existingScore(score = 0),
        )
        val vm = AssessmentPracticalScoringViewModel(savedState, getQuestions, recordScore)

        vm.onDecrement("q1") // already 0, no-op

        assertEquals(0, vm.uiState.value.questions.single().score)
        coVerify(exactly = 0) { recordScore(any(), any(), any(), any(), any()) }
    }

    private fun question(
        id: String,
        number: Int,
        prompt: String,
        maxScore: Int = 10,
    ) = SectionQuestion(
        id = id,
        sectionId = "PE-2024-sec-A",
        number = number,
        prompt = prompt,
        imageUrl = null,
        maxScore = maxScore,
    )

    private fun existingScore(score: Int) =
        ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore(
            scheduleId = "PE-2024",
            candidateId = "c1",
            questionId = "q-stub",
            score = score,
            scoredAt = 0L,
        )
}
