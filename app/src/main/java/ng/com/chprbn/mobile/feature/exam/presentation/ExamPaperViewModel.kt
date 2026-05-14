package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamPaperDetail
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamPaperDetailResult
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamPaperDetailUseCase
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Resolves the paper detail for the route's `paperId` and maps it to
 * the existing [ExamPaperUiState] shape. Sync status is derived from
 * `pendingSyncCount`; "last updated" is the most recent attendance
 * `markedAt` relative to wall-clock.
 *
 * NotFound / Error fall back to the placeholder content so the screen
 * stays usable until the dossier is downloaded.
 */
@HiltViewModel
class ExamPaperViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPaperDetail: GetExamPaperDetailUseCase,
) : ViewModel() {

    private val paperId: String = savedStateHandle.get<String>("paperId").orEmpty()

    private val _uiState = MutableStateFlow(ExamPaperUiState.placeholder())
    val uiState: StateFlow<ExamPaperUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = getPaperDetail(paperId)) {
                is ExamPaperDetailResult.Success -> _uiState.value = result.detail.toUiState()
                ExamPaperDetailResult.NotFound,
                is ExamPaperDetailResult.Error -> Unit
            }
        }
    }

    private fun ExamPaperDetail.toUiState(): ExamPaperUiState {
        val total = totalCandidates.coerceAtLeast(1)
        val percent = (checkedInCount.toFloat() / total).coerceIn(0f, 1f)
        return ExamPaperUiState(
            institutionHeroImageUrl = center.heroImageUrl ?: EXAM_PAPER_HERO_IMAGE_URL,
            institutionShortCode = center.code,
            institutionCodeLabel = "Institution Code",
            institutionName = center.name,
            institutionLocation = center.location,
            sessionLabel = "Today's Session",
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
                "Cloud Synced"
            },
            infoTitle = "Verification ongoing",
            infoMessage = "Please ensure all biometric data and QR codes are scanned before the session start time.",
        )
    }

    private fun formatLastUpdated(at: Long?): String {
        if (at == null || at == 0L) return "No data yet"
        val elapsed = Duration.between(Instant.ofEpochMilli(at), Instant.now())
        return when {
            elapsed.toMinutes() < 1 -> "Last updated: just now"
            elapsed.toMinutes() < 60 -> "Last updated: ${elapsed.toMinutes()} min ago"
            elapsed.toHours() < 24 -> "Last updated: ${elapsed.toHours()} h ago"
            else -> "Last updated: ${elapsed.toDays()} d ago"
        }
    }
}
