package ng.com.chprbn.mobile.feature.exam.presentation

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamPaperDetail
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamPaperDetailResult
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamPaperDetailUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.SyncExamRecordsUseCase
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Resolves the paper detail for the route's `paperId` and maps it to
 * the existing [ExamPaperUiState] shape. Sync status is derived from
 * `pendingSyncCount`; "last updated" is the most recent attendance
 * `markedAt` relative to wall-clock.
 *
 * NotFound / Error keep whatever content was already showing (placeholder
 * on first load, stale real data on a later failed refresh) but now also
 * set [ExamPaperUiState.errorMessage], rendered as a banner by
 * `ExamPaperContent` (mirrors `AssessmentPaperDetailContent`'s A-S7
 * pattern) — previously this failed silently with no indication to the
 * officer.
 *
 * [syncState] toggles to [SyncOperationUiState.Syncing] while [onSyncData]
 * runs, then to [SyncOperationUiState.Result] so the officer sees how many
 * rows synced vs. failed instead of the [ng.com.chprbn.mobile.core.domain.model.SyncBatchResult]
 * being silently discarded.
 */
@HiltViewModel
class ExamPaperViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPaperDetail: GetExamPaperDetailUseCase,
    private val syncExamRecords: SyncExamRecordsUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val paperId: String = savedStateHandle.get<String>("paperId").orEmpty()

    private val _uiState = MutableStateFlow(ExamPaperUiState.placeholder())
    val uiState: StateFlow<ExamPaperUiState> = _uiState.asStateFlow()

    private val _syncState = MutableStateFlow<SyncOperationUiState>(SyncOperationUiState.Idle)
    val syncState: StateFlow<SyncOperationUiState> = _syncState.asStateFlow()

    init {
        refresh()
    }

    /** Public so [ExamPaperScreen] can re-invoke it on `LifecycleResumeEffect` (e.g. returning from the scan flow). */
    fun refresh() {
        viewModelScope.launch {
            when (val result = getPaperDetail(paperId)) {
                is ExamPaperDetailResult.Success -> _uiState.value = result.detail.toUiState()
                ExamPaperDetailResult.NotFound -> _uiState.update {
                    it.copy(errorMessage = context.getString(R.string.exam_paper_error_not_found))
                }
                is ExamPaperDetailResult.Error -> _uiState.update {
                    it.copy(errorMessage = result.message)
                }
            }
        }
    }

    fun onSyncData() {
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

    private fun ExamPaperDetail.toUiState(): ExamPaperUiState {
        val total = totalCandidates.coerceAtLeast(1)
        val percent = (checkedInCount.toFloat() / total).coerceIn(0f, 1f)
        return ExamPaperUiState(
            institutionHeroImageUrl = center.heroImageUrl ?: EXAM_PAPER_HERO_IMAGE_URL,
            institutionShortCode = center.code,
            institutionCodeLabel = context.getString(R.string.exam_paper_institution_code_label),
            institutionName = center.name,
            institutionLocation = center.location,
            sessionLabel = context.getString(R.string.exam_paper_session_label_today),
            paperTitle = paper.title,
            totalCandidates = totalCandidates.toString(),
            verifiedPresent = checkedInCount.toString(),
            attendanceProgressFraction = percent,
            attendanceProgressLabel = "$checkedInCount of $totalCandidates candidates checked in",
            attendancePercentLabel = "${(percent * 100).toInt()}%",
            lastUpdatedLabel = formatLastUpdated(lastSyncAt),
            syncStatusLabel = if (pendingSyncCount > 0) {
                "Pending Sync ($pendingSyncCount)"
            } else {
                context.getString(R.string.exam_paper_sync_status_cloud_synced)
            },
            infoTitle = context.getString(R.string.exam_paper_info_title_verification_ongoing),
            infoMessage = context.getString(R.string.exam_paper_info_message_verification_ongoing),
        )
    }

    private fun formatLastUpdated(at: Long?): String {
        if (at == null || at == 0L) return context.getString(R.string.exam_paper_no_data_yet)
        val elapsed = Duration.between(Instant.ofEpochMilli(at), Instant.now())
        return when {
            elapsed.toMinutes() < 1 -> "Last updated: just now"
            elapsed.toMinutes() < 60 -> "Last updated: ${elapsed.toMinutes()} min ago"
            elapsed.toHours() < 24 -> "Last updated: ${elapsed.toHours()} h ago"
            else -> "Last updated: ${elapsed.toDays()} d ago"
        }
    }
}
