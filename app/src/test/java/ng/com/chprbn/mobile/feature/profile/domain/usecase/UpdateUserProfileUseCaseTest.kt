package ng.com.chprbn.mobile.feature.profile.domain.usecase

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.profile.domain.repository.ProfileRepository
import org.junit.Before
import org.junit.Test

class UpdateUserProfileUseCaseTest {

    private lateinit var profileRepository: ProfileRepository
    private lateinit var useCase: UpdateUserProfileUseCase

    @Before
    fun setUp() {
        profileRepository = mockk(relaxed = true)
        useCase = UpdateUserProfileUseCase(profileRepository)
    }

    @Test
    fun `invoke delegates to repository updateUserProfile exactly once`() = runTest {
        val user = User(
            id = "adhoc_1",
            username = "johndoe",
            email = "john@example.com",
            fullName = "John Doe",
            accessToken = "tok",
            permissions = emptyList(),
            userPhoto = null
        )

        useCase(user)

        coVerify(exactly = 1) { profileRepository.updateUserProfile(user) }
    }
}
