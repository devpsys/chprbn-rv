package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamStatistics
import ng.com.chprbn.mobile.feature.exam.domain.usecase.ClearExamCacheUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamStatisticsUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.SyncExamRecordsUseCase
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Surfaces real local-DB counters in the existing statistics shape.
 * Exposes [refresh], [onSyncNow], and [onClearCached] for the screen's
 * action buttons; the FAB callbacks today are still pure navigation
 * stubs in the Screen layer (P3 hardening will wire them through).
 */
@HiltViewModel
class ExamStatisticsViewModel @Inject constructor(
    private val getStatistics: GetExamStatisticsUseCase,
    private val syncExamRecords: SyncExamRecordsUseCase,
    private val clearExamCache: ClearExamCacheUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamStatisticsUiState.placeholder())
    val uiState: StateFlow<ExamStatisticsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = getStatistics().toUiState()
        }
    }

    fun onSyncNow() {
        viewModelScope.launch {
            syncExamRecords()
            refresh()
        }
    }

    fun onClearCached() {
        viewModelScope.launch {
            clearExamCache()
            refresh()
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
        if (at == null || at == 0L) return "No data yet"
        val elapsed = Duration.between(Instant.ofEpochMilli(at), Instant.now())
        return when {
            elapsed.toMinutes() < 1 -> "Updated just now"
            elapsed.toMinutes() < 60 -> "Updated ${elapsed.toMinutes()}m ago"
            elapsed.toHours() < 24 -> "Updated ${elapsed.toHours()}h ago"
            else -> "Updated ${elapsed.toDays()}d ago"
        }
    }
}
