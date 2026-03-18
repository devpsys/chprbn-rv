package ng.com.chprbn.mobile.feature.dashboard.domain.usecase

import ng.com.chprbn.mobile.feature.dashboard.domain.model.DashboardFeature
import ng.com.chprbn.mobile.feature.dashboard.domain.repository.DashboardRepository
import javax.inject.Inject

/**
 * Use case: get the list of dashboard feature items.
 * Presentation layer uses this; domain layer defines it.
 */
class GetDashboardFeaturesUseCase @Inject constructor(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(): List<DashboardFeature> = repository.getFeatures()
}
