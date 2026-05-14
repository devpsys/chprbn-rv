package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.feature.exam.domain.model.Paper
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamPapersUseCase
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
 * When the cache is empty (cold start before dossier download), the VM
 * falls back to the original placeholder content so the screen isn't blank.
 */
@HiltViewModel
class ExamPapersViewModel @Inject constructor(
    private val getPapers: GetExamPapersUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamPapersUiState.placeholder())
    val uiState: StateFlow<ExamPapersUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val papers = getPapers()
            if (papers.isEmpty()) return@launch
            _uiState.value = papers.toUiState()
        }
    }

    private fun List<Paper>.toUiState(): ExamPapersUiState {
        // Heuristic: the first paper gets the action label. Without a real
        // wall clock to compare to, we mark the first paper Active and
        // the rest Upcoming.
        val cards = mapIndexed { index, paper ->
            val status = when (index) {
                0 -> ExamPaperAttendanceStatus.Active
                else -> ExamPaperAttendanceStatus.Upcoming
            }
            paper.toCardUiState(status)
        }
        return ExamPapersUiState(
            dailyOverviewTitle = "Daily Overview",
            dailyDateLabel = DATE_FORMATTER.format(
                Instant.ofEpochMilli(firstOrNull()?.startAt ?: 0L),
            ),
            totalPapersLabel = size.toString().padStart(2, '0'),
            studentsLabel = sumOf { it.totalCandidates }.toString(),
            statusPillLabel = "In Progress",
            papers = cards,
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
            primaryActionLabel = if (status == ExamPaperAttendanceStatus.Active) "Mark Attendance" else null,
        )

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
            .ofPattern("h:mm a", Locale.US)
            .withZone(ZoneId.systemDefault())
    }
}
