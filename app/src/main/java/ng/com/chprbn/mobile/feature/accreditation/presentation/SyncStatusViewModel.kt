package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Presentation-only Sync Status VM. Placeholder failed rows prove
 * [ng.com.chprbn.mobile.core.network.stripHostsAndUrls] sanitisation
 * until accreditation SyncBatchRunner jobs exist.
 */
@HiltViewModel
class SyncStatusViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SyncStatusUiState.placeholder())
    val uiState: StateFlow<SyncStatusUiState> = _uiState.asStateFlow()

    fun onRetryDraft(draftId: String) {
        _uiState.update { state ->
            val remaining = state.failedDrafts.filterNot { it.id == draftId }
            state.copy(
                failedDrafts = remaining,
                overallStatus = if (remaining.isEmpty()) {
                    OverallSyncStatusUi.Synced
                } else {
                    OverallSyncStatusUi.SyncFailed
                },
                progressFraction = if (remaining.isEmpty()) 1f else state.progressFraction,
            )
        }
    }

    fun onRetryFailed() {
        if (_uiState.value.isRetrying || _uiState.value.failedDrafts.isEmpty()) return
        _uiState.update {
            it.copy(
                isRetrying = true,
                showRetrySuccess = false,
                overallStatus = OverallSyncStatusUi.Syncing,
            )
        }
        viewModelScope.launch {
            delay(1_200)
            _uiState.update {
                it.copy(
                    isRetrying = false,
                    failedDrafts = emptyList(),
                    overallStatus = OverallSyncStatusUi.Synced,
                    progressFraction = 1f,
                    showRetrySuccess = true,
                    lastSuccessfulSyncLabel = "Just now",
                )
            }
        }
    }

    fun dismissRetrySuccess() {
        _uiState.update { it.copy(showRetrySuccess = false) }
    }
}
