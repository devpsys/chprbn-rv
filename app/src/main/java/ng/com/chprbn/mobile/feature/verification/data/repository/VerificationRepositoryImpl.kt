package ng.com.chprbn.mobile.feature.verification.data.repository

import ng.com.chprbn.mobile.feature.verification.data.source.OfficerRemarkOptionsRemoteSource
import ng.com.chprbn.mobile.feature.verification.domain.model.FeatureType
import ng.com.chprbn.mobile.feature.verification.domain.model.VerificationFeature
import ng.com.chprbn.mobile.feature.verification.domain.repository.VerificationRepository
import javax.inject.Inject

/**
 * Data layer implementation of [VerificationRepository]. Features are static.
 * Officer-remark options are fetched from the remote source; an empty list is
 * returned on any failure so the presentation layer falls back to bundled
 * defaults.
 */
class VerificationRepositoryImpl @Inject constructor(
    private val officerRemarkOptionsRemoteSource: OfficerRemarkOptionsRemoteSource,
) : VerificationRepository {

    override suspend fun getFeatures(): List<VerificationFeature> = listOf(
        VerificationFeature(id = FeatureType.VerifiedList, isPrimary = false),
        VerificationFeature(id = FeatureType.Sync, isPrimary = false),
        VerificationFeature(id = FeatureType.Profile, isPrimary = false)
    )

    override suspend fun getOfficerRemarkOptions(): List<String> =
        officerRemarkOptionsRemoteSource.fetchOptions().orEmpty()
}
