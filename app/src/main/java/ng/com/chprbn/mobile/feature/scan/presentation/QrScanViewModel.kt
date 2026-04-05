package ng.com.chprbn.mobile.feature.scan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScanUiState(
    val isScanning: Boolean = false,
    val scanStatus: String = "Waiting for card...",
    val scanProgress: Float = 0.33f, // 1/3 progress
    val isTorchOn: Boolean = false,
    val scannedRegistrationNumber: String? = null
)

sealed class ScanUiEvent {
    object ToggleTorch : ScanUiEvent()
    object StartScan : ScanUiEvent()
    object StopScan : ScanUiEvent()
    object EnterManually : ScanUiEvent()
    data class RegistrationScanned(val registrationNumber: String?) : ScanUiEvent()
}

class QrScanViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun handleEvent(event: ScanUiEvent) {
        when (event) {
            is ScanUiEvent.ToggleTorch -> {
                _uiState.value = _uiState.value.copy(
                    isTorchOn = !_uiState.value.isTorchOn
                )
            }

            is ScanUiEvent.StartScan -> {
                _uiState.value = _uiState.value.copy(
                    isScanning = true,
                    scanStatus = "Scanning..."
                )
            }

            is ScanUiEvent.StopScan -> {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    scanStatus = "Waiting for card..."
                )
            }

            is ScanUiEvent.EnterManually -> {
                // Navigation handled by caller
            }

            is ScanUiEvent.RegistrationScanned -> {
                viewModelScope.launch {
                    if (event.registrationNumber == null) {
                        _uiState.value =
                            _uiState.value.copy(scannedRegistrationNumber = null)
                        return@launch
                    }
                    val cleaned = event.registrationNumber.trim()
                    if (cleaned.isNotBlank()) {
                        _uiState.value = _uiState.value.copy(
                            scannedRegistrationNumber = cleaned,
                            scanStatus = "Card detected!",
                            scanProgress = 1.0f
                        )
                    }
                }
            }
        }
    }
}

