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
import ng.com.chprbn.mobile.feature.exam.domain.model.AddRemarkResult
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamCandidateRow
import ng.com.chprbn.mobile.feature.exam.domain.usecase.AddRemarkUseCase
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
 * ever showed [ExamCandidatesUiState.preview]'s fake roster regardless
 * of dossier state. Fixed alongside the same class of bug on
 * `ExamPapersViewModel`/`ExamDashboardViewModel`: a real (possibly
 * empty) result always replaces [source] now; an empty roster renders
 * `ExamCandidatesContent`'s existing empty state instead of fake data.
 * [_uiState] itself now starts from [ExamCandidatesUiState.initial] (no
 * candidates, `hasLoaded = false`) rather than the fake roster, so the
 * loading spinner — not made-up names — is what a real officer briefly
 * sees before the first [refresh] resolves.
 *
 * Filter + search are applied **client-side** against the source list —
 * cheap at today's roster size (bounded ~200/day per centre); revisit
 * with a SQL-side filter if that changes.
 *
 * [remarkDialogState] drives the Add Remark modal ([AddRemarkDialog]):
 * [onAddRemarkClicked] opens it for a candidate, [onSelectRemarkType]
 * tracks the picked [RemarkType], and [onSaveRemark] calls
 * [AddRemarkUseCase] and optimistically sets that candidate's
 * [ExamCandidateUiState.remarkCount] to 1 on success (one remark per
 * candidate — a save always replaces, never appends) rather than
 * re-querying the whole roster.
 */
@HiltViewModel
class ExamCandidatesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCandidates: GetExamCandidatesUseCase,
    private val addRemark: AddRemarkUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamCandidatesUiState.initial())
    val uiState: StateFlow<ExamCandidatesUiState> = _uiState.asStateFlow()

    private val _remarkDialogState = MutableStateFlow<AddRemarkUiState>(AddRemarkUiState.Closed)
    val remarkDialogState: StateFlow<AddRemarkUiState> = _remarkDialogState.asStateFlow()

    // Unfiltered source of truth for the currently-loaded roster. Replaced
    // once at init with the real (possibly empty) result. Every
    // filter/search re-derives from this list.
    private var source: List<ExamCandidateUiState> = _uiState.value.candidates

    /** Exposed so [ExamCandidatesScreen] can pass it along when navigating to a candidate's profile. */
    val paperId: String = savedStateHandle.get<String>("paperId").orEmpty()

    init {
        refresh()
    }

    /**
     * Public so [ExamCandidatesScreen] can re-invoke it on
     * `LifecycleResumeEffect` — returning from a candidate's profile
     * (where remarks can be cleared, changing [ExamCandidateUiState.remarkCount])
     * shouldn't leave this roster showing stale counts.
     */
    fun refresh() {
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

    fun onAddRemarkClicked(candidateId: String) {
        val candidateName = source.firstOrNull { it.candidateId == candidateId }?.name.orEmpty()
        _remarkDialogState.value = AddRemarkUiState.Open(
            candidateId = candidateId,
            candidateName = candidateName,
        )
    }

    fun onSelectRemarkType(type: RemarkType) {
        _remarkDialogState.update { state ->
            if (state is AddRemarkUiState.Open) {
                state.copy(selectedType = type, errorMessage = null)
            } else {
                state
            }
        }
    }

    fun onDismissRemarkDialog() {
        _remarkDialogState.value = AddRemarkUiState.Closed
    }

    fun onSaveRemark() {
        val state = _remarkDialogState.value
        if (state !is AddRemarkUiState.Open || state.isSaving) return
        val type = state.selectedType ?: return

        _remarkDialogState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            val result = addRemark(
                candidateId = state.candidateId,
                paperId = paperId,
                body = context.getString(type.labelRes),
                severity = type.severity,
                code = type.code,
            )
            when (result) {
                is AddRemarkResult.Success -> {
                    // One remark per candidate now — saving replaces
                    // whatever was already on file, so the count is
                    // always 1 after a successful save, never incremented.
                    source = source.map { candidate ->
                        if (candidate.candidateId == state.candidateId) {
                            candidate.copy(remarkCount = 1)
                        } else {
                            candidate
                        }
                    }
                    emitVisible()
                    _remarkDialogState.value = AddRemarkUiState.Closed
                }
                is AddRemarkResult.Error -> {
                    _remarkDialogState.value = state.copy(
                        isSaving = false,
                        errorMessage = result.message.ifBlank {
                            context.getString(R.string.exam_candidates_remark_dialog_error_default)
                        },
                    )
                }
            }
        }
    }

    private fun emitVisible() {
        val query = _uiState.value.searchQuery.trim()
        val filter = _uiState.value.activeFilterLabel
        val visible = source.asSequence()
            .filter { it.matchesFilter(filter) }
            .filter { it.matchesQuery(query) }
            .toList()
        _uiState.update { it.copy(candidates = visible, hasLoaded = true) }
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
        candidateId = candidate.id,
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
