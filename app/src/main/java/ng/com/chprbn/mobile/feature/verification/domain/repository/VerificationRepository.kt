package ng.com.chprbn.mobile.feature.verification.domain.repository

import ng.com.chprbn.mobile.feature.verification.domain.model.VerificationFeature

/**
 * Domain contract for Verification data. Data layer implements this.
 *
 * User-profile retrieval is intentionally NOT on this contract: the
 * profile path is owned by `feature/profile` and applies
 * `SessionTokenPolicy` at its data boundary. Use
 * `GetUserProfileUseCase` from `feature/profile` instead.
 */
interface VerificationRepository {
    /** Verification feature grid items (static or from remote later). */
    suspend fun getFeatures(): List<VerificationFeature>

    /**
     * Canonical officer-remark choices for the verification form
     * dropdown. Returns an empty list on any remote failure; callers
     * fall back to bundled defaults.
     */
    suspend fun getOfficerRemarkOptions(): List<String>
}
