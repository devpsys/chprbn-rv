package ng.com.chprbn.mobile.feature.verification.domain.usecase

import ng.com.chprbn.mobile.feature.profile.domain.usecase.GetUserProfileUseCase
import ng.com.chprbn.mobile.feature.verification.domain.model.VerificationData
import ng.com.chprbn.mobile.feature.verification.domain.repository.VerificationRepository
import javax.inject.Inject

/**
 * Use case: get full Verification data (user profile + features) in one call.
 * Ensures consistent snapshot from local cache. The user-profile path goes
 * through `feature/profile` so `SessionTokenPolicy` is applied uniformly.
 */
class GetVerificationDataUseCase @Inject constructor(
    private val repository: VerificationRepository,
    private val getUserProfile: GetUserProfileUseCase
) {
    suspend operator fun invoke(): VerificationData = VerificationData(
        user = getUserProfile(),
        features = repository.getFeatures()
    )
}
