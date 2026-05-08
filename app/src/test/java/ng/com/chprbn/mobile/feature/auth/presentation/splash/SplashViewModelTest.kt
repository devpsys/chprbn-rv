package ng.com.chprbn.mobile.feature.auth.presentation.splash

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.auth.data.network.AuthTokenStore
import ng.com.chprbn.mobile.feature.auth.data.network.SessionTokenPolicy
import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.profile.domain.usecase.GetUserProfileUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getUserProfileUseCase: GetUserProfileUseCase
    private lateinit var authTokenStore: AuthTokenStore

    @Before
    fun setUp() {
        getUserProfileUseCase = mockk()
        authTokenStore = mockk(relaxed = true)
    }

    @Test
    fun `routes to Verification when cached user has valid token`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { getUserProfileUseCase() } returns cachedUser(token = "valid-token-123")

            val viewModel = SplashViewModel(getUserProfileUseCase, authTokenStore)
            advanceUntilIdle()

            assertEquals(SplashDestination.Verification, viewModel.destination.value)
            verify { authTokenStore.setToken("valid-token-123") }
        }

    @Test
    fun `routes to Login and clears token when no cached user`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { getUserProfileUseCase() } returns null

            val viewModel = SplashViewModel(getUserProfileUseCase, authTokenStore)
            advanceUntilIdle()

            assertEquals(SplashDestination.Login, viewModel.destination.value)
            verify { authTokenStore.clear() }
        }

    @Test
    fun `routes to Login when cached user has legacy seed placeholder token`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { getUserProfileUseCase() } returns cachedUser(
                token = SessionTokenPolicy.LEGACY_SEED_PLACEHOLDER
            )

            val viewModel = SplashViewModel(getUserProfileUseCase, authTokenStore)
            advanceUntilIdle()

            assertEquals(SplashDestination.Login, viewModel.destination.value)
            verify { authTokenStore.clear() }
        }

    @Test
    fun `destination is null before splash delay elapses`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { getUserProfileUseCase() } returns null

            val viewModel = SplashViewModel(getUserProfileUseCase, authTokenStore)
            // Deliberately do not advance virtual time — delay(2500L) is still pending.

            assertNull(viewModel.destination.value)
        }

    private fun cachedUser(token: String) = User(
        id = "adhoc_1",
        username = "johndoe",
        email = "john@example.com",
        fullName = "John Doe",
        accessToken = token,
        permissions = emptyList(),
        userPhoto = null
    )
}
