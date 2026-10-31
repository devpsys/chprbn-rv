package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Presentation-only result VM. Grade, decision, period, and quotas are
 * treated as server-returned placeholders — never computed client-side.
 */
@HiltViewModel
class AssessmentResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val institutionId: String =
        savedStateHandle.get<String>("institutionId").orEmpty()
    private val decisionId: String =
        savedStateHandle.get<String>("decisionId").orEmpty()

    private val _uiState = MutableStateFlow<AssessmentResultUiState>(
        if (institutionId.isBlank()) {
            AssessmentResultUiState.Error("Missing institution")
        } else {
            AssessmentResultUiState.placeholder(
                institutionId = institutionId,
                decisionId = decisionId.ifBlank { "dec-$institutionId-2024" },
            )
        },
    )
    val uiState: StateFlow<AssessmentResultUiState> = _uiState.asStateFlow()

    fun onViewCertificate() {
        updateSuccess { it.copy(showCertificateDialog = true) }
    }

    fun dismissCertificateDialog() {
        updateSuccess { it.copy(showCertificateDialog = false) }
    }

    fun dismissErrorDialog() {
        updateSuccess { it.copy(showErrorDialog = false) }
    }

    fun retryLoad() {
        if (institutionId.isBlank()) {
            _uiState.value = AssessmentResultUiState.Error("Missing institution")
            return
        }
        _uiState.value = AssessmentResultUiState.placeholder(
            institutionId = institutionId,
            decisionId = decisionId.ifBlank { "dec-$institutionId-2024" },
        )
    }

    private fun updateSuccess(transform: (AssessmentResultData) -> AssessmentResultData) {
        _uiState.update { state ->
            val success = state as? AssessmentResultUiState.Success ?: return@update state
            AssessmentResultUiState.Success(transform(success.data))
        }
    }
}
