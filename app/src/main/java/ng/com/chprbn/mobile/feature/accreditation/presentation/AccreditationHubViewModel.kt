package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetHubResult
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.GetAccreditationHubUseCase
import javax.inject.Inject

@HiltViewModel
class AccreditationHubViewModel @Inject constructor(
    private val getHub: GetAccreditationHubUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AccreditationHubUiState.placeholder().copy(isLoading = true),
    )
    val uiState: StateFlow<AccreditationHubUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = getHub()) {
                is GetHubResult.Success -> _uiState.value = result.data.toUiState()
                is GetHubResult.Error -> _uiState.value = AccreditationHubUiState.placeholder().copy(
                    isLoading = false,
                    errorMessage = result.message,
                )
            }
        }
    }

    /** First active assigned institution, if any — drives Start New Assessment. */
    fun firstActiveInstitutionId(): String? =
        _uiState.value.assignedInstitutions.firstOrNull { it.isActive }?.id

    companion object {
        /** Preview / test factory — does not hit repositories. */
        fun placeholder(): AccreditationHubUiState = AccreditationHubUiState.placeholder()
    }
}
