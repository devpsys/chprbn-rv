package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentCandidateRow
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaper
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaperDetailResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.DownloadAssessmentPackageResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentCandidateRepository
import ng.com.chprbn.mobile.feature.assessment.domain.repository.PracticalScoringRepository
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.DownloadAssessmentPackageUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetAssessmentCandidatesUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetAssessmentPaperDetailUseCase
import javax.inject.Inject

/**
 * Resolves the paper detail and a short preview of assigned candidates
 * for the screen header + candidate strip.
 *
 * `progressFraction` / `checkedInCount` / `totalCount` are driven by a live
 * `combine(observeAssignedCount, observeStartedCandidateCount)` — "started"
 * is a pragmatic proxy for check-in until the assessment side models
 * per-paper attendance (A-S5 audit fix; was hardcoded 100%).
 *
 * Also owns the per-schedule package-download flow, triggered from the
 * screen's overflow action: warning dialog → loading overlay → success
 * / error result.
 */
@HiltViewModel
class AssessmentPaperDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPaperDetail: GetAssessmentPaperDetailUseCase,
    private val getCandidates: GetAssessmentCandidatesUseCase,
    private val downloadPackage: DownloadAssessmentPackageUseCase,
    private val candidateRepository: AssessmentCandidateRepository,
    private val practicalScoringRepository: PracticalScoringRepository,
) : ViewModel() {

    private val scheduleId: String = savedStateHandle.get<String>("scheduleId").orEmpty()

    private val _uiState = MutableStateFlow(AssessmentPaperDetailUiState())
    val uiState: StateFlow<AssessmentPaperDetailUiState> = _uiState.asStateFlow()

    private val _downloadState =
        MutableStateFlow<DownloadPackageUiState>(DownloadPackageUiState.Idle)
    val downloadState: StateFlow<DownloadPackageUiState> = _downloadState.asStateFlow()

    init {
        refresh()
        observeProgress()
    }

    /**
     * Combines the assigned-candidate count (denominator) with the
     * distinct-candidates-with-any-score count (numerator) into the
     * paper-detail progress pill. Re-emits every time either changes —
     * so scoring a candidate on a child screen updates this screen when
     * the user pops back (A-S5 audit fix). Both flows are live Room
     * queries: no explicit refresh needed.
     */
    private fun observeProgress() {
        if (scheduleId.isBlank()) return
        viewModelScope.launch {
            combine(
                candidateRepository.observeAssignedCount(scheduleId),
                practicalScoringRepository.observeStartedCandidateCount(scheduleId),
            ) { assigned, started ->
                val fraction = if (assigned > 0) {
                    started.toFloat() / assigned.toFloat()
                } else {
                    0f
                }
                Triple(assigned, started, fraction.coerceIn(0f, 1f))
            }.collect { (assigned, started, fraction) ->
                _uiState.update {
                    it.copy(
                        checkedInCount = started,
                        totalCount = assigned,
                        progressFraction = fraction,
                    )
                }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            val paperResult = getPaperDetail(scheduleId)
            val candidates = getCandidates(scheduleId)
            applyResults(paperResult, candidates)
        }
    }

    fun onDownloadPackageClicked() {
        if (_downloadState.value !is DownloadPackageUiState.Downloading) {
            _downloadState.value = DownloadPackageUiState.WarningShown
        }
    }

    fun onDownloadConfirmed() {
        if (_downloadState.value is DownloadPackageUiState.Downloading) return
        _downloadState.value = DownloadPackageUiState.Downloading
        viewModelScope.launch {
            _downloadState.value = when (val result = downloadPackage(scheduleId)) {
                is DownloadAssessmentPackageResult.Success -> {
                    refresh()
                    DownloadPackageUiState.Success(
                        candidatesCount = result.candidatesCount,
                        sectionsCount = result.sectionsCount,
                        questionsCount = result.questionsCount,
                    )
                }
                is DownloadAssessmentPackageResult.Error ->
                    DownloadPackageUiState.Error(result.message)
            }
        }
    }

    fun onDownloadDismissed() {
        if (_downloadState.value !is DownloadPackageUiState.Downloading) {
            _downloadState.value = DownloadPackageUiState.Idle
        }
    }

    private fun applyResults(
        paperResult: AssessmentPaperDetailResult,
        candidates: List<AssessmentCandidateRow>,
    ) {
        val total = candidates.size
        val previewRows = candidates.take(PREVIEW_ROW_COUNT).map { it.toPreviewRow() }

        when (paperResult) {
            is AssessmentPaperDetailResult.Success -> _uiState.update { current ->
                paperResult.paper.applyTo(current, total, previewRows).copy(errorMessage = null)
            }
            AssessmentPaperDetailResult.NotFound -> _uiState.update { current ->
                current.copy(
                    candidates = previewRows,
                    totalCount = total,
                    errorMessage = "This paper isn't in your cache yet — download the package to view details.",
                )
            }
            is AssessmentPaperDetailResult.Error -> _uiState.update { current ->
                current.copy(
                    candidates = previewRows,
                    totalCount = total,
                    errorMessage = paperResult.message.ifBlank {
                        "Could not load paper details."
                    },
                )
            }
        }
    }

    private fun AssessmentPaper.applyTo(
        current: AssessmentPaperDetailUiState,
        @Suppress("UNUSED_PARAMETER") total: Int,
        previewRows: List<CandidateRowUiState>,
    ): AssessmentPaperDetailUiState = current.copy(
        paperTitle = title,
        statusLabel = statusLabel,
        // progressFraction / checkedInCount / totalCount are owned by
        // observeProgress() — a live combine() of assigned + started counts
        // (A-S5 audit fix). Leaving the placeholder overrides here would
        // race the flow's first emission.
        facilityName = facility.name,
        facilityAddress = facility.address,
        hallName = hall.name,
        hallAddress = hall.address,
        candidates = previewRows,
        heroImageUrl = heroImageUrl,
    )

    private fun AssessmentCandidateRow.toPreviewRow(): CandidateRowUiState = CandidateRowUiState(
        id = candidate.id,
        initials = candidate.fullName.toInitials(),
        fullName = candidate.fullName,
        syncStatus = if (syncStatus == SyncStatus.Synced) {
            CandidateSyncStatus.Synced
        } else {
            CandidateSyncStatus.Unsynced
        },
    )

    private fun String.toInitials(): String =
        trim().split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }

    private companion object {
        const val PREVIEW_ROW_COUNT = 2
    }
}

sealed interface DownloadPackageUiState {
    data object Idle : DownloadPackageUiState
    data object WarningShown : DownloadPackageUiState
    data object Downloading : DownloadPackageUiState
    data class Success(
        val candidatesCount: Int,
        val sectionsCount: Int,
        val questionsCount: Int,
    ) : DownloadPackageUiState
    data class Error(val message: String) : DownloadPackageUiState
}
