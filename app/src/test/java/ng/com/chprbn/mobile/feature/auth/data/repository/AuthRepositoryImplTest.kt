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

    private lateinit var authRepository: AuthRepositoryImpl

    @Before
    fun setUp() {
        apiService = mockk()
        userDao = mockk(relaxed = true)
        gson = mockk()
        connectivityChecker = mockk()
        authTokenStore = mockk(relaxed = true)

        authRepository = AuthRepositoryImpl(
            apiService, userDao, gson, connectivityChecker, authTokenStore
        )
    }

    @Test
    fun `login returns success when network calls succeed`() = runTest {
        // Given
        every { connectivityChecker.isConnected() } returns true
        coEvery { userDao.getUser() } returns null // No cache

        val loginResponse = Response.success(
            LoginEnvelopeDto(
                status = true,
                message = "Success",
                data = LoginDataDto(token = "fakeToken")
            )
        )
        coEvery { apiService.adhocLogin(any()) } returns loginResponse

        val profileResponse = Response.success(
            AdhocProfileEnvelopeDto(
                status = true,
                message = "Success",
                data = AdhocProfileDataDto(
                    id = 1.0,
                    name = "John Doe",
                    email = "john@example.com",
                    username = "johndoe",
                    phone = "123456",
                    status = 1,
                    role = null,
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
                status = true,
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
                status = true,
                message = "OK",
                data = LoginDataDto(token = "freshToken")
            )
        )
        coEvery { apiService.getAdhocProfile() } returns Response.success(
            AdhocProfileEnvelopeDto(
                status = false,
                message = "Account suspended",
                data = AdhocProfileDataDto(
                    id = 1.0,
                    name = "John Doe",
                    email = "john@example.com",
                    username = "johndoe",
                    phone = null,
                    status = 0,
                    role = null,
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
    fun `login returns cached user when offline and cache has valid token`() = runTest {
        every { connectivityChecker.isConnected() } returns false

        val cached = UserEntity(
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
            lastLoginAt = "May 8, 9:00 AM"
        )
        coEvery { userDao.getUser() } returns cached
        every { authTokenStore.peekToken() } returns "valid-cached-token"

        // Username comparison is case-insensitive; pass mismatched case to verify.
        val result = authRepository.login("JOHNDOE", "password")

        assertTrue(result is AuthResult.Success)
        val user = (result as AuthResult.Success).user
        assertEquals("johndoe", user.username)
        assertEquals("John Doe", user.fullName)
        assertEquals("valid-cached-token", user.accessToken)
        // No network calls and no token mutation when serving from cache
        coVerify(exactly = 0) { apiService.adhocLogin(any()) }
        coVerify(exactly = 0) { apiService.getAdhocProfile() }
        coVerify(exactly = 0) { authTokenStore.setToken(any()) }
        coVerify(exactly = 0) { authTokenStore.clear() }
    }
}
