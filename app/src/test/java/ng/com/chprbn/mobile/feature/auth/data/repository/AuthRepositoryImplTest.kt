package ng.com.chprbn.mobile.feature.auth.data.repository

import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.auth.data.api.AuthApiService
import ng.com.chprbn.mobile.feature.auth.data.connectivity.ConnectivityChecker
import ng.com.chprbn.mobile.feature.auth.data.dto.AdhocProfileDataDto
import ng.com.chprbn.mobile.feature.auth.data.dto.AdhocProfileEnvelopeDto
import ng.com.chprbn.mobile.feature.auth.data.dto.LoginDataDto
import ng.com.chprbn.mobile.feature.auth.data.dto.LoginEnvelopeDto
import ng.com.chprbn.mobile.feature.auth.data.local.UserDao
import ng.com.chprbn.mobile.feature.auth.data.local.UserEntity
import ng.com.chprbn.mobile.feature.auth.data.network.AuthTokenStore
import ng.com.chprbn.mobile.feature.auth.data.network.PasswordVerifier
import ng.com.chprbn.mobile.feature.auth.domain.model.AuthResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AuthRepositoryImplTest {

    private lateinit var apiService: AuthApiService
    private lateinit var userDao: UserDao
    private lateinit var gson: Gson
    private lateinit var connectivityChecker: ConnectivityChecker
    private lateinit var authTokenStore: AuthTokenStore
    private lateinit var passwordVerifier: PasswordVerifier

    private lateinit var authRepository: AuthRepositoryImpl

    @Before
    fun setUp() {
        apiService = mockk()
        userDao = mockk(relaxed = true)
        gson = mockk()
        connectivityChecker = mockk()
        authTokenStore = mockk(relaxed = true)
        passwordVerifier = mockk()
        // Default: online login stores a fresh credential; offline verify defaults
        // to `false` so tests that don't opt in fail closed (matches production).
        every { passwordVerifier.hash(any()) } returns PasswordVerifier.Credential(
            saltB64 = "salt",
            verifierB64 = "verifier",
            algorithm = "PBKDF2-HMAC-SHA256:210000:256",
        )
        every { passwordVerifier.verify(any(), any()) } returns false

        authRepository = AuthRepositoryImpl(
            apiService, userDao, gson, connectivityChecker, authTokenStore, passwordVerifier,
        )
    }

    @Test
    fun `login returns success when network calls succeed`() = runTest {
        // Given
        every { connectivityChecker.isConnected() } returns true
        coEvery { userDao.getUser() } returns null // No cache

        val loginResponse = Response.success(
            LoginEnvelopeDto(
                success = true,
                message = "Success",
                data = LoginDataDto(token = "fakeToken")
            )
        )
        coEvery { apiService.adhocLogin(any()) } returns loginResponse

        val profileResponse = Response.success(
            AdhocProfileEnvelopeDto(
                success = true,
                message = "Success",
                data = AdhocProfileDataDto(
                    id = 1.0,
                    name = "John Doe",
                    email = "john@example.com",
                    username = "johndoe",
                    phone = "123456",
                    status = 1,
                    department = null
                )
            )
        )
        coEvery { apiService.getAdhocProfile() } returns profileResponse

        // When
        val result = authRepository.login("johndoe", "password")

        // Then
        assertTrue(result is AuthResult.Success)
        coVerify { authTokenStore.setToken("fakeToken") }
        coVerify { userDao.upsertUser(any()) }
    }

    @Test
    fun `login returns error when network unavailable and no cache`() = runTest {
        every { connectivityChecker.isConnected() } returns false
        coEvery { userDao.getUser() } returns null

        val result = authRepository.login("johndoe", "password")

        assertTrue(result is AuthResult.Error)
        val errorMessage = (result as AuthResult.Error).message
        assertTrue(errorMessage.contains("No cached session available"))
    }

    @Test
    fun `login clears token when profile fetch fails with HTTP error`() = runTest {
        every { connectivityChecker.isConnected() } returns true
        coEvery { userDao.getUser() } returns null

        coEvery { apiService.adhocLogin(any()) } returns Response.success(
            LoginEnvelopeDto(
                success = true,
                message = "OK",
                data = LoginDataDto(token = "freshToken")
            )
        )

        val emptyError = "".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { apiService.getAdhocProfile() } returns Response.error(500, emptyError)

        val result = authRepository.login("johndoe", "password")

        assertTrue(result is AuthResult.Error)
        // Token was set, then must be cleared after profile fetch fails. The upfront
        // clear() at login start is also expected — we assert the order of all three.
        coVerifyOrder {
            authTokenStore.clear()
            authTokenStore.setToken("freshToken")
            authTokenStore.clear()
        }
        coVerify(exactly = 0) { userDao.upsertUser(any()) }
    }

    @Test
    fun `login clears token and errors when profile envelope has status false`() = runTest {
        every { connectivityChecker.isConnected() } returns true
        coEvery { userDao.getUser() } returns null

        coEvery { apiService.adhocLogin(any()) } returns Response.success(
            LoginEnvelopeDto(
                success = true,
                message = "OK",
                data = LoginDataDto(token = "freshToken")
            )
        )
        coEvery { apiService.getAdhocProfile() } returns Response.success(
            AdhocProfileEnvelopeDto(
                success = false,
                message = "Account suspended",
                data = AdhocProfileDataDto(
                    id = 1.0,
                    name = "John Doe",
                    email = "john@example.com",
                    username = "johndoe",
                    phone = null,
                    status = 0,
                    department = null
                )
            )
        )

        val result = authRepository.login("johndoe", "password")

        assertTrue(result is AuthResult.Error)
        assertEquals("Account suspended", (result as AuthResult.Error).message)
        coVerifyOrder {
            authTokenStore.clear()
            authTokenStore.setToken("freshToken")
            authTokenStore.clear()
        }
        coVerify(exactly = 0) { userDao.upsertUser(any()) }
    }

    @Test
    fun `offline login succeeds when password verifies against stored credential`() = runTest {
        every { connectivityChecker.isConnected() } returns false

        val cached = cachedUserEntityWithCredential()
        coEvery { userDao.getUser() } returns cached
        every { authTokenStore.peekToken() } returns "valid-cached-token"
        every { passwordVerifier.verify(eq("password"), any()) } returns true

        // Username comparison is case-insensitive; pass mismatched case to verify.
        val result = authRepository.login("JOHNDOE", "password")

        assertTrue(result is AuthResult.Success)
        val user = (result as AuthResult.Success).user
        assertEquals("johndoe", user.username)
        assertEquals("John Doe", user.fullName)
        assertEquals("valid-cached-token", user.accessToken)
        coVerify(exactly = 0) { apiService.adhocLogin(any()) }
        coVerify(exactly = 0) { apiService.getAdhocProfile() }
        coVerify(exactly = 0) { authTokenStore.setToken(any()) }
        coVerify(exactly = 0) { authTokenStore.clear() }
    }

    @Test
    fun `offline login rejects a wrong password (does not fall through to Success)`() = runTest {
        every { connectivityChecker.isConnected() } returns false
        coEvery { userDao.getUser() } returns cachedUserEntityWithCredential()
        every { passwordVerifier.verify(any(), any()) } returns false

        val result = authRepository.login("johndoe", "wrong")

        assertTrue(result is AuthResult.Error)
        assertEquals("Incorrect username or password.", (result as AuthResult.Error).message)
        coVerify(exactly = 0) { authTokenStore.setToken(any()) }
    }

    @Test
    fun `offline login refuses cached user without a stored credential (pre-v8)`() = runTest {
        every { connectivityChecker.isConnected() } returns false
        // Row migrated from v7 has no PBKDF2 columns.
        coEvery { userDao.getUser() } returns cachedUserEntityWithCredential().copy(
            passwordSalt = null,
            passwordVerifier = null,
            passwordAlgorithm = null,
        )

        val result = authRepository.login("johndoe", "password")

        assertTrue(result is AuthResult.Error)
        assertTrue(
            (result as AuthResult.Error).message.contains("Sign in online", ignoreCase = true),
        )
        // Never attempted to derive a hash — the caller was refused before that.
        coVerify(exactly = 0) { passwordVerifier.verify(any(), any()) }
    }

    private fun cachedUserEntityWithCredential(): UserEntity = UserEntity(
        id = "adhoc_1",
        username = "johndoe",
        email = "john@example.com",
        fullName = "John Doe",
        permissions = emptyList(),
        userPhoto = null,
        role = "examiner",
        staffId = null,
        unit = null,
        organization = null,
        lastLoginAt = "May 8, 9:00 AM",
        passwordSalt = "salt",
        passwordVerifier = "verifier",
        passwordAlgorithm = "PBKDF2-HMAC-SHA256:210000:256",
    )
}
