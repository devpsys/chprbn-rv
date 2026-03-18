package ng.com.chprbn.mobile.feature.dashboard.presentation

import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.dashboard.domain.model.DashboardFeature

/**
 * UI state for the dashboard screen.
 * Sealed so the UI can react to loading, success, or error.
 */
sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(
        val user: User?,
        val features: List<DashboardFeature>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
