package ng.com.chprbn.mobile.feature.verification.data.repository

import ng.com.chprbn.mobile.feature.verification.domain.model.FeatureType
import ng.com.chprbn.mobile.feature.verification.domain.model.VerificationFeature
import ng.com.chprbn.mobile.feature.verification.domain.repository.VerificationRepository
import javax.inject.Inject

/**
 * Data layer implementation of [VerificationRepository]. Features are static.
 */
class VerificationRepositoryImpl @Inject constructor() : VerificationRepository {

    override suspend fun getFeatures(): List<VerificationFeature> = listOf(
        VerificationFeature(id = FeatureType.VerifiedList, isPrimary = false),
        VerificationFeature(id = FeatureType.Sync, isPrimary = false),
        VerificationFeature(id = FeatureType.Profile, isPrimary = false)
    )
}
