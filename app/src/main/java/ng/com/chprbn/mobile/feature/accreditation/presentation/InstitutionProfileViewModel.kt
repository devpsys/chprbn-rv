package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Presentation-only profile VM. Reads [institutionId] from nav args and
 * emits [InstitutionProfileUiState.placeholder] until a real lookup use
 * case exists. Blank id → [NotFound].
 */
@HiltViewModel
class InstitutionProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val institutionId: String =
        savedStateHandle.get<String>("institutionId").orEmpty()

    private val _uiState = MutableStateFlow<InstitutionProfileUiState>(
        if (institutionId.isBlank()) {
            InstitutionProfileUiState.NotFound(institutionId)
        } else {
            InstitutionProfileUiState.placeholder(institutionId)
        },
    )
    val uiState: StateFlow<InstitutionProfileUiState> = _uiState.asStateFlow()
}
