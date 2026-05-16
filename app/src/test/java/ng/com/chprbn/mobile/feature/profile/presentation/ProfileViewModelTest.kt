package ng.com.chprbn.mobile.feature.profile.presentation

import android.content.Context
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.profile.domain.usecase.GetUserProfileUseCase
import ng.com.chprbn.mobile.feature.profile.domain.usecase.LogoutUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getUserProfileUseCase: GetUserProfileUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var context: Context

    @Before
    fun setUp() {
        getUserProfileUseCase = mockk()
        logoutUseCase = mockk(relaxed = true)
        context = mockk {
            every { getString(R.string.profile_error_not_logged_in) } returns "Not logged in"
            every { getString(R.string.profile_error_unknown) } returns "Unknown error"
            every { getString(R.string.profile_error_logout_failed) } returns "Logout failed"
        }
    }

    @Test
    fun `init emits Success when cached user is present`() = runTest {
        coEvery { getUserProfileUseCase() } returns sampleUser()

        val viewModel = ProfileViewModel(getUserProfileUseCase, logoutUseCase, context)

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state is ProfileUiState.Success)
            assertEquals("johndoe", (state as ProfileUiState.Success).user.username)
        }
    }

    @Test
    fun `init emits Error when no cached user`() = runTest {
        coEvery { getUserProfileUseCase() } returns null

        val viewModel = ProfileViewModel(getUserProfileUseCase, logoutUseCase, context)

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state is ProfileUiState.Error)
            assertEquals("Not logged in", (state as ProfileUiState.Error).message)
        }
    }

    @Test
    fun `init emits Error when use case throws`() = runTest {
        coEvery { getUserProfileUseCase() } throws RuntimeException("DB unavailable")

        val viewModel = ProfileViewModel(getUserProfileUseCase, logoutUseCase, context)

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state is ProfileUiState.Error)
            assertEquals("DB unavailable", (state as ProfileUiState.Error).message)
        }
    }

    @Test
    fun `logout success emits LoggedOut`() = runTest {
        coEvery { getUserProfileUseCase() } returns sampleUser()

        val viewModel = ProfileViewModel(getUserProfileUseCase, logoutUseCase, context)

        viewModel.state.test {
            awaitItem() // initial Success
            viewModel.logout()
            assertEquals(ProfileUiState.LoggedOut, awaitItem())
        }
        coVerify(exactly = 1) { logoutUseCase() }
    }

    @Test
    fun `logout failure emits Error with thrown message`() = runTest {
        coEvery { getUserProfileUseCase() } returns sampleUser()
        coEvery { logoutUseCase() } throws RuntimeException("Network gone")

        val viewModel = ProfileViewModel(getUserProfileUseCase, logoutUseCase, context)

        viewModel.state.test {
            awaitItem() // initial Success
            viewModel.logout()
            val state = awaitItem()
            assertTrue(state is ProfileUiState.Error)
            assertEquals("Network gone", (state as ProfileUiState.Error).message)
        }
    }

    private fun sampleUser() = User(
        id = "adhoc_1",
        username = "johndoe",
        email = "john@example.com",
        fullName = "John Doe",
        accessToken = "tok",
        permissions = emptyList(),
        userPhoto = null
    )
}
