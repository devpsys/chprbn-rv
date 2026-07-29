package ng.com.chprbn.mobile.feature.profile.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.session.SessionCleaner
import ng.com.chprbn.mobile.feature.auth.data.local.UserDao
import ng.com.chprbn.mobile.feature.auth.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.auth.data.mappers.toEntity
import ng.com.chprbn.mobile.feature.auth.data.network.AuthTokenStore
import ng.com.chprbn.mobile.feature.auth.data.network.SessionTokenPolicy
import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Data layer implementation of [ProfileRepository].
 * Single source of truth: local UserDao (auth DB).
 */
class ProfileRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val authTokenStore: AuthTokenStore,
    private val sessionCleaner: SessionCleaner,
) : ProfileRepository {

    override suspend fun getUserProfile(): User? {
        val entity = withContext(Dispatchers.IO) { userDao.getUser() } ?: return null
        // A blank token or the legacy seed placeholder must not surface a User —
        // any caller (e.g. ProfileViewModel) treats a non-null User as "logged in".
        val token = authTokenStore.peekToken()
            ?.trim()
            ?.takeIf { SessionTokenPolicy.isValidForAuthenticatedApi(it) }
            ?: return null
        return entity.toDomain(token)
    }

    override suspend fun updateUserProfile(user: User) {
        withContext(Dispatchers.IO) { userDao.upsertUser(user.toEntity()) }
    }

    override suspend fun logout() {
        // Wipe every feature's cached rows + the shared sync queue first, so
        // the next user on this device never inherits the previous user's
        // exam / assessment / verification data (A2 audit finding). Auth-side
        // state (UserDao + token) is cleared last — any exception raised by
        // a feature cleaner is logged inside SessionCleaner and does not
        // block the auth wipe.
        sessionCleaner.clearAllFeatures()
        withContext(Dispatchers.IO) { userDao.clearUser() }
        authTokenStore.clear()
    }
}
