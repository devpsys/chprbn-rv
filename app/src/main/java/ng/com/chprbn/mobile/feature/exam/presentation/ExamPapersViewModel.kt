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
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.feature.exam.domain.model.Paper
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamPapersUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.SyncExamRecordsUseCase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * Loads today's papers via [GetExamPapersUseCase] and maps them into the
 * design's card list. Picks the first paper as `Active` so the "Mark
 * Attendance" CTA renders on the right card.
 *
 * [ExamPapersUiState.hasDownloadedData] flips true once a real load
 * completes — an empty cache renders the real "no papers" empty state
 * (see `ExamPapersContent`), not the fake placeholder roster forever
 * (that used to be a silent bug: `refresh()` returned early on an empty
 * list, so the initial `placeholder()` content — including its fake
 * "Monday, June 12" date — never cleared).
 *
 * [syncState] toggles to [SyncOperationUiState.Syncing] while [onSyncNow]
 * runs, then to [SyncOperationUiState.Result] so the officer sees how many
 * rows synced vs. failed instead of the [ng.com.chprbn.mobile.core.domain.model.SyncBatchResult]
 * being silently discarded.
 */
@HiltViewModel
class ExamPapersViewModel @Inject constructor(
    private val getPapers: GetExamPapersUseCase,
    private val syncExamRecords: SyncExamRecordsUseCase,
    private val clock: Clock,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamPapersUiState.placeholder())
    val uiState: StateFlow<ExamPapersUiState> = _uiState.asStateFlow()

    private val _syncState = MutableStateFlow<SyncOperationUiState>(SyncOperationUiState.Idle)
    val syncState: StateFlow<SyncOperationUiState> = _syncState.asStateFlow()

    init {
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.value = getPapers().toUiState()
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

    private fun List<Paper>.toUiState(): ExamPapersUiState {
        // Compare each paper's start/end window to now (E9 audit fix — the
        // previous "first item = Active" heuristic mis-marked papers on
        // multi-day exams and after the current paper ended). If start/end
        // are unset (0L placeholders from the mapper), fall back to the
        // old first-item heuristic so the screen still shows *something*
        // clickable rather than nothing.
        val now = clock.nowMillis()
        val anyPaperHasWindow = any { it.startAt > 0L || it.endAt > 0L }
        val cards = mapIndexed { index, paper ->
            val status = if (anyPaperHasWindow) {
                paper.attendanceStatusFor(now)
            } else {
                if (index == 0) ExamPaperAttendanceStatus.Active
                else ExamPaperAttendanceStatus.Upcoming
            }
            paper.toCardUiState(status)
        }
        return ExamPapersUiState(
            dailyOverviewTitle = context.getString(R.string.exam_papers_daily_overview_title),
            dailyDateLabel = DATE_FORMATTER.format(Instant.ofEpochMilli(now)),
            totalPapersLabel = size.toString().padStart(2, '0'),
            studentsLabel = sumOf { it.totalCandidates }.toString(),
            statusPillLabel = context.getString(R.string.exam_papers_status_pill_in_progress),
            papers = cards,
            hasDownloadedData = true,
        )
    }

    private fun Paper.toCardUiState(status: ExamPaperAttendanceStatus): ExamPaperCardUiState =
        ExamPaperCardUiState(
            id = id,
            title = title,
            subtitle = subtitle,
            status = status,
            timeLabel = formatTimeRange(startAt, endAt),
            groupOrLocationLabel = hall.ifBlank { "$totalCandidates assigned" },
            iconKind = when (paperKind) {
                PaperKind.Practical -> ExamPaperIconKind.Science
                PaperKind.Theory -> ExamPaperIconKind.Description
                PaperKind.Project -> ExamPaperIconKind.EditNote
            },
            primaryActionLabel = if (status == ExamPaperAttendanceStatus.Active) {
                context.getString(R.string.exam_papers_action_mark_attendance)
            } else null,
        )

    /**
     * Bucket a paper against wall-clock `now`:
     *
     * - `now < startAt` → Upcoming
     * - `startAt ≤ now ≤ endAt` → Active
     * - `endAt < now` → Completed
     *
     * Papers with an unset window (both endpoints 0L) are marked Upcoming
     * so they at least don't grab the "Mark Attendance" CTA prematurely —
     * the caller may still override to `Active` when nothing else has a
     * window (fallback in [toUiState]).
     */
    private fun Paper.attendanceStatusFor(now: Long): ExamPaperAttendanceStatus = when {
        startAt <= 0L && endAt <= 0L -> ExamPaperAttendanceStatus.Upcoming
        now < startAt -> ExamPaperAttendanceStatus.Upcoming
        endAt in 1L..now -> ExamPaperAttendanceStatus.Completed
        else -> ExamPaperAttendanceStatus.Active
    }

    private fun formatTimeRange(start: Long, end: Long): String =
        if (start == 0L && end == 0L) {
            ""
        } else {
            "${TIME_FORMATTER.format(Instant.ofEpochMilli(start))} - " +
                TIME_FORMATTER.format(Instant.ofEpochMilli(end))
        }

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter
            .ofPattern("EEEE, MMMM d", Locale.US)
            .withZone(ZoneId.systemDefault())
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter
            .ofPattern("hh:mm a", Locale.US)
            .withZone(ZoneId.systemDefault())
    }
}
