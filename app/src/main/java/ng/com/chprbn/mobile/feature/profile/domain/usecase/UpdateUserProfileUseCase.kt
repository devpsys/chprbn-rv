package ng.com.chprbn.mobile.feature.profile.domain.usecase

import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Use case: update user profile in local cache (e.g. after edit or API refresh).
 */
class UpdateUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(user: User) = repository.updateUserProfile(user)
}
