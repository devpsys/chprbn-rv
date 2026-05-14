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
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.LookupAssessmentCandidateUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.RecordProjectScoreUseCase
import javax.inject.Inject

/**
 * Loads the candidate profile on init and auto-saves the score on every
 * keystroke that parses to a complete in-range Double. The "Save Score"
 * FAB remains a pure navigation gesture (handled by the Screen layer);
 * persistence has already happened by the time it's tapped.
 *
 * Trade-off of auto-save: typing "8.5" saves "8" first, then "8.5". The
 * intermediate "8." doesn't parse and is skipped. Acceptable — the final
 * value is what's persisted and the sync engine upserts on a stable PK.
 */
@HiltViewModel
class AssessmentProjectAssessmentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lookupCandidate: LookupAssessmentCandidateUseCase,
    private val recordProjectScore: RecordProjectScoreUseCase,
) : ViewModel() {

    private val scheduleId: String = savedStateHandle.get<String>("scheduleId").orEmpty()
    private val candidateId: String = savedStateHandle.get<String>("candidateId").orEmpty()

    private val _uiState = MutableStateFlow(AssessmentProjectAssessmentUiState())
    val uiState: StateFlow<AssessmentProjectAssessmentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val candidate = lookupCandidate(scheduleId, candidateId)
            if (candidate != null) {
                _uiState.update {
                    it.copy(
                        candidateName = candidate.fullName,
                        examId = candidate.examNumber,
                        photoUrl = candidate.photoUrl,
                        // `role` and `verified` aren't part of the cross-feature
                        // Candidate shape yet — leave defaults until a richer
                        // profile lookup lands.
                    )
                }
            }
        }
    }

    fun onScoreChange(text: String) {
        if (text.isEmpty()) {
            _uiState.update { it.copy(scoreText = "") }
            return
        }
        if (!text.matches(SCORE_PATTERN)) return

        val parsed = text.toDoubleOrNull()
        val state = _uiState.value
        if (parsed == null || parsed in 0.0..state.maxScore.toDouble()) {
            _uiState.update { it.copy(scoreText = text) }
            // Persist on every fully-parseable value. Range/precision rules
            // live in the use case, so a misbehaving caller can't slip a
            // bad value past persistence.
            if (parsed != null) {
                viewModelScope.launch {
                    recordProjectScore(
                        scheduleId = scheduleId,
                        candidateId = candidateId,
                        score = parsed,
                        maxScore = state.maxScore,
                    )
                }
            }
        }
    }

    private companion object {
        private val SCORE_PATTERN = Regex("^\\d{1,2}(\\.\\d?)?$")
    }
}
