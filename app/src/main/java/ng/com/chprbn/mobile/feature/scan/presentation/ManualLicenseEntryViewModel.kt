package ng.com.chprbn.mobile.feature.scan.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ManualLicenseEntryUiState(
    val licenseNumber: String = ""
)

@HiltViewModel
class ManualLicenseEntryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ManualLicenseEntryUiState())
    val uiState: StateFlow<ManualLicenseEntryUiState> = _uiState.asStateFlow()

    fun onLicenseNumberChange(value: String) {
        _uiState.update { it.copy(licenseNumber = value) }
    }
}

