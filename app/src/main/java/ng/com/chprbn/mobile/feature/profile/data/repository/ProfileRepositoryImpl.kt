package ng.com.chprbn.mobile.feature.profile.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.feature.auth.data.local.UserDao
import ng.com.chprbn.mobile.feature.auth.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.auth.data.mappers.toEntity
import ng.com.chprbn.mobile.feature.auth.data.network.AuthTokenStore
import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * Data layer implementation of [ProfileRepository].
 * Single source of truth: local UserDao (auth DB).
 */
class ProfileRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val authTokenStore: AuthTokenStore
) : ProfileRepository {

    override suspend fun getUserProfile(): User? {
        val entity = withContext(Dispatchers.IO) { userDao.getUser() } ?: return null
        val token = authTokenStore.peekToken() ?: return null
        return entity.toDomain(token)
    }

    override suspend fun updateUserProfile(user: User) {
        withContext(Dispatchers.IO) { userDao.upsertUser(user.toEntity()) }
    }

    override suspend fun logout() {
        withContext(Dispatchers.IO) { userDao.clearUser() }
        authTokenStore.clear()
    }
}
