package ng.com.chprbn.mobile.feature.auth.data.repository

import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
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
import ng.com.chprbn.mobile.feature.auth.data.network.AuthTokenStore
import ng.com.chprbn.mobile.feature.auth.domain.model.AuthResult
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
}
