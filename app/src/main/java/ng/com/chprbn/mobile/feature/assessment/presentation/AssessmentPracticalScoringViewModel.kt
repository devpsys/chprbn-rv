package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.SectionQuestion
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetPracticalQuestionsUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.RecordPracticalScoreUseCase
import javax.inject.Inject

/**
 * Loads the section's questions + the candidate's current scores, and
 * persists every stepper tap via [RecordPracticalScoreUseCase]. The
 * domain use case validates the score range; the VM clamps locally for
 * the UI but the persistence call is authoritative.
 *
 * No debouncing — `recordScore` upserts on a primary key, so rapid
 * `+ + + +` is a sequence of cheap REPLACEs. If profiling shows
 * contention later, add a `MutableSharedFlow.collectLatest` pipeline.
 */
@HiltViewModel
class AssessmentPracticalScoringViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getQuestions: GetPracticalQuestionsUseCase,
    private val recordScore: RecordPracticalScoreUseCase,
) : ViewModel() {

    private val scheduleId: String = savedStateHandle.get<String>("scheduleId").orEmpty()
    private val candidateId: String = savedStateHandle.get<String>("candidateId").orEmpty()
    private val sectionId: String = savedStateHandle.get<String>("sectionId").orEmpty()

    private val _uiState = MutableStateFlow(AssessmentPracticalScoringUiState())
    val uiState: StateFlow<AssessmentPracticalScoringUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val pairs = getQuestions(scheduleId, candidateId, sectionId)
            _uiState.update {
                it.copy(
                    sectionTitle = sectionTitleFor(sectionId),
                    questions = pairs.map { (q, existing) -> q.toScoreUi(existing) },
                )
            }
        }
    }

    fun onIncrement(questionId: String) {
        adjustScore(questionId, +1)
    }

    fun onDecrement(questionId: String) {
        adjustScore(questionId, -1)
    }

    private fun adjustScore(questionId: String, delta: Int) {
        val before = _uiState.value.questions.firstOrNull { it.id == questionId } ?: return
        val nextScore = (before.score + delta).coerceIn(0, before.maxScore)
        if (nextScore == before.score) return

        _uiState.update { state ->
            state.copy(
                questions = state.questions.map { q ->
                    if (q.id == questionId) q.copy(score = nextScore) else q
                },
            )
        }
        viewModelScope.launch {
            recordScore(
                scheduleId = scheduleId,
                candidateId = candidateId,
                questionId = questionId,
                score = nextScore,
                maxScore = before.maxScore,
            )
        }
    }

    private fun SectionQuestion.toScoreUi(existing: PracticalScore?): ScoreQuestionUiState =
        ScoreQuestionUiState(
            id = id,
            number = number,
            prompt = prompt,
            imageUrl = imageUrl,
            maxScore = maxScore,
            score = existing?.score ?: 0,
        )

    // Section titles aren't carried by the question payload; the title comes
    // from the parent PracticalSection. Since the screen only knows
    // `sectionId`, derive a sensible label from the id letter until a richer
    // lookup is wired.
    private fun sectionTitleFor(id: String): String {
        val letter = id.substringAfterLast("-").uppercase()
        return when (letter) {
            "A" -> "Section A — Patient Assessment"
            "B" -> "Section B — Clinical Diagnosis"
            "C" -> "Section C — Ethical Standards"
            else -> "Section $letter"
        }
    }
}
