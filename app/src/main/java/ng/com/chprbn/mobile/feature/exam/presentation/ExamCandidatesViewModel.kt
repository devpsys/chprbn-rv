package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceFilter
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamCandidateRow
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamCandidatesUseCase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * Candidates list for the user's currently-active paper. The first
 * paper in cache stands in for "currently active" until the auth /
 * session layer wires up a true active-paper signal. SQL-side filter
 * + LIKE-pattern free-text search; an in-flight query change drops
 * stale results.
 */
@HiltViewModel
class ExamCandidatesViewModel @Inject constructor(
    private val getCandidates: GetExamCandidatesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamCandidatesUiState.placeholder())
    val uiState: StateFlow<ExamCandidatesUiState> = _uiState.asStateFlow()

    // TODO: replace with the active paper id from OfficerSession once
    // the auth feature exposes one. v1 leaves it blank, so the use case
    // returns empty and we keep the placeholder list as a visual fallback.
    private val paperId: String = ""

    init {
        viewModelScope.launch {
            val rows = getCandidates(paperId)
            if (rows.isNotEmpty()) {
                _uiState.update { it.copy(candidates = rows.map { row -> row.toCardUi() }) }
            }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(searchQuery = value) }
        viewModelScope.launch {
            val filter = filterFromLabel(_uiState.value.activeFilterLabel)
            val rows = getCandidates(paperId, filter, value)
            _uiState.update { current ->
                if (current.searchQuery != value) current
                else current.copy(candidates = rows.map { it.toCardUi() })
            }
        }
    }

    fun onFilterChange(label: String) {
        _uiState.update { it.copy(activeFilterLabel = label) }
        viewModelScope.launch {
            val rows = getCandidates(paperId, filterFromLabel(label), _uiState.value.searchQuery)
            _uiState.update { it.copy(candidates = rows.map { row -> row.toCardUi() }) }
        }
    }

    private fun filterFromLabel(label: String): AttendanceFilter = when (label) {
        "Signed In" -> AttendanceFilter.SignedIn
        "Signed Out" -> AttendanceFilter.SignedOut
        "Flagged" -> AttendanceFilter.Flagged
        else -> AttendanceFilter.All
    }

    private fun ExamCandidateRow.toCardUi(): ExamCandidateUiState = ExamCandidateUiState(
        avatarUrl = candidate.photoUrl ?: MARCUS_AVATAR_URL,
        name = candidate.fullName,
        idLabel = "ID: ${candidate.examNumber}",
        statusPillLabel = when (attendance?.status) {
            AttendanceStatus.SignedIn -> "Signed In"
            AttendanceStatus.SignedOut -> "Signed Out"
            AttendanceStatus.Flagged -> "Flagged"
            null -> if (remarkCount > 0) "$remarkCount Remark" else "Pending"
        },
        statusSubLabel = attendance?.markedAt
            ?.takeIf { it > 0L }
            ?.let { TIME_FORMATTER.format(Instant.ofEpochMilli(it)) }
            ?: "Pending",
    )

    private companion object {
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter
            .ofPattern("h:mm a", Locale.US)
            .withZone(ZoneId.systemDefault())
    }
}
