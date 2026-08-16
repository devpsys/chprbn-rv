package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.SectionQuestion
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.CommitPracticalSectionUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetPracticalQuestionsUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetPracticalSectionsUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.LookupAssessmentCandidateUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.RecordPracticalScoreUseCase
import javax.inject.Inject

/**
 * Loads the section's questions + the candidate's current scores, and
 * persists every stepper tap via [RecordPracticalScoreUseCase]. The
 * domain use case validates the score range; the VM clamps locally for
 * the UI but the persistence call is authoritative.
 *
 * [onSaveScores] is the explicit "done with this section" gesture —
 * [CommitPracticalSectionUseCase] flags the pending rows for upload,
 * then [sectionSaved] fires so the Screen layer can pop back to the
 * practical-sections hub (which is already observing live summaries).
 *
 * No debouncing — `recordScore` upserts on a primary key, so rapid
 * `+ + + +` is a sequence of cheap REPLACEs. If profiling shows
 * contention later, add a `MutableSharedFlow.collectLatest` pipeline.
 */
@HiltViewModel
class AssessmentPracticalScoringViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lookupCandidate: LookupAssessmentCandidateUseCase,
    private val getQuestions: GetPracticalQuestionsUseCase,
    private val recordScore: RecordPracticalScoreUseCase,
    private val getSections: GetPracticalSectionsUseCase,
    private val commitSection: CommitPracticalSectionUseCase,
) : ViewModel() {

    private val scheduleId: String = savedStateHandle.get<String>("scheduleId").orEmpty()
    // Nav arg name is `candidateId`, actual value is the QR-extracted
    // registration/exam number — resolved to a domain id below so scores
    // aren't written under a bogus PK.
    private val scannedPayload: String = savedStateHandle.get<String>("candidateId").orEmpty()
    private val sectionId: String = savedStateHandle.get<String>("sectionId").orEmpty()

    /** Populated by init once the scanned payload resolves against the roster. */
    private var resolvedCandidateId: String? = null

    private val _uiState = MutableStateFlow(AssessmentPracticalScoringUiState())
    val uiState: StateFlow<AssessmentPracticalScoringUiState> = _uiState.asStateFlow()

    private val _sectionSaved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sectionSaved: SharedFlow<Unit> = _sectionSaved.asSharedFlow()

    init {
        viewModelScope.launch {
            val candidate = lookupCandidate(scheduleId, scannedPayload)
            if (candidate == null) {
                // Sections hub would have caught this too, but guard here
                // in case a deep-link lands us straight on scoring.
                return@launch
            }
            resolvedCandidateId = candidate.id
            val pairs = getQuestions(scheduleId, candidate.id, sectionId)
            val sectionTitle = getSections(scheduleId, candidate.id)
                .firstOrNull { it.section.id == sectionId }
                ?.section
                ?.title
                .orEmpty()
            _uiState.update {
                it.copy(
                    sectionTitle = sectionTitle,
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

    fun onSaveScores() {
        if (_uiState.value.isSaving) return
        val candidateId = resolvedCandidateId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (commitSection(scheduleId, candidateId, sectionId)) {
                SaveResult.Success -> _sectionSaved.emit(Unit)
                is SaveResult.Error -> _uiState.update { it.copy(isSaving = false) }
            }
        }
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
        // Only persist once we have a real DB id — writing under the raw
        // scan payload would corrupt the score table with orphan rows.
        val candidateId = resolvedCandidateId ?: return
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
}
