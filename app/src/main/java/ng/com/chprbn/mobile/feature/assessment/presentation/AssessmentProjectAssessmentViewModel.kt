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
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetProjectScoreUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.LookupAssessmentCandidateUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.RecordProjectScoreUseCase
import javax.inject.Inject

/**
 * Loads the candidate profile and any previously saved project score, then
 * holds edits locally until [onSaveScore]. Persistence is the Save FAB
 * gesture (not per-keystroke) so the spinner on that FAB has real work to
 * cover; [scoreSaved] fires on success so the Screen layer can pop back
 * to the practical-sections hub.
 *
 * Mid-entry strings like `"8."` stay in [AssessmentProjectAssessmentUiState.scoreText]
 * without being written. Range/precision rules live in
 * [RecordProjectScoreUseCase], so a misbehaving caller can't slip a bad
 * value past persistence.
 */
@HiltViewModel
class AssessmentProjectAssessmentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lookupCandidate: LookupAssessmentCandidateUseCase,
    private val getProjectScore: GetProjectScoreUseCase,
    private val recordProjectScore: RecordProjectScoreUseCase,
) : ViewModel() {

    private val scheduleId: String = savedStateHandle.get<String>("scheduleId").orEmpty()
    // Nav arg name is `candidateId`, actual value is the QR-extracted
    // registration/exam number.
    private val scannedPayload: String = savedStateHandle.get<String>("candidateId").orEmpty()

    /** Populated once the scanned payload resolves against the roster. */
    private var resolvedCandidateId: String? = null

    private val _uiState = MutableStateFlow(AssessmentProjectAssessmentUiState())
    val uiState: StateFlow<AssessmentProjectAssessmentUiState> = _uiState.asStateFlow()

    private val _scoreSaved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scoreSaved: SharedFlow<Unit> = _scoreSaved.asSharedFlow()

    init {
        viewModelScope.launch {
            val candidate = lookupCandidate(scheduleId, scannedPayload) ?: return@launch
            resolvedCandidateId = candidate.id
            val existing = getProjectScore(scheduleId, candidate.id)
            _uiState.update {
                it.copy(
                    candidateName = candidate.fullName,
                    examId = candidate.examNumber,
                    photoUrl = candidate.photoUrl,
                    scoreText = existing?.score?.let(::formatScoreForInput).orEmpty(),
                    maxScore = existing?.maxScore ?: it.maxScore,
                    // `role` and `verified` aren't part of the cross-feature
                    // Candidate shape yet — leave defaults until a richer
                    // profile lookup lands.
                )
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
        val max = _uiState.value.maxScore.toDouble()
        if (parsed == null || parsed in 0.0..max) {
            _uiState.update { it.copy(scoreText = text) }
        }
    }

    fun onSaveScore() {
        if (_uiState.value.isSaving) return
        val candidateId = resolvedCandidateId ?: return
        val parsed = _uiState.value.scoreText.toDoubleOrNull() ?: return
        val maxScore = _uiState.value.maxScore
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (
                recordProjectScore(
                    scheduleId = scheduleId,
                    candidateId = candidateId,
                    score = parsed,
                    maxScore = maxScore,
                )
            ) {
                SaveResult.Success -> _scoreSaved.emit(Unit)
                is SaveResult.Error -> _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private companion object {
        private val SCORE_PATTERN = Regex("^\\d{1,2}(\\.\\d?)?$")

        fun formatScoreForInput(score: Double): String {
            val asLong = score.toLong()
            return if (score == asLong.toDouble()) asLong.toString() else score.toString()
        }
    }
}
