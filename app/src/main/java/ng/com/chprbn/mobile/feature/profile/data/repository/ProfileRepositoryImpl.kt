package ng.com.chprbn.mobile.feature.profile.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        // Explicit product decision: logout clears only the auth session
        // (cached user row + token) and leaves every feature's downloaded
        // records (exam dossier, assessment packages, verification cache)
        // in place. This used to also wipe every feature's cache via
        // SessionCleaner (A2 audit finding, shared-device threat model) —
        // that call was removed on request; SessionCleaner/SessionScopedCleaner
        // are unused now but kept in case a distinct "switch account" /
        // "wipe all data" flow wants them later.
        withContext(Dispatchers.IO) { userDao.clearUser() }
        authTokenStore.clear()
    }
}
