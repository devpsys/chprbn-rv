package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.exam.domain.model.DownloadDossierResult
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamDashboardResult
import ng.com.chprbn.mobile.feature.exam.domain.usecase.DownloadExamDossierUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamDashboardUseCase
import ng.com.chprbn.mobile.feature.profile.domain.usecase.LogoutUseCase
import javax.inject.Inject

/**
 * Loads the dashboard summary via [GetExamDashboardUseCase] and folds it
 * into the existing [ExamDashboardUiState] shape. Static visual content
 * (hero URLs, default chip labels) comes from the placeholder; only the
 * institution / chip-label fields are overridden when domain data lands.
 *
 * [refresh] is public (not just called from `init`) so the screen can
 * reload it on every `ON_RESUME` — the bottom-nav tab returns here via
 * `popBackStack` rather than a fresh `navigate()`, so this ViewModel
 * instance survives a round trip to Statistics and would otherwise show
 * stale data after a sync/clear there.
 *
 * Also owns the destructive dossier-download flow: the FAB asks the
 * user to confirm via the warning dialog, then [downloadDossier] runs
 * the use case while the screen renders the loading overlay, and the
 * outcome flips [downloadState] to Success or Error.
 *
 * [logoutUseCase] mirrors `ProfileViewModel.logout()` — clears the local
 * session; the screen observes [loggedOut] and navigates to Login.
 */
@HiltViewModel
class ExamDashboardViewModel @Inject constructor(
    private val getDashboard: GetExamDashboardUseCase,
    private val downloadDossier: DownloadExamDossierUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamDashboardUiState.placeholder())
    val uiState: StateFlow<ExamDashboardUiState> = _uiState.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadDossierUiState>(DownloadDossierUiState.Idle)
    val downloadState: StateFlow<DownloadDossierUiState> = _downloadState.asStateFlow()

    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            when (val result = getDashboard()) {
                is ExamDashboardResult.Success -> {
                    val summary = result.summary
                    _uiState.update { current ->
                        current.copy(
                            institutionName = summary.center.name,
                            institutionCode = "#${summary.center.code}",
                            institutionLocation = summary.center.location,
                            heroImageUrl = summary.center.heroImageUrl ?: current.heroImageUrl,
                            hasDownloadedData = true,
                            hasSchedules = summary.papersCount > 0,
                            hasAttendanceCard = summary.hasAttendancePapers,
                            hasPracticalAssessment = summary.hasPracticalAssessment,
                            attendanceTask = current.attendanceTask.copy(
                                chipSecondaryLabel = summary.attendanceCard.statusLabel,
                            ),
                            practicalTask = current.practicalTask.copy(
                                chipSecondaryLabel = summary.practicalCard.statusLabel,
                            ),
                        )
                    }
                }
                // No dossier ever downloaded — drives the empty state.
                ExamDashboardResult.Empty -> {
                    _uiState.update { it.copy(hasDownloadedData = false) }
                }
                // Loading / Error keep whatever content is already showing
                // so the screen stays usable before/around a transient hiccup.
                ExamDashboardResult.Loading,
                is ExamDashboardResult.Error -> Unit
            }
            // Only the very first call matters — isLoading is already false
            // on every resume-triggered refresh() after that, so this never
            // re-shows the spinner over already-loaded content.
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            runCatching { logoutUseCase() }
                .onSuccess { _loggedOut.value = true }
        }
    }

    fun onDownloadDossierClicked() {
        if (_downloadState.value !is DownloadDossierUiState.Downloading) {
            // First-time download vs later refresh get different copy —
            // the initial dialog explains what will land on the device,
            // the refresh dialog explains that already-captured work is
            // preserved. Reading hasDownloadedData off the current
            // dashboard uiState (populated by refresh()) keeps the two
            // flows in the same VM without duplicating the "have we
            // ever synced?" check.
            _downloadState.value = DownloadDossierUiState.WarningShown(
                isInitialDownload = !_uiState.value.hasDownloadedData,
            )
        }
    }

    fun onDownloadConfirmed() {
        if (_downloadState.value is DownloadDossierUiState.Downloading) return
        _downloadState.value = DownloadDossierUiState.Downloading
        viewModelScope.launch {
            _downloadState.value = when (val result = downloadDossier()) {
                is DownloadDossierResult.Success -> {
                    refresh()
                    DownloadDossierUiState.Success(
                        papersCount = result.papersCount,
                        newCandidatesCount = result.newCandidatesCount,
                        skippedCandidatesCount = result.skippedCandidatesCount,
                    )
                }
                is DownloadDossierResult.Error -> DownloadDossierUiState.Error(result.message)
            }
        }
    }

    fun onDownloadDismissed() {
        // Ignore dismiss while the operation is in flight — the overlay
        // is uncancelable. Warning / Success / Error all fall back to Idle.
        if (_downloadState.value !is DownloadDossierUiState.Downloading) {
            _downloadState.value = DownloadDossierUiState.Idle
        }
    }
}

sealed interface DownloadDossierUiState {
    data object Idle : DownloadDossierUiState

    /**
     * Warning dialog is up. [isInitialDownload] flips the dialog copy:
     * `true` when no dossier has ever been cached (first-time download —
     * emphasise what will land on the device), `false` on any subsequent
     * click (refresh — emphasise that captured attendance / remarks /
     * scores stay put).
     */
    data class WarningShown(val isInitialDownload: Boolean) : DownloadDossierUiState

    data object Downloading : DownloadDossierUiState
    /**
     * Additive-merge summary. [newCandidatesCount] is how many candidates
     * actually landed; [skippedCandidatesCount] is how many the wire
     * re-sent that were already cached (kept as-is to preserve any
     * captured data). [papersCount] is the total post-refresh count for
     * papers (authoritative).
     */
    data class Success(
        val papersCount: Int,
        val newCandidatesCount: Int,
        val skippedCandidatesCount: Int,
    ) : DownloadDossierUiState
    data class Error(val message: String) : DownloadDossierUiState
}
