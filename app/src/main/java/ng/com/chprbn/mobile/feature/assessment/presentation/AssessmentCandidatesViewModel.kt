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
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentCandidateRow
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetAssessmentCandidatesUseCase
import javax.inject.Inject

/**
 * Filters happen SQL-side via [GetAssessmentCandidatesUseCase] — the query
 * parameter is forwarded directly. The `totalCount` header reflects the
 * **unfiltered** cohort size so it doesn't shrink as the user types; the
 * `candidates` list reflects the filter.
 */
@HiltViewModel
class AssessmentCandidatesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCandidates: GetAssessmentCandidatesUseCase,
) : ViewModel() {

    private val scheduleId: String = savedStateHandle.get<String>("scheduleId").orEmpty()

    private val _uiState = MutableStateFlow(AssessmentCandidatesUiState())
    val uiState: StateFlow<AssessmentCandidatesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val all = getCandidates(scheduleId)
            _uiState.update {
                it.copy(
                    totalCount = all.size,
                    candidates = all.map { row -> row.toCard() },
                )
            }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
        viewModelScope.launch {
            val filtered = getCandidates(scheduleId, value)
            _uiState.update { current ->
                // Drop the result if the query changed while the SQL was in flight.
                if (current.query != value) current
                else current.copy(candidates = filtered.map { it.toCard() })
            }
        }
    }

    fun onViewModeChange(mode: CandidatesViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    private fun AssessmentCandidateRow.toCard(): CandidateCardUiState = CandidateCardUiState(
        id = candidate.id,
        indexingNumber = candidate.examNumber,
        fullName = candidate.fullName,
        photoUrl = candidate.photoUrl,
        score = aggregateScore,
        level = when (level) {
            ng.com.chprbn.mobile.feature.assessment.domain.model.ScoreLevel.Normal -> ScoreLevel.Normal
            ng.com.chprbn.mobile.feature.assessment.domain.model.ScoreLevel.Low -> ScoreLevel.Low
        },
    )
}
