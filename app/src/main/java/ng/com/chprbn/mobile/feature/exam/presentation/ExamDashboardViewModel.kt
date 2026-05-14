package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamDashboardResult
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamDashboardUseCase
import javax.inject.Inject

/**
 * Loads the dashboard summary via [GetExamDashboardUseCase] and folds it
 * into the existing [ExamDashboardUiState] shape. Static visual content
 * (hero URLs, default chip labels) comes from the placeholder; only the
 * institution / chip-label fields are overridden when domain data lands.
 *
 * Error / Loading results fall back to the placeholder so the screen
 * stays renderable while the data layer warms up (or, today, while the
 * dossier isn't downloaded yet).
 */
@HiltViewModel
class ExamDashboardViewModel @Inject constructor(
    private val getDashboard: GetExamDashboardUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamDashboardUiState.placeholder())
    val uiState: StateFlow<ExamDashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = getDashboard()) {
                is ExamDashboardResult.Success -> {
                    val summary = result.summary
                    _uiState.value = _uiState.value.copy(
                        institutionName = summary.center.name,
                        institutionCode = "#${summary.center.code}",
                        institutionLocation = summary.center.location,
                        attendanceTask = _uiState.value.attendanceTask.copy(
                            chipSecondaryLabel = summary.attendanceCard.statusLabel,
                        ),
                        practicalTask = _uiState.value.practicalTask.copy(
                            chipSecondaryLabel = summary.practicalCard.statusLabel,
                        ),
                    )
                }
                // Loading / Error keep the placeholder content so the
                // screen stays usable before the dossier lands.
                ExamDashboardResult.Loading,
                is ExamDashboardResult.Error -> Unit
            }
        }
    }
}
