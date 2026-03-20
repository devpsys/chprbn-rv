package ng.com.chprbn.mobile.feature.sync.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.sync.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.feature.sync.domain.model.SyncRecord
import ng.com.chprbn.mobile.feature.sync.domain.usecase.GetSyncRecordsUseCase
import ng.com.chprbn.mobile.feature.sync.domain.usecase.RetryFailedSyncUseCase
import ng.com.chprbn.mobile.feature.sync.domain.usecase.SyncAllRecordsUseCase
import ng.com.chprbn.mobile.feature.verified.domain.model.SyncStatus

data class SyncUiState(
    val records: List<SyncRecord> = emptyList(),
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val error: String? = null,
    /** Short user-facing summary of the last batch operation (sync all / retry). */
    val lastBatchSummary: String? = null
) {
    val total: Int get() = records.size
    val syncedCount: Int get() = records.count { it.syncStatus == SyncStatus.Synced }
    val pendingCount: Int get() = records.count { it.syncStatus == SyncStatus.Pending }
    val failedCount: Int get() = records.count { it.syncStatus == SyncStatus.Failed }

    val syncProgress: Float
        get() = if (total == 0) 0f else syncedCount.toFloat() / total.toFloat()

    val lastSuccessfulSyncMillis: Long?
        get() = records
            .filter { it.syncStatus == SyncStatus.Synced }
            .mapNotNull { it.lastSyncAttempt }
            .maxOrNull()
}

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val getSyncRecordsUseCase: GetSyncRecordsUseCase,
    private val syncAllRecordsUseCase: SyncAllRecordsUseCase,
    private val retryFailedSyncUseCase: RetryFailedSyncUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    private val timeFormat =
        SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            reloadFromDb()
        }
    }

    fun syncAll() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSyncing = true, error = null, lastBatchSummary = null)
            }
            runCatching { syncAllRecordsUseCase() }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            lastBatchSummary = formatBatchSummary("Sync all", result)
                        )
                    }
                    reloadFromDb()
                }
                .onFailure { t ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            error = t.message ?: "Sync failed."
                        )
                    }
                    reloadFromDb()
                }
        }
    }

    fun retryFailed() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSyncing = true, error = null, lastBatchSummary = null)
            }
            runCatching { retryFailedSyncUseCase() }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            lastBatchSummary = formatBatchSummary("Retry failed", result)
                        )
                    }
                    reloadFromDb()
                }
                .onFailure { t ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            error = t.message ?: "Retry failed."
                        )
                    }
                    reloadFromDb()
                }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }

    private suspend fun reloadFromDb() {
        runCatching { getSyncRecordsUseCase() }
            .onSuccess { rows ->
                _uiState.update {
                    it.copy(
                        records = rows,
                        isLoading = false,
                        error = null
                    )
                }
            }
            .onFailure { t ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = t.message ?: "Unable to load records."
                    )
                }
            }
    }

    private fun formatBatchSummary(title: String, result: SyncBatchResult): String {
        if (result.attempted == 0) {
            return "$title: nothing to upload."
        }
        return "$title: ${result.succeeded} ok, ${result.failed} failed (${result.attempted} attempted)"
    }

    fun formatRelativeLastSync(millis: Long?): String {
        if (millis == null) return "No successful sync yet"
        return "Last success: ${timeFormat.format(Date(millis))}"
    }
}
