package ng.com.chprbn.mobile.feature.dashboard.domain.usecase

import ng.com.chprbn.mobile.feature.dashboard.domain.model.DashboardData
import ng.com.chprbn.mobile.feature.dashboard.domain.repository.DashboardRepository
import javax.inject.Inject

/**
 * Use case: get full dashboard data (user profile + features) in one call.
 * Ensures consistent snapshot from local cache.
 */
class GetDashboardDataUseCase @Inject constructor(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(): DashboardData = DashboardData(
        user = repository.getUserProfile(),
        features = repository.getFeatures()
    )
}
