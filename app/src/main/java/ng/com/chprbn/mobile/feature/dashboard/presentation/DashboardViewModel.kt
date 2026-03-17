package ng.com.chprbn.mobile.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.dashboard.domain.usecase.GetDashboardFeaturesUseCase
import javax.inject.Inject

/**
 * Presentation ViewModel. Exposes [DashboardUiState] via [state];
 * depends only on domain use case.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardFeaturesUseCase: GetDashboardFeaturesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        loadFeatures()
    }

    private fun loadFeatures() {
        viewModelScope.launch {
            _state.value = DashboardUiState.Loading
            runCatching { getDashboardFeaturesUseCase() }
                .onSuccess { features -> _state.value = DashboardUiState.Success(features) }
                .onFailure { e ->
                    _state.value = DashboardUiState.Error(e.message ?: "Unknown error")
                }
        }
    }

    fun retry() = loadFeatures()
}
