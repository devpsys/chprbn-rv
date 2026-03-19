package ng.com.chprbn.mobile.feature.profile.domain.usecase

import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Use case: get current user profile from local cache.
 */
class GetUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): User? = repository.getUserProfile()
}
