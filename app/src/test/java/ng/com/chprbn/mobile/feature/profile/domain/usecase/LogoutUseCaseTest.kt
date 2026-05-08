package ng.com.chprbn.mobile.feature.profile.domain.usecase

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.profile.domain.repository.ProfileRepository
import org.junit.Before
import org.junit.Test

class LogoutUseCaseTest {

    private lateinit var profileRepository: ProfileRepository
    private lateinit var logoutUseCase: LogoutUseCase

    @Before
    fun setUp() {
        profileRepository = mockk(relaxed = true)
        logoutUseCase = LogoutUseCase(profileRepository)
    }

    @Test
    fun `invoke delegates to repository logout exactly once`() = runTest {
        logoutUseCase()

        coVerify(exactly = 1) { profileRepository.logout() }
    }
}
