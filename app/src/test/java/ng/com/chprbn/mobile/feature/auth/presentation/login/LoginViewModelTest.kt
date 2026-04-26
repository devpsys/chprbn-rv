package ng.com.chprbn.mobile.feature.auth.presentation.login

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.auth.domain.model.AuthResult
import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.auth.domain.usecase.LoginUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        loginUseCase = mockk()
        viewModel = LoginViewModel(loginUseCase)
    }

    @Test
    fun `signIn with empty credentials updates state with error`() = runTest {
        viewModel.uiState.test {
            // Initial state
            val initialState = awaitItem()
            assertNull(initialState.errorMessage)
            
            viewModel.signIn("", "pass")
            
            val errorState = awaitItem()
            assertEquals("Username and password are required.", errorState.errorMessage)
        }
    }

    @Test
    fun `signIn success updates state correctly`() = runTest {
        val user = User(
            id = "1",
            username = "testUser",
            email = "test@example.com",
            fullName = "Test User",
            accessToken = "token123",
            permissions = emptyList(),
            userPhoto = null
        )
        coEvery { loginUseCase("user", "pass") } returns AuthResult.Success(user)

        viewModel.uiState.test {
            // Initial
            awaitItem()

            viewModel.signIn("user", "pass")

            // Loading state
            val loadingState = awaitItem()
            assertEquals(true, loadingState.isLoading)

            // Success state
            val successState = awaitItem()
            assertEquals(false, successState.isLoading)
            assertEquals(user, successState.authenticatedUser)
            assertNull(successState.errorMessage)
        }
    }

    @Test
    fun `signIn failure updates state with error`() = runTest {
        coEvery { loginUseCase("wrong", "pass") } returns AuthResult.Error("Invalid credentials")

        viewModel.uiState.test {
            // Initial
            awaitItem()

            viewModel.signIn("wrong", "pass")

            // Loading state
            val loadingState = awaitItem()
            assertEquals(true, loadingState.isLoading)

            // Error state
            val errorState = awaitItem()
            assertEquals(false, errorState.isLoading)
            assertEquals("Invalid credentials", errorState.errorMessage)
            assertNull(errorState.authenticatedUser)
        }
    }
}
