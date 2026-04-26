package ng.com.chprbn.mobile.feature.verification.domain.model

import ng.com.chprbn.mobile.feature.auth.domain.model.User

/**
 * Aggregated Verification data: current user profile (from local cache) and feature list.
 * Single source of truth is local; remote sync can refresh cache.
 */
data class VerificationData(
    val user: User?,
    val features: List<VerificationFeature>
)
