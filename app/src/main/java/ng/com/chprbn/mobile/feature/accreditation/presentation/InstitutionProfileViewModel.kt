package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetInstitutionProfileResult
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.GetInstitutionProfileUseCase
import javax.inject.Inject

@HiltViewModel
class InstitutionProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProfile: GetInstitutionProfileUseCase,
) : ViewModel() {

    private val institutionId: String =
        savedStateHandle.get<String>("institutionId").orEmpty()

    private val _uiState = MutableStateFlow<InstitutionProfileUiState>(
        if (institutionId.isBlank()) {
            InstitutionProfileUiState.NotFound(institutionId)
        } else {
            InstitutionProfileUiState.Loading
        },
    )
    val uiState: StateFlow<InstitutionProfileUiState> = _uiState.asStateFlow()

    init {
        if (institutionId.isNotBlank()) {
            viewModelScope.launch {
                when (val result = getProfile(institutionId)) {
                    is GetInstitutionProfileResult.Success ->
                        _uiState.value = InstitutionProfileUiState.Success(result.profile.toUiData())
                    is GetInstitutionProfileResult.NotFound ->
                        _uiState.value = InstitutionProfileUiState.NotFound(result.institutionId)
                    is GetInstitutionProfileResult.Error ->
                        _uiState.value = InstitutionProfileUiState.Error(result.message)
                }
            }
        }
    }
}
