package ng.com.chprbn.mobile.feature.verification.domain.usecase

import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.verification.domain.repository.VerificationRepository
import javax.inject.Inject

/**
 * Use case: get the current user profile (from local cache).
 * Used by Verification to show welcome card and profile info.
 */
class GetUserProfileUseCase @Inject constructor(
    private val repository: VerificationRepository
) {
    suspend operator fun invoke(): User? = repository.getUserProfile()
}
