package ng.com.chprbn.mobile.feature.verification.domain.repository

import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.verification.domain.model.VerificationFeature

/**
 * Domain contract for Verification data. Data layer implements this.
 * Single source of truth: local cache; remote can refresh.
 */
interface VerificationRepository {
    /** Current user profile from local cache (set by auth login). */
    suspend fun getUserProfile(): User?
    /** Verification feature grid items (static or from remote later). */
    suspend fun getFeatures(): List<VerificationFeature>
}
