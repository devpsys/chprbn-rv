package ng.com.chprbn.mobile.feature.exam.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CandidateScanResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CandidateScanResultUiState.fromScannedPayload(
            scannedPayload = savedStateHandle.get<String>("scannedPayload")
                ?.let(Uri::decode)
                .orEmpty(),
            context = context,
        ),
    )
    val uiState: StateFlow<CandidateScanResultUiState> = _uiState.asStateFlow()
}
