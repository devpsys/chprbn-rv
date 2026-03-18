package ng.com.chprbn.mobile.feature.dashboard.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.feature.auth.data.local.UserDao
import ng.com.chprbn.mobile.feature.auth.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.dashboard.domain.model.DashboardFeature
import ng.com.chprbn.mobile.feature.dashboard.domain.model.FeatureType
import ng.com.chprbn.mobile.feature.dashboard.domain.repository.DashboardRepository
import javax.inject.Inject

/**
 * Data layer implementation of [DashboardRepository].
 * Single source of truth: local cache (UserDao from auth). Features are static; remote sync can be added later.
 */
class DashboardRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : DashboardRepository {

    override suspend fun getUserProfile() = withContext(Dispatchers.IO) {
        userDao.getUser()?.toDomain()
    }

    override suspend fun getFeatures(): List<DashboardFeature> = withContext(Dispatchers.IO) {
        listOf(
//            DashboardFeature(
//                id = FeatureType.ScanQr,
//                title = "Scan License QR",
//                subtitle = "Validate practitioner credentials",
//                isPrimary = true
//            ),
            DashboardFeature(
                id = FeatureType.VerifiedList,
                title = "Verified Practitioners",
                subtitle = "Search secure database",
                isPrimary = false
            ),
            DashboardFeature(
                id = FeatureType.Sync,
                title = "Sync Records",
                subtitle = "Last sync: 2 mins ago",
                isPrimary = false
            ),
            DashboardFeature(
                id = FeatureType.Profile,
                title = "Profile",
                subtitle = "Settings and identity",
                isPrimary = false
            )
        )
    }
}
