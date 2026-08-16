package ng.com.chprbn.mobile.feature.assessment.presentation

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentCandidateRow
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaper
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaperDetailResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSyncStats
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentCandidateRepository
import ng.com.chprbn.mobile.feature.assessment.domain.repository.PracticalScoringRepository
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetAssessmentCandidatesUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetAssessmentPaperDetailUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetAssessmentSyncStatsUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.SyncAssessmentScoresUseCase
import ng.com.chprbn.mobile.feature.exam.presentation.SyncOperationUiState
import java.time.Duration
import java.time.Instant
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
 * Sync chrome ([AssessmentPaperDetailUiState.lastUpdatedLabel] /
 * [AssessmentPaperDetailUiState.syncStatusLabel]) is loaded in [refresh]
 * from practical + project score rows. [onSyncData] runs
 * [SyncAssessmentScoresUseCase] (both score types share the queue) and
 * re-reads those stats.
 */
@HiltViewModel
class AssessmentPaperDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPaperDetail: GetAssessmentPaperDetailUseCase,
    private val getCandidates: GetAssessmentCandidatesUseCase,
    private val getSyncStats: GetAssessmentSyncStatsUseCase,
    private val syncScores: SyncAssessmentScoresUseCase,
    private val candidateRepository: AssessmentCandidateRepository,
    private val practicalScoringRepository: PracticalScoringRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val scheduleId: String = savedStateHandle.get<String>("scheduleId").orEmpty()

    private val _uiState = MutableStateFlow(AssessmentPaperDetailUiState())
    val uiState: StateFlow<AssessmentPaperDetailUiState> = _uiState.asStateFlow()

    private val _syncState = MutableStateFlow<SyncOperationUiState>(SyncOperationUiState.Idle)
    val syncState: StateFlow<SyncOperationUiState> = _syncState.asStateFlow()

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

    fun refresh() {
        viewModelScope.launch {
            val paperResult = getPaperDetail(scheduleId)
            val candidates = getCandidates(scheduleId)
            val stats = getSyncStats(scheduleId)
            applyResults(paperResult, candidates, stats)
        }
    }

    fun onSyncData() {
        if (_syncState.value is SyncOperationUiState.Syncing) return
        _syncState.value = SyncOperationUiState.Syncing
        viewModelScope.launch {
            val result = syncScores()
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

    private fun applyResults(
        paperResult: AssessmentPaperDetailResult,
        candidates: List<AssessmentCandidateRow>,
        stats: AssessmentSyncStats,
    ) {
        val total = candidates.size
        val previewRows = candidates.take(PREVIEW_ROW_COUNT).map { it.toPreviewRow() }
        val syncChrome = syncChrome(stats)

        when (paperResult) {
            is AssessmentPaperDetailResult.Success -> _uiState.update { current ->
                paperResult.paper.applyTo(current, total, previewRows)
                    .copy(
                        errorMessage = null,
                        lastUpdatedLabel = syncChrome.first,
                        syncStatusLabel = syncChrome.second,
                    )
            }
            AssessmentPaperDetailResult.NotFound -> _uiState.update { current ->
                current.copy(
                    candidates = previewRows,
                    totalCount = total,
                    lastUpdatedLabel = syncChrome.first,
                    syncStatusLabel = syncChrome.second,
                    errorMessage = "This paper isn't in the current dossier — pull a fresh dossier from the dashboard to load its details.",
                )
            }
            is AssessmentPaperDetailResult.Error -> _uiState.update { current ->
                current.copy(
                    candidates = previewRows,
                    totalCount = total,
                    lastUpdatedLabel = syncChrome.first,
                    syncStatusLabel = syncChrome.second,
                    errorMessage = paperResult.message.ifBlank {
                        "Could not load paper details."
                    },
                )
            }
        }
    }

    private fun syncChrome(stats: AssessmentSyncStats): Pair<String, String> {
        val lastUpdated = formatLastUpdated(stats.lastSyncAt)
        val syncStatus = if (stats.pendingSyncCount > 0) {
            context.getString(
                R.string.assessment_paper_detail_pending_sync_format,
                stats.pendingSyncCount,
            )
        } else {
            context.getString(R.string.assessment_paper_detail_sync_status_cloud_synced)
        }
        return lastUpdated to syncStatus
    }

    private fun formatLastUpdated(at: Long?): String {
        if (at == null || at == 0L) {
            return context.getString(R.string.assessment_paper_detail_no_data_yet)
        }
        val elapsed = Duration.between(Instant.ofEpochMilli(at), Instant.now())
        return when {
            elapsed.toMinutes() < 1 ->
                context.getString(R.string.assessment_paper_detail_last_updated_just_now)
            elapsed.toMinutes() < 60 ->
                context.getString(
                    R.string.assessment_paper_detail_last_updated_minutes_format,
                    elapsed.toMinutes(),
                )
            elapsed.toHours() < 24 ->
                context.getString(
                    R.string.assessment_paper_detail_last_updated_hours_format,
                    elapsed.toHours(),
                )
            else ->
                context.getString(
                    R.string.assessment_paper_detail_last_updated_days_format,
                    elapsed.toDays(),
                )
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
