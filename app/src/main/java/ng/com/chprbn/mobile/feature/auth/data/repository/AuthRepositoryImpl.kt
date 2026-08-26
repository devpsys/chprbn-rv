package ng.com.chprbn.mobile.feature.auth.data.repository

import com.google.gson.Gson
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import ng.com.chprbn.mobile.core.network.toUserFacingMessage
import ng.com.chprbn.mobile.feature.auth.data.api.AuthApiService
import ng.com.chprbn.mobile.feature.auth.data.dto.ApiErrorDto
import ng.com.chprbn.mobile.feature.auth.data.dto.LoginRequestDto
import ng.com.chprbn.mobile.feature.auth.data.connectivity.ConnectivityChecker
import ng.com.chprbn.mobile.feature.auth.data.local.UserDao
import ng.com.chprbn.mobile.feature.auth.data.local.UserEntity
import ng.com.chprbn.mobile.feature.auth.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.auth.data.mappers.toEntity
import ng.com.chprbn.mobile.feature.auth.data.network.AuthTokenStore
import ng.com.chprbn.mobile.feature.auth.data.network.PasswordVerifier
import ng.com.chprbn.mobile.feature.auth.data.network.SessionTokenPolicy
import ng.com.chprbn.mobile.feature.auth.domain.model.AuthResult
import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.auth.domain.repository.AuthRepository

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val userDao: UserDao,
    private val gson: Gson,
    private val connectivityChecker: ConnectivityChecker,
    private val authTokenStore: AuthTokenStore,
    private val passwordVerifier: PasswordVerifier,
) : AuthRepository {

    override suspend fun login(username: String, password: String): AuthResult {
        val trimmedUsername = username.trim()
        val cachedEntity = withContext(Dispatchers.IO) { userDao.getUser() }

        if (!connectivityChecker.isConnected()) {
            return offlineLogin(trimmedUsername, password, cachedEntity)
        }

        return try {
            authTokenStore.clear()
            val response = apiService.adhocLogin(
                LoginRequestDto(username = trimmedUsername, password = password)
            )

            if (!response.isSuccessful) {
                val errorMessage = parseErrorMessage(response.errorBody()?.string())
                    ?: response.message().ifEmpty { "Login failed." }
                return AuthResult.Error(errorMessage)
            }

            val envelope = response.body()
            val token = envelope?.data?.token
            if (envelope?.success != true || token.isNullOrBlank()) {
                return AuthResult.Error(envelope?.message ?: "Invalid login response.")
            }

            authTokenStore.setToken(token.trim())

            val profileResponse = apiService.getAdhocProfile()
            if (!profileResponse.isSuccessful) {
                authTokenStore.clear()
                val err = parseErrorMessage(profileResponse.errorBody()?.string())
                    ?: profileResponse.message().ifEmpty { "Could not load profile." }
                return AuthResult.Error(err)
            }

            val profileEnvelope = profileResponse.body()
            val profileData = profileEnvelope?.data
            if (profileData == null || profileEnvelope.success != true) {
                authTokenStore.clear()
                return AuthResult.Error(profileEnvelope?.message ?: "Invalid profile response.")
            }

            val domainUser = profileData.toDomain(accessToken = token).copy(
                lastLoginAt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date())
            )

            // Persist a fresh password verifier alongside the cached user so
            // future offline logins can prove password knowledge (fixes the
            // pre-A1 hole where any password unlocked a cached username).
            val credential = passwordVerifier.hash(password)
            val entity = domainUser.toEntity().copy(
                passwordSalt = credential.saltB64,
                passwordVerifier = credential.verifierB64,
                passwordAlgorithm = credential.algorithm,
            )
            withContext(Dispatchers.IO) { userDao.upsertUser(entity) }
            AuthResult.Success(domainUser)
        } catch (t: Throwable) {
            authTokenStore.clear()
            if (t is IOException || !connectivityChecker.isConnected()) {
                return offlineLogin(trimmedUsername, password, cachedEntity)
            }
            // Never surface `t.message` directly — a socket / DNS / TLS
            // failure carries the backend hostname or a cert chain string.
            AuthResult.Error(t.toUserFacingMessage(default = "Login failed."))
        }
    }

    /**
     * Offline path: succeeds only when the cached user matches the entered
     * username AND the entered password verifies against the stored PBKDF2
     * credential AND the cached token still passes [SessionTokenPolicy].
     *
     * Users cached under schema v7 have no verifier (null columns) — they
     * are refused offline login and must sign in online at least once so
     * the v8 credential can be materialised.
     */
    private suspend fun offlineLogin(
        username: String,
        password: String,
        cachedEntity: UserEntity?,
    ): AuthResult {
        if (cachedEntity == null) {
            return AuthResult.Error("No cached session available for offline login.")
        }
        if (!cachedEntity.username.equals(username, ignoreCase = true)) {
            return AuthResult.Error("No cached session available for offline login.")
        }
        val salt = cachedEntity.passwordSalt
        val verifier = cachedEntity.passwordVerifier
        val algorithm = cachedEntity.passwordAlgorithm
        if (salt == null || verifier == null || algorithm == null) {
            return AuthResult.Error(
                "Sign in online at least once so this device can verify offline logins.",
            )
        }
        val ok = passwordVerifier.verify(
            password = password,
            credential = PasswordVerifier.Credential(
                saltB64 = salt,
                verifierB64 = verifier,
                algorithm = algorithm,
            ),
        )
        if (!ok) {
            return AuthResult.Error("Incorrect username or password.")
        }
        val token = authTokenStore.peekToken()?.trim()
            ?.takeIf { SessionTokenPolicy.isValidForAuthenticatedApi(it) }
            ?: return AuthResult.Error(
                "Session expired. Please connect to sign in again.",
            )
        return AuthResult.Success(cachedEntity.toDomain(token))
    }

    private fun parseErrorMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            gson.fromJson(raw, ApiErrorDto::class.java).message
        }.getOrNull()
    }
}
