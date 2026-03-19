package ng.com.chprbn.mobile.feature.profile.presentation

import ng.com.chprbn.mobile.feature.auth.domain.model.User

/**
 * UI state for the profile screen.
 */
sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    /** Emitted after logout; UI should navigate to login. */
    data object LoggedOut : ProfileUiState()
}
