package ng.com.chprbn.mobile.feature.verification.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ManualEntryUiState(
    val licenseNumber: String = ""
)

@HiltViewModel
class ManualEntryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ManualEntryUiState())
    val uiState: StateFlow<ManualEntryUiState> = _uiState.asStateFlow()

    fun onLicenseNumberChange(value: String) {
        _uiState.update { it.copy(licenseNumber = value) }
    }
}

