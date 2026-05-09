package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Hardcoded directory of candidates for the assessment Paper Detail. Once
 * a real lookup lands, the VM will fetch by [scheduleId] and replace
 * `allCandidates` with the result. Search filters in-memory; view-mode
 * toggle is purely UI state.
 */
@HiltViewModel
class AssessmentCandidatesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Suppress("unused")
    private val scheduleId: String = savedStateHandle.get<String>("scheduleId").orEmpty()

    private val allCandidates: List<CandidateCardUiState> = listOf(
        CandidateCardUiState(
            id = "EX-2024-0092",
            indexingNumber = "EX-2024-0092",
            fullName = "Johnathan Doe",
            photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAAKlgS0gSpKaR2TvaNe3Wvuf2Qg08H48cEFKjJlLL8JxKcAoEr4FQX9_4ey9rNwWYDHMgYQI9rm-yPao7zVhdrWRozh2Ww2uOC9lXIdXy4q4B1FDeLlDykO0yojTNYFUkp9tMraJxeLgF4uAYmRChUjIcuVAQMrTzLRjx-DnRfWAeJf8Rh52i8YjEBo7NF2b1dvFSz1q6ElZQwSj094djczQGlbU7D_7b5L6VgBUPZyuvxQrDDcsZQ09qtZKxuJslkCMcyxFqz6iqX",
            score = 82,
            level = ScoreLevel.Normal,
        ),
        CandidateCardUiState(
            id = "EX-2024-0105",
            indexingNumber = "EX-2024-0105",
            fullName = "Sarah Jenkins",
            photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB4XjoGoyFxJoORd2Mvz-YY7GHlwCwGBYGyDhnfynY-DzKEJETb085yhPq6JirwcbGrYrtfWSBSFVQuiPrnCGkWNu5io9R5e7Bc3lp6a_pNvagR3nPMt4UkBAdEHejRzZdLxjtj2EHORXLLddIvI2WzF-sjvdtA9KnT3JycyFgmnaCQvkBvWLJVdV1EtQ0hqcGZVyCnELLvQBIt1CMoC_bJAWrPtx9j7ZYvNFvov8VObqHJIihudL4ZR6EI9IJpjkqIDT8fkVVK5vS7",
            score = 91,
            level = ScoreLevel.Normal,
        ),
        CandidateCardUiState(
            id = "EX-2024-0088",
            indexingNumber = "EX-2024-0088",
            fullName = "Michael Abiodun",
            photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBIbg5B2_YbbZmDRD7l_cr8F2olOCCyl3EoAHomR4eHMQAsgS7DziBKns-oXbB5adPGbolYeonqd4_uqlDdwqEQChWc9J2nhl_Y3iLvdm_4FLTVa1Et0nKb_FBqTsqk2leevj8WdG4HdAPBJHF2EKrCYMif1fZJ0D3ghhvBiKpRMAnhuXvve4LHslpSzuzObPGhSzUYh9UNpUpleCA_ICBCdTXkBAL-R6Slp_3vUmoXf8SmQWhtZhGRb-UPOEU8igUWxJYSMLuEGJxS",
            score = 45,
            // Threshold: scores under 50 read as a low/attention state and
            // render with the secondary (warning) colour band per the design.
            level = ScoreLevel.Low,
        ),
    )

    private val _uiState = MutableStateFlow(
        AssessmentCandidatesUiState(
            // Design's "150 Total" is the directory's total, not just the
            // visible 3 placeholders — keep it explicit so the header is
            // honest about the cohort size.
            totalCount = 150,
            query = "",
            viewMode = CandidatesViewMode.List,
            candidates = allCandidates,
        ),
    )
    val uiState: StateFlow<AssessmentCandidatesUiState> = _uiState.asStateFlow()

    fun onQueryChange(value: String) {
        val filtered = if (value.isBlank()) {
            allCandidates
        } else {
            allCandidates.filter { candidate ->
                candidate.fullName.contains(value, ignoreCase = true) ||
                    candidate.indexingNumber.contains(value, ignoreCase = true)
            }
        }
        _uiState.update { it.copy(query = value, candidates = filtered) }
    }

    fun onViewModeChange(mode: CandidatesViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }
}
