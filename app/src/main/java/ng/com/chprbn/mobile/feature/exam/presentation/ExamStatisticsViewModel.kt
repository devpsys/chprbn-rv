package ng.com.chprbn.mobile.feature.exam.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamStatistics
import ng.com.chprbn.mobile.feature.exam.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.exam.domain.usecase.ClearExamCacheUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamStatisticsUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.SyncExamRecordsUseCase
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Surfaces real local-DB counters in the existing statistics shape.
 * Exposes [refresh], [onSyncNow], and the [onClearCachedClicked]/
 * [onClearCacheConfirmed]/[onClearCacheDismissed] trio for the screen's
 * action buttons; the FAB callbacks today are still pure navigation
 * stubs in the Screen layer (P3 hardening will wire them through).
 *
 * [syncState] toggles to [SyncOperationUiState.Syncing] for the duration
 * of [onSyncNow], then to [SyncOperationUiState.Result] so the screen can
 * tell the officer how many rows synced vs. failed instead of the
 * [ng.com.chprbn.mobile.core.domain.model.SyncBatchResult] being silently
 * discarded.
 *
 * [clearCacheState] gates the destructive clear-cache action behind a
 * warning dialog — [ClearExamCacheUseCase]'s own contract requires the
 * caller to confirm with the user before invoking it.
 */
@HiltViewModel
class ExamStatisticsViewModel @Inject constructor(
    private val getStatistics: GetExamStatisticsUseCase,
    private val syncExamRecords: SyncExamRecordsUseCase,
    private val clearExamCache: ClearExamCacheUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamStatisticsUiState.placeholder())
    val uiState: StateFlow<ExamStatisticsUiState> = _uiState.asStateFlow()

    private val _syncState = MutableStateFlow<SyncOperationUiState>(SyncOperationUiState.Idle)
    val syncState: StateFlow<SyncOperationUiState> = _syncState.asStateFlow()

    private val _clearCacheState = MutableStateFlow<ClearCacheUiState>(ClearCacheUiState.Idle)
    val clearCacheState: StateFlow<ClearCacheUiState> = _clearCacheState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = getStatistics().toUiState()
        }
    }

    fun onSyncNow() {
        if (_syncState.value is SyncOperationUiState.Syncing) return
        _syncState.value = SyncOperationUiState.Syncing
        viewModelScope.launch {
            val result = syncExamRecords()
            refresh()
            _syncState.value = SyncOperationUiState.Result(
                succeeded = result.succeeded,
                failed = result.failed,
            )
        }
    }

    fun onSyncResultDismissed() {
        _syncState.value = SyncOperationUiState.Idle
    }

    fun onClearCachedClicked() {
        if (_clearCacheState.value !is ClearCacheUiState.Clearing) {
            _clearCacheState.value = ClearCacheUiState.WarningShown
        }
    }

    fun onClearCacheConfirmed() {
        if (_clearCacheState.value is ClearCacheUiState.Clearing) return
        _clearCacheState.value = ClearCacheUiState.Clearing
        viewModelScope.launch {
            _clearCacheState.value = when (val result = clearExamCache()) {
                SaveResult.Success -> {
                    refresh()
                    ClearCacheUiState.Success
                }
                is SaveResult.Error -> ClearCacheUiState.Error(result.message)
            }
        }
    }

    fun onClearCacheDismissed() {
        if (_clearCacheState.value !is ClearCacheUiState.Clearing) {
            _clearCacheState.value = ClearCacheUiState.Idle
        }
    }

    private fun ExamStatistics.toUiState(): ExamStatisticsUiState {
        val total = cachedCount.coerceAtLeast(1)
        val syncedFrac = (syncedCount.toFloat() / total).coerceIn(0f, 1f)
        val cachedFrac = ((pendingCount + failedCount).toFloat() / total).coerceIn(0f, 1f)
        return ExamStatisticsUiState(
            recordsDownloaded = recordsDownloaded.toString(),
            attendanceCaptured = attendanceCaptured.toString(),
            syncedRecords = syncedCount.toString(),
            recordsUpdatedLabel = formatLastUpdated(lastUpdatedAt),
            attendanceSubtitle = if (recordsDownloaded > 0) {
                "${(attendanceCaptured * 100 / recordsDownloaded.coerceAtLeast(1))}% Completion"
            } else {
                "—"
            },
            syncProgressFraction = syncedFrac,
            cachedBarFraction = cachedFrac,
            syncedBarFraction = syncedFrac,
            cachedCountLabel = (pendingCount + failedCount).toString(),
            syncedCountLabel = syncedCount.toString(),
            totalCountLabel = cachedCount.toString(),
            pendingSyncLegendCount = pendingCount.toString(),
            successfullySyncedLegendCount = syncedCount.toString(),
            footnote = if (failedCount > 0) "* $failedCount records failed to sync." else "",
            illustrationImageUrl = EXAM_STATISTICS_HERO_IMAGE_URL,
        )
    }

    private fun formatLastUpdated(at: Long?): String {
        if (at == null || at == 0L) return context.getString(R.string.exam_paper_no_data_yet)
        val elapsed = Duration.between(Instant.ofEpochMilli(at), Instant.now())
        return when {
            elapsed.toMinutes() < 1 -> context.getString(R.string.exam_statistics_updated_just_now)
            elapsed.toMinutes() < 60 -> context.getString(
                R.string.exam_statistics_updated_minutes_ago_format,
                elapsed.toMinutes().toInt(),
            )
            elapsed.toHours() < 24 -> context.getString(
                R.string.exam_statistics_updated_hours_ago_format,
                elapsed.toHours().toInt(),
            )
            else -> context.getString(
                R.string.exam_statistics_updated_days_ago_format,
                elapsed.toDays().toInt(),
            )
        }
    }
}

/**
 * State of a manual sync-now operation. [Result] carries the outcome
 * counts from the cross-feature `SyncBatchResult` so the screen can tell
 * the officer how many rows synced vs. failed; [onSyncResultDismissed]
 * returns to [Idle].
 */
sealed interface SyncOperationUiState {
    data object Idle : SyncOperationUiState
    data object Syncing : SyncOperationUiState
    data class Result(val succeeded: Int, val failed: Int) : SyncOperationUiState
}

/**
 * State of the destructive clear-cache action. [WarningShown] gates the
 * actual [ClearExamCacheUseCase] call behind a confirm dialog per its own
 * doc contract; [onClearCacheDismissed] returns [Success]/[Error]/
 * [WarningShown] to [Idle].
 */
sealed interface ClearCacheUiState {
    data object Idle : ClearCacheUiState
    data object WarningShown : ClearCacheUiState
    data object Clearing : ClearCacheUiState
    data object Success : ClearCacheUiState
    data class Error(val message: String) : ClearCacheUiState
}
