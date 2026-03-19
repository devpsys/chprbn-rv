package ng.com.chprbn.mobile.feature.profile.domain.usecase

import ng.com.chprbn.mobile.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Use case: clear local session (logout). Caller should navigate to login after.
 */
class LogoutUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke() = repository.logout()
}
