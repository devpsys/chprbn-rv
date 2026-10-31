package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Presentation-only overview VM. Reads [institutionId] from nav args and
 * emits [AssessmentOverviewUiState.placeholder] until a real lookup exists.
 */
@HiltViewModel
class AssessmentOverviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val institutionId: String =
        savedStateHandle.get<String>("institutionId").orEmpty()

    private val _uiState = MutableStateFlow<AssessmentOverviewUiState>(
        if (institutionId.isBlank()) {
            AssessmentOverviewUiState.Error("Missing institution")
        } else {
            AssessmentOverviewUiState.placeholder(institutionId)
        },
    )
    val uiState: StateFlow<AssessmentOverviewUiState> = _uiState.asStateFlow()

    /** Marks every SSQ section as saved locally so Continue to Checklist unlocks. */
    fun saveAllDrafts() {
        _uiState.update { state ->
            val success = state as? AssessmentOverviewUiState.Success ?: return@update state
            AssessmentOverviewUiState.Success(
                success.data.copy(
                    sections = success.data.sections.map { section ->
                        section.copy(status = SectionDraftStatus.SavedLocally)
                    },
                ),
            )
        }
    }
}
