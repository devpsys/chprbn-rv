package ng.com.chprbn.mobile.feature.auth.data.repository

import com.google.gson.Gson
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.feature.auth.data.api.AuthApiService
import ng.com.chprbn.mobile.feature.auth.data.dto.ApiErrorDto
import ng.com.chprbn.mobile.feature.auth.data.dto.LoginRequestDto
import ng.com.chprbn.mobile.feature.auth.data.connectivity.ConnectivityChecker
import ng.com.chprbn.mobile.feature.auth.data.local.UserDao
import ng.com.chprbn.mobile.feature.auth.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.auth.data.mappers.toEntity
import ng.com.chprbn.mobile.feature.auth.data.network.AuthTokenStore
import ng.com.chprbn.mobile.feature.auth.domain.model.AuthResult
import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.auth.domain.repository.AuthRepository

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val userDao: UserDao,
    private val gson: Gson,
    private val connectivityChecker: ConnectivityChecker,
    private val authTokenStore: AuthTokenStore
) : AuthRepository {

    override suspend fun login(username: String, password: String): AuthResult {
        val trimmedUsername = username.trim()
        val cachedUser = getCachedUser(trimmedUsername)

        if (!connectivityChecker.isConnected()) {
            return cachedUser?.let {
                authTokenStore.setToken(it.accessToken)
                AuthResult.Success(it)
            } ?: AuthResult.Error("No cached session available for offline login.")
        }

        return try {
            authTokenStore.clear()
            val response = apiService.login(
                LoginRequestDto(username = trimmedUsername, password = password)
            )

            if (!response.isSuccessful) {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                    ?: response.message().ifEmpty { "Login failed." }
                return AuthResult.Error(errorMessage)
            }

            val envelope = response.body()
            val token = envelope?.data?.token
            if (token.isNullOrBlank()) {
                return AuthResult.Error(envelope?.message ?: "Invalid login response.")
            }

            authTokenStore.setToken(token)

            val profileResponse = apiService.getCurrentUser()
            if (!profileResponse.isSuccessful) {
                authTokenStore.clear()
                val err = parseErrorMessage(profileResponse.errorBody()?.string())
                    ?: profileResponse.message().ifEmpty { "Could not load profile." }
                return AuthResult.Error(err)
            }

            val profileEnvelope = profileResponse.body()
            val profileData = profileEnvelope?.data
            if (profileData == null || profileEnvelope.status != true) {
                authTokenStore.clear()
                return AuthResult.Error(profileEnvelope?.message ?: "Invalid profile response.")
            }

            val domainUser = profileData.toDomain(accessToken = token).copy(
                lastLoginAt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date())
            )

            withContext(Dispatchers.IO) { userDao.upsertUser(domainUser.toEntity()) }
            AuthResult.Success(domainUser)
        } catch (t: Throwable) {
            authTokenStore.clear()
            if (t is IOException || !connectivityChecker.isConnected()) {
                return cachedUser?.let {
                    authTokenStore.setToken(it.accessToken)
                    AuthResult.Success(it)
                }
                    ?: AuthResult.Error("Network unavailable. Please try again or use manual verification.")
            }
            AuthResult.Error(t.message ?: "Login failed.")
        }
    }

    private suspend fun getCachedUser(username: String): User? {
        val cached = withContext(Dispatchers.IO) { userDao.getUser() }
        return cached
            ?.takeIf { it.username.equals(username, ignoreCase = true) }
            ?.toDomain()
    }

    private fun parseErrorMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            gson.fromJson(raw, ApiErrorDto::class.java).message
        }.getOrNull()
    }
}
