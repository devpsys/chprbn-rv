package ng.com.chprbn.mobile.feature.auth.data.repository

import com.google.gson.Gson
import ng.com.chprbn.mobile.feature.auth.data.connectivity.ConnectivityChecker
import ng.com.chprbn.mobile.feature.auth.data.api.AuthApiService
import ng.com.chprbn.mobile.feature.auth.data.dto.ApiErrorDto
import ng.com.chprbn.mobile.feature.auth.data.dto.LoginRequestDto
import ng.com.chprbn.mobile.feature.auth.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.auth.data.mappers.toDomainUser
import ng.com.chprbn.mobile.feature.auth.data.mappers.toEntity
import ng.com.chprbn.mobile.feature.auth.data.local.UserDao
import ng.com.chprbn.mobile.feature.auth.domain.model.AuthResult
import ng.com.chprbn.mobile.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val userDao: UserDao,
    private val gson: Gson,
    private val connectivityChecker: ConnectivityChecker
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResult {
        val trimmedEmail = email.trim()
        val cachedUser = getCachedUser(trimmedEmail)

        // Offline login: use cached session only (password cannot be validated offline).
        if (!connectivityChecker.isConnected()) {
            return cachedUser?.let { AuthResult.Success(it) }
                ?: AuthResult.Error("No cached session available for offline login.")
        }

        return try {
            val response = apiService.login(
                request = LoginRequestDto(email = email, password = password)
            )

            if (response.isSuccessful) {
                val body = response.body() ?: return AuthResult.Error("Empty response from server.")

                val domainUser = body.toDomainUser().copy(
                    lastLoginAt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date())
                )
                // Cache authenticated user locally on success.
                withContext(Dispatchers.IO) { userDao.upsertUser(domainUser.toEntity()) }
                AuthResult.Success(domainUser)
            } else {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                    ?: response.message().ifEmpty { "Login failed." }
                AuthResult.Error(errorMessage)
            }
        } catch (t: Throwable) {
            // If we got a connectivity-related exception, attempt offline fallback.
            if (t is IOException || !connectivityChecker.isConnected()) {
                return cachedUser?.let { AuthResult.Success(it) }
                    ?: AuthResult.Error("Network unavailable. Please try again or use manual verification.")
            }
            AuthResult.Error(t.message ?: "Login failed.")
        }
    }

    private suspend fun getCachedUser(email: String): ng.com.chprbn.mobile.feature.auth.domain.model.User? {
        val cached = withContext(Dispatchers.IO) { userDao.getUser() }
        return cached
            ?.takeIf { it.email.equals(email, ignoreCase = true) }
            ?.toDomain()
    }

    private fun parseErrorMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            gson.fromJson(raw, ApiErrorDto::class.java).message
        }.getOrNull()
    }
}

