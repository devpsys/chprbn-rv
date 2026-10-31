package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetOverviewResult
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.GetAssessmentOverviewUseCase
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.SaveAllSectionDraftsUseCase
import javax.inject.Inject

@HiltViewModel
class AssessmentOverviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getOverview: GetAssessmentOverviewUseCase,
    private val saveAllSectionDrafts: SaveAllSectionDraftsUseCase,
) : ViewModel() {

    private val institutionId: String =
        savedStateHandle.get<String>("institutionId").orEmpty()

    private val _uiState = MutableStateFlow<AssessmentOverviewUiState>(
        if (institutionId.isBlank()) {
            AssessmentOverviewUiState.Error("Missing institution")
        } else {
            AssessmentOverviewUiState.Loading
        },
    )
    val uiState: StateFlow<AssessmentOverviewUiState> = _uiState.asStateFlow()

    init {
        if (institutionId.isNotBlank()) {
            viewModelScope.launch { reload() }
        }
    }

    fun saveAllDrafts() {
        if (institutionId.isBlank()) return
        viewModelScope.launch {
            saveAllSectionDrafts(institutionId)
            reload()
        }
    }

    private suspend fun reload() {
        when (val result = getOverview(institutionId)) {
            is GetOverviewResult.Success ->
                _uiState.value = AssessmentOverviewUiState.Success(result.overview.toUiData())
            is GetOverviewResult.Error ->
                _uiState.value = AssessmentOverviewUiState.Error(result.message)
        }
    }
}
