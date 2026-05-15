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
import javax.inject.Inject

/**
 * Loads the dashboard summary via [GetExamDashboardUseCase] and folds it
 * into the existing [ExamDashboardUiState] shape. Static visual content
 * (hero URLs, default chip labels) comes from the placeholder; only the
 * institution / chip-label fields are overridden when domain data lands.
 *
 * Also owns the destructive dossier-download flow: the FAB asks the
 * user to confirm via the warning dialog, then [downloadDossier] runs
 * the use case while the screen renders the loading overlay, and the
 * outcome flips [downloadState] to Success or Error.
 */
@HiltViewModel
class ExamDashboardViewModel @Inject constructor(
    private val getDashboard: GetExamDashboardUseCase,
    private val downloadDossier: DownloadExamDossierUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamDashboardUiState.placeholder())
    val uiState: StateFlow<ExamDashboardUiState> = _uiState.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadDossierUiState>(DownloadDossierUiState.Idle)
    val downloadState: StateFlow<DownloadDossierUiState> = _downloadState.asStateFlow()

    init {
        refreshDashboard()
    }

    private fun refreshDashboard() {
        viewModelScope.launch {
            when (val result = getDashboard()) {
                is ExamDashboardResult.Success -> {
                    val summary = result.summary
                    _uiState.update { current ->
                        current.copy(
                            institutionName = summary.center.name,
                            institutionCode = "#${summary.center.code}",
                            institutionLocation = summary.center.location,
                            attendanceTask = current.attendanceTask.copy(
                                chipSecondaryLabel = summary.attendanceCard.statusLabel,
                            ),
                            practicalTask = current.practicalTask.copy(
                                chipSecondaryLabel = summary.practicalCard.statusLabel,
                            ),
                        )
                    }
                }
                // Loading / Error keep the placeholder content so the
                // screen stays usable before the dossier lands.
                ExamDashboardResult.Loading,
                is ExamDashboardResult.Error -> Unit
            }
        }
    }

    fun onDownloadDossierClicked() {
        if (_downloadState.value !is DownloadDossierUiState.Downloading) {
            _downloadState.value = DownloadDossierUiState.WarningShown
        }
    }

    fun onDownloadConfirmed() {
        if (_downloadState.value is DownloadDossierUiState.Downloading) return
        _downloadState.value = DownloadDossierUiState.Downloading
        viewModelScope.launch {
            _downloadState.value = when (val result = downloadDossier()) {
                is DownloadDossierResult.Success -> {
                    refreshDashboard()
                    DownloadDossierUiState.Success(
                        papersCount = result.papersCount,
                        candidatesCount = result.candidatesCount,
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
    data object WarningShown : DownloadDossierUiState
    data object Downloading : DownloadDossierUiState
    data class Success(val papersCount: Int, val candidatesCount: Int) : DownloadDossierUiState
    data class Error(val message: String) : DownloadDossierUiState
}
