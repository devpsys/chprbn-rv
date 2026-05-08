package ng.com.chprbn.mobile.feature.exam.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CandidateScanResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CandidateScanResultUiState.fromScannedPayload(
            scannedPayload = savedStateHandle.get<String>("scannedPayload")
                ?.let(Uri::decode)
                .orEmpty(),
        ),
    )
    val uiState: StateFlow<CandidateScanResultUiState> = _uiState.asStateFlow()
}
