package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SyncDraftsResult
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.GetAccreditationSyncStatusUseCase
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.RetryFailedAccreditationSyncUseCase

@HiltViewModel
class SyncStatusViewModel @Inject constructor(
    private val getSyncStatus: GetAccreditationSyncStatusUseCase,
    private val retryFailed: RetryFailedAccreditationSyncUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncStatusUiState(isLoading = true))
    val uiState: StateFlow<SyncStatusUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { reload() }
    }

    fun onRetryDraft(draftId: String) {
        viewModelScope.launch {
            retryFailed()
            reload()
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
            val result = retryFailed()
            reload()
            _uiState.update { state ->
                state.copy(
                    isRetrying = false,
                    showRetrySuccess = result is SyncDraftsResult.Success,
                )
            }
        }
    }

    fun dismissRetrySuccess() {
        _uiState.update { it.copy(showRetrySuccess = false) }
    }

    private suspend fun reload() {
        val snapshot = getSyncStatus()
        _uiState.value = snapshot.toUiState()
    }
}
