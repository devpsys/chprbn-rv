package ng.com.chprbn.mobile.feature.dashboard.domain.repository

import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.dashboard.domain.model.DashboardFeature

/**
 * Domain contract for dashboard data. Data layer implements this.
 * Single source of truth: local cache; remote can refresh.
 */
interface DashboardRepository {
    /** Current user profile from local cache (set by auth login). */
    suspend fun getUserProfile(): User?
    /** Dashboard feature grid items (static or from remote later). */
    suspend fun getFeatures(): List<DashboardFeature>
}
