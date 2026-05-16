package ng.com.chprbn.mobile.feature.verification.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.feature.verification.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.feature.verification.domain.model.SyncRecord
import ng.com.chprbn.mobile.feature.verification.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.verification.domain.usecase.GetSyncRecordsUseCase
import ng.com.chprbn.mobile.feature.verification.domain.usecase.RetryFailedSyncUseCase
import ng.com.chprbn.mobile.feature.verification.domain.usecase.SyncAllRecordsUseCase

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
    private val retryFailedSyncUseCase: RetryFailedSyncUseCase,
    @ApplicationContext private val context: Context,
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
            val actionLabel = context.getString(R.string.sync_action_label_sync_all)
            runCatching { syncAllRecordsUseCase() }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            lastBatchSummary = formatBatchSummary(actionLabel, result)
                        )
                    }
                    reloadFromDb()
                }
                .onFailure { t ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            error = t.message ?: context.getString(R.string.sync_error_sync_failed),
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
            val actionLabel = context.getString(R.string.sync_action_label_retry_failed)
            runCatching { retryFailedSyncUseCase() }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            lastBatchSummary = formatBatchSummary(actionLabel, result)
                        )
                    }
                    reloadFromDb()
                }
                .onFailure { t ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            error = t.message ?: context.getString(R.string.sync_error_retry_failed),
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
                // Don't clear `error` here — sync/retry failure paths rely on
                // the error message surviving the implicit reload that follows.
                // Callers (refresh, syncAll, retryFailed) clear `error` up front
                // when starting a fresh user-driven action; consumeError() handles
                // explicit dismissal.
                _uiState.update {
                    it.copy(
                        records = rows,
                        isLoading = false
                    )
                }
            }
            .onFailure { t ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = t.message ?: context.getString(R.string.sync_error_unable_to_load),
                    )
                }
            }
    }

    private fun formatBatchSummary(title: String, result: SyncBatchResult): String {
        return if (result.attempted == 0) {
            context.getString(R.string.sync_summary_nothing_to_upload, title)
        } else {
            context.getString(
                R.string.sync_summary_format,
                title,
                result.succeeded,
                result.failed,
                result.attempted,
            )
        }
    }

    fun formatRelativeLastSync(millis: Long?): String {
        if (millis == null) return context.getString(R.string.sync_no_successful_yet)
        return context.getString(R.string.sync_last_success_format, timeFormat.format(Date(millis)))
    }
}
