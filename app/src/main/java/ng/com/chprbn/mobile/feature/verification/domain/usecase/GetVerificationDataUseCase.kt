package ng.com.chprbn.mobile.feature.verification.domain.usecase

import ng.com.chprbn.mobile.feature.verification.domain.model.VerificationData
import ng.com.chprbn.mobile.feature.verification.domain.repository.VerificationRepository
import javax.inject.Inject

/**
 * Use case: get full Verification data (user profile + features) in one call.
 * Ensures consistent snapshot from local cache.
 */
class GetVerificationDataUseCase @Inject constructor(
    private val repository: VerificationRepository
) {
    suspend operator fun invoke(): VerificationData = VerificationData(
        user = repository.getUserProfile(),
        features = repository.getFeatures()
    )
}
