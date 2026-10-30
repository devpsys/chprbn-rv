package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Presentation-only hub VM. Emits [AccreditationHubUiState.placeholder]
 * until a GetAssignedInstitutions (or equivalent) use case exists.
 *
 * [onQuickSync] / feature-tile actions are intentional no-ops here —
 * navigation is owned by the Screen/NavHost callbacks.
 */
@HiltViewModel
class AccreditationHubViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AccreditationHubUiState.placeholder())
    val uiState: StateFlow<AccreditationHubUiState> = _uiState.asStateFlow()

    /** First active assigned institution, if any — drives Start New Assessment. */
    fun firstActiveInstitutionId(): String? =
        _uiState.value.assignedInstitutions.firstOrNull { it.isActive }?.id
}
