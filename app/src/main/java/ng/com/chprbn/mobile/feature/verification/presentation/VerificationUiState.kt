package ng.com.chprbn.mobile.feature.verification.presentation

import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.verification.domain.model.VerificationFeature

/**
 * UI state for the Verification screen.
 * Sealed so the UI can react to loading, success, or error.
 */
sealed class VerificationUiState {
    data object Loading : VerificationUiState()
    data class Success(
        val user: User?,
        val features: List<VerificationFeature>
    ) : VerificationUiState()
    data class Error(val message: String) : VerificationUiState()
}
