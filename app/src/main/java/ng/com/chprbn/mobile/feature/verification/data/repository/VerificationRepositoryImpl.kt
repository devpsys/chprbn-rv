package ng.com.chprbn.mobile.feature.verification.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.feature.auth.data.local.UserDao
import ng.com.chprbn.mobile.feature.auth.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.auth.data.network.AuthTokenStore
import ng.com.chprbn.mobile.feature.verification.domain.model.VerificationFeature
import ng.com.chprbn.mobile.feature.verification.domain.model.FeatureType
import ng.com.chprbn.mobile.feature.verification.domain.repository.VerificationRepository
import javax.inject.Inject

/**
 * Data layer implementation of [VerificationRepository].
 * Single source of truth: local cache (UserDao from auth). Features are static; remote sync can be added later.
 */
class VerificationRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val authTokenStore: AuthTokenStore
) : VerificationRepository {

    override suspend fun getUserProfile() = withContext(Dispatchers.IO) {
        userDao.getUser()
    }?.let { entity ->
        authTokenStore.peekToken()?.let { token ->
            entity.toDomain(token)
        }
    }

    override suspend fun getFeatures(): List<VerificationFeature> = listOf(
//            VerificationFeature(
//                id = FeatureType.ScanQr,
//                title = "Scan License QR",
//                subtitle = "Validate practitioner credentials",
//                isPrimary = true
//            ),
            VerificationFeature(
                id = FeatureType.VerifiedList,
                title = "Verified Practitioners",
                subtitle = "Search secure database",
                isPrimary = false
            ),
            VerificationFeature(
                id = FeatureType.Sync,
                title = "Sync Records",
                subtitle = "Last sync: 2 mins ago",
                isPrimary = false
            ),
            VerificationFeature(
                id = FeatureType.Profile,
                title = "Profile",
                subtitle = "Settings and identity",
                isPrimary = false
            )
        )
}
