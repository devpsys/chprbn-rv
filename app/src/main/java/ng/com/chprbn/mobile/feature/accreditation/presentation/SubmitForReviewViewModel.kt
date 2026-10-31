package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Presentation-only confirm-and-submit VM. Simulates upload delay then
 * emits a placeholder [decisionId] for navigation to Assessment Result.
 * Real registry sync lands in Prompt I.
 */
@HiltViewModel
class SubmitForReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val institutionId: String =
        savedStateHandle.get<String>("institutionId").orEmpty()
    private val assessmentId: String =
        savedStateHandle.get<String>("assessmentId").orEmpty()

    private val _uiState = MutableStateFlow<SubmitForReviewUiState>(
        if (institutionId.isBlank()) {
            SubmitForReviewUiState.Error("Missing institution")
        } else {
            SubmitForReviewUiState.placeholder(
                institutionId = institutionId,
                assessmentId = assessmentId.ifBlank { "assess-$institutionId" },
            )
        },
    )
    val uiState: StateFlow<SubmitForReviewUiState> = _uiState.asStateFlow()

    fun onSubmitAssessment() {
        updateSuccess { it.copy(isSubmitting = true, showErrorDialog = false) }
        viewModelScope.launch {
            delay(1_200)
            val decisionId = "dec-${institutionId.ifBlank { "inst-abuth" }}-2024"
            updateSuccess {
                it.copy(
                    isSubmitting = false,
                    requestResultNavigation = true,
                    submittedDecisionId = decisionId,
                )
            }
        }
    }

    fun consumeResultNavigation() {
        updateSuccess { it.copy(requestResultNavigation = false) }
    }

    fun dismissErrorDialog() {
        updateSuccess { it.copy(showErrorDialog = false) }
    }

    fun retrySubmit() {
        dismissErrorDialog()
        onSubmitAssessment()
    }

    private fun updateSuccess(transform: (SubmitForReviewData) -> SubmitForReviewData) {
        _uiState.update { state ->
            val success = state as? SubmitForReviewUiState.Success ?: return@update state
            SubmitForReviewUiState.Success(transform(success.data))
        }
    }
}
