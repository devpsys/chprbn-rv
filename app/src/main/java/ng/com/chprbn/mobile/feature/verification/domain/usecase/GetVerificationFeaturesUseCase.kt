package ng.com.chprbn.mobile.feature.verification.domain.usecase

import ng.com.chprbn.mobile.feature.verification.domain.model.VerificationFeature
import ng.com.chprbn.mobile.feature.verification.domain.repository.VerificationRepository
import javax.inject.Inject

/**
 * Use case: get the list of Verification feature items.
 * Presentation layer uses this; domain layer defines it.
 */
class GetVerificationFeaturesUseCase @Inject constructor(
    private val repository: VerificationRepository
) {
    suspend operator fun invoke(): List<VerificationFeature> = repository.getFeatures()
}
