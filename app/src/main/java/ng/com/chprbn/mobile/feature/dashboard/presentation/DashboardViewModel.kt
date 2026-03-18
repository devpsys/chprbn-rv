package ng.com.chprbn.mobile.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.dashboard.domain.usecase.GetDashboardDataUseCase
import javax.inject.Inject

/**
 * Presentation ViewModel. Exposes [DashboardUiState] via [state];
 * depends only on domain use case (single flow: UseCase → Repository → Local/Remote).
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _state.value = DashboardUiState.Loading
            runCatching { getDashboardDataUseCase() }
                .onSuccess { data ->
                    _state.value = DashboardUiState.Success(
                        user = data.user,
                        features = data.features
                    )
                }
                .onFailure { e ->
                    _state.value = DashboardUiState.Error(e.message ?: "Unknown error")
                }
        }
    }

    fun retry() = loadDashboard()
}
