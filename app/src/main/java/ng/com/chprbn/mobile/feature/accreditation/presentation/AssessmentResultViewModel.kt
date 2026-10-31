package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetDecisionResult
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.GetAssessmentDecisionUseCase

@HiltViewModel
class AssessmentResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDecision: GetAssessmentDecisionUseCase,
) : ViewModel() {

    private val institutionId: String =
        savedStateHandle.get<String>("institutionId").orEmpty()
    private val decisionId: String =
        savedStateHandle.get<String>("decisionId").orEmpty()

    private val _uiState = MutableStateFlow<AssessmentResultUiState>(
        if (institutionId.isBlank()) {
            AssessmentResultUiState.Error("Missing institution")
        } else {
            AssessmentResultUiState.Loading
        },
    )
    val uiState: StateFlow<AssessmentResultUiState> = _uiState.asStateFlow()

    init {
        if (institutionId.isNotBlank()) {
            viewModelScope.launch { load() }
        }
    }

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
        _uiState.value = AssessmentResultUiState.Loading
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val id = decisionId.ifBlank { "dec-$institutionId-2024" }
        when (val result = getDecision(id)) {
            is GetDecisionResult.Success ->
                _uiState.value = AssessmentResultUiState.Success(result.decision.toUiData())
            is GetDecisionResult.NotFound ->
                _uiState.value = AssessmentResultUiState.Error("Decision not found")
            is GetDecisionResult.Error ->
                _uiState.value = AssessmentResultUiState.Error(result.message)
        }
    }

    private fun updateSuccess(transform: (AssessmentResultData) -> AssessmentResultData) {
        _uiState.update { state ->
            val success = state as? AssessmentResultUiState.Success ?: return@update state
            AssessmentResultUiState.Success(transform(success.data))
        }
    }
}
