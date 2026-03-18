package ng.com.chprbn.mobile.feature.dashboard.domain.model

import ng.com.chprbn.mobile.feature.auth.domain.model.User

/**
 * Aggregated dashboard data: current user profile (from local cache) and feature list.
 * Single source of truth is local; remote sync can refresh cache.
 */
data class DashboardData(
    val user: User?,
    val features: List<DashboardFeature>
)
