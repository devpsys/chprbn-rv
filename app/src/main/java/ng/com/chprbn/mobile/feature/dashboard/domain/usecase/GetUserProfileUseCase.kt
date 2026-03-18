package ng.com.chprbn.mobile.feature.dashboard.domain.usecase

import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.dashboard.domain.repository.DashboardRepository
import javax.inject.Inject

/**
 * Use case: get the current user profile (from local cache).
 * Used by Dashboard to show welcome card and profile info.
 */
class GetUserProfileUseCase @Inject constructor(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(): User? = repository.getUserProfile()
}
