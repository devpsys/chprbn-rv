package ng.com.chprbn.mobile.feature.auth.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.auth.domain.model.AuthResult
import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.auth.domain.repository.AuthRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        loginUseCase = LoginUseCase(authRepository)
    }

    @Test
    fun `invoke calls repository and returns success`() = runTest {
        val user = User(
            id = "1",
            username = "testUser",
            email = "test@example.com",
            fullName = "Test User",
            accessToken = "token123",
            permissions = emptyList(),
            userPhoto = null
        )
        coEvery { authRepository.login("user", "pass") } returns AuthResult.Success(user)

        val result = loginUseCase("user", "pass")

        assertEquals(AuthResult.Success(user), result)
        coVerify(exactly = 1) { authRepository.login("user", "pass") }
    }

    @Test
    fun `invoke calls repository and returns error`() = runTest {
        val errorResult = AuthResult.Error("Invalid credentials")
        coEvery { authRepository.login("wrong", "pass") } returns errorResult

        val result = loginUseCase("wrong", "pass")

        assertEquals(errorResult, result)
        coVerify(exactly = 1) { authRepository.login("wrong", "pass") }
    }
}
