package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
 * Loads the candidate profile and any previously saved project score,
 * then auto-saves on every valid keystroke with a short debounce.
 * Officers were regularly leaving the screen without tapping the Save
 * FAB, losing captured scores — the debounced write ensures the last
 * fully-parseable value is on disk within ~[AUTO_SAVE_DEBOUNCE_MS] of
 * the user pausing. Range/precision rules live in
 * [RecordProjectScoreUseCase], so a misbehaving caller can't slip a
 * bad value past persistence.
 *
 * The Save FAB stays as an explicit confirm gesture: it re-issues the
 * write (harmless — REPLACE is idempotent on the same PK) and emits
 * [scoreSaved] so the Screen layer can pop back to the
 * practical-sections hub.
 *
 * Mid-entry strings like `"8."` still land in
 * [AssessmentProjectAssessmentUiState.scoreText] because `"8."` parses
 * as `8.0` and is a valid write; the moment the user types `"8.5"` the
 * debounce cancel-and-restart replaces that intermediate write with
 * the final value.
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

    /**
     * The inflight auto-save. Cancelled and replaced on every new
     * keystroke so a fast typer's intermediate values (`"8"` → `"8."` →
     * `"8.5"`) never race the final write; only the last one lands.
     */
    private var autoSaveJob: Job? = null

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
            // Cancel any pending write — user cleared the field and
            // will type a new value. Their previously-persisted score
            // stays as-is (we never delete on clear; there's no UI
            // gesture for "erase this score").
            autoSaveJob?.cancel()
            _uiState.update { it.copy(scoreText = "") }
            return
        }
        if (!text.matches(SCORE_PATTERN)) return

        val parsed = text.toDoubleOrNull()
        val max = _uiState.value.maxScore.toDouble()
        if (parsed == null || parsed in 0.0..max) {
            _uiState.update { it.copy(scoreText = text) }
            if (parsed != null) scheduleAutoSave(parsed)
        }
    }

    /**
     * Debounces + cancel-previous so a burst of keystrokes results in
     * at most one persistence attempt for the final settled value.
     * Silent on the UI (no [AssessmentProjectAssessmentUiState.isSaving]
     * flip) — the loading spinner is reserved for the FAB gesture.
     */
    private fun scheduleAutoSave(score: Double) {
        val candidateId = resolvedCandidateId ?: return
        val maxScore = _uiState.value.maxScore
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(AUTO_SAVE_DEBOUNCE_MS)
            recordProjectScore(
                scheduleId = scheduleId,
                candidateId = candidateId,
                score = score,
                maxScore = maxScore,
            )
        }
    }

    fun onSaveScore() {
        if (_uiState.value.isSaving) return
        val candidateId = resolvedCandidateId ?: return
        val parsed = _uiState.value.scoreText.toDoubleOrNull() ?: return
        val maxScore = _uiState.value.maxScore
        // Preempt the debounced auto-save so we don't write twice for
        // the same value or race the FAB path. The explicit call below
        // is authoritative.
        autoSaveJob?.cancel()
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
        /**
         * Long enough to swallow a burst of keystrokes for a two-digit
         * score (`"8.5"` = three onScoreChange fires in <100ms typical),
         * short enough that a user pausing to look at the field sees
         * their work land almost instantly.
         */
        private const val AUTO_SAVE_DEBOUNCE_MS = 300L
        private val SCORE_PATTERN = Regex("^\\d{1,2}(\\.\\d?)?$")

        fun formatScoreForInput(score: Double): String {
            val asLong = score.toLong()
            return if (score == asLong.toDouble()) asLong.toString() else score.toString()
        }
    }
}
