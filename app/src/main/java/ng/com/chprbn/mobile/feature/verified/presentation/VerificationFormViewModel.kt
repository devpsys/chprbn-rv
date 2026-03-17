package ng.com.chprbn.mobile.feature.verified.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class VerificationFormUiState(
    val verificationLocation: String = "",
    val practitionerPresent: Boolean = true,
    val officerRemarks: String = ""
)

@HiltViewModel
class VerificationFormViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(VerificationFormUiState())
    val uiState: StateFlow<VerificationFormUiState> = _uiState.asStateFlow()

    fun onVerificationLocationChange(value: String) {
        _uiState.update { it.copy(verificationLocation = value) }
    }

    fun onPractitionerPresentChange(value: Boolean) {
        _uiState.update { it.copy(practitionerPresent = value) }
    }

    fun onOfficerRemarksChange(value: String) {
        _uiState.update { it.copy(officerRemarks = value) }
    }

    fun saveVerification() {
        // TODO: domain/data layer — submit verification
    }
}
