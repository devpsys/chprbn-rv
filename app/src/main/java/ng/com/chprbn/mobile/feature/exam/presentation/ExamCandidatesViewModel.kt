package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamCandidateRow
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamCandidatesUseCase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * Candidates list for [paperId], read off the `Routes.ExamCandidates` nav
 * arg the same way [ExamPaperViewModel] reads `paperId` for the paper
 * detail screen. Previously hardcoded to `""` (E10 audit ticket) — the
 * use case silently returns empty for a blank paper id, so the VM only
 * ever showed [ExamCandidatesUiState.placeholder]'s fake roster
 * regardless of dossier state. Fixed alongside the same class of bug on
 * `ExamPapersViewModel`/`ExamDashboardViewModel`: a real (possibly
 * empty) result always replaces [source] now; an empty roster renders
 * `ExamCandidatesContent`'s existing empty state instead of fake data.
 *
 * Filter + search are applied **client-side** against the source list —
 * cheap at today's roster size (bounded ~200/day per centre); revisit
 * with a SQL-side filter if that changes.
 */
@HiltViewModel
class ExamCandidatesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCandidates: GetExamCandidatesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamCandidatesUiState.placeholder())
    val uiState: StateFlow<ExamCandidatesUiState> = _uiState.asStateFlow()

    // Unfiltered source of truth for the currently-loaded roster. Replaced
    // once at init with the real (possibly empty) result. Every
    // filter/search re-derives from this list.
    private var source: List<ExamCandidateUiState> = _uiState.value.candidates

    private val paperId: String = savedStateHandle.get<String>("paperId").orEmpty()

    init {
        viewModelScope.launch {
            source = getCandidates(paperId).map { it.toCardUi() }
            emitVisible()
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(searchQuery = value) }
        emitVisible()
    }

    fun onFilterChange(label: String) {
        _uiState.update { it.copy(activeFilterLabel = label) }
        emitVisible()
    }

    private fun emitVisible() {
        val query = _uiState.value.searchQuery.trim()
        val filter = _uiState.value.activeFilterLabel
        val visible = source.asSequence()
            .filter { it.matchesFilter(filter) }
            .filter { it.matchesQuery(query) }
            .toList()
        _uiState.update { it.copy(candidates = visible) }
    }

    private fun ExamCandidateUiState.matchesFilter(filterLabel: String): Boolean =
        when (filterLabel) {
            FILTER_ALL, "" -> true
            else -> statusPillLabel.equals(filterLabel, ignoreCase = true)
        }

    private fun ExamCandidateUiState.matchesQuery(query: String): Boolean =
        if (query.isEmpty()) true
        else name.contains(query, ignoreCase = true) ||
            idLabel.contains(query, ignoreCase = true)

    private fun ExamCandidateRow.toCardUi(): ExamCandidateUiState = ExamCandidateUiState(
        // Nullable in the model — the card renders a bundled Icon fallback
        // when null (E11 audit fix; previously hotlinked stock avatars).
        avatarUrl = candidate.photoUrl,
        name = candidate.fullName,
        idLabel = "ID: ${candidate.examNumber}",
        // The pill is pure attendance now — remark count drives the button
        // style in ExamCandidatesContent, not the pill (design spec).
        statusPillLabel = when (attendance?.status) {
            AttendanceStatus.SignedIn -> "Signed In"
            AttendanceStatus.SignedOut -> "Signed Out"
            AttendanceStatus.Flagged -> "Flagged"
            null -> "Pending"
        },
        statusSubLabel = attendance?.markedAt
            ?.takeIf { it > 0L }
            ?.let { TIME_FORMATTER.format(Instant.ofEpochMilli(it)) }
            ?: "Pending",
        remarkCount = remarkCount,
    )

    private companion object {
        const val FILTER_ALL = "All"
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter
            .ofPattern("h:mm a", Locale.US)
            .withZone(ZoneId.systemDefault())
    }
}
