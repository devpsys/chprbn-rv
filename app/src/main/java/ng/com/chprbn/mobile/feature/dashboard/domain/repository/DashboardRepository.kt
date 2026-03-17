package ng.com.chprbn.mobile.feature.dashboard.domain.repository

import ng.com.chprbn.mobile.feature.dashboard.domain.model.DashboardFeature

/**
 * Domain contract for dashboard data. Data layer implements this.
 */
interface DashboardRepository {
    fun getFeatures(): List<DashboardFeature>
}
