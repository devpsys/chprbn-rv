package ng.com.chprbn.mobile.feature.profile.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.profile.domain.repository.ProfileRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetUserProfileUseCaseTest {

    private lateinit var profileRepository: ProfileRepository
    private lateinit var useCase: GetUserProfileUseCase

    @Before
    fun setUp() {
        profileRepository = mockk()
        useCase = GetUserProfileUseCase(profileRepository)
    }

    @Test
    fun `invoke returns user from repository`() = runTest {
        val user = User(
            id = "adhoc_1",
            username = "johndoe",
            email = "john@example.com",
            fullName = "John Doe",
            accessToken = "tok",
            permissions = emptyList(),
            userPhoto = null
        )
        coEvery { profileRepository.getUserProfile() } returns user

        val result = useCase()

        assertEquals(user, result)
        coVerify(exactly = 1) { profileRepository.getUserProfile() }
    }

    @Test
    fun `invoke returns null when repository returns null`() = runTest {
        coEvery { profileRepository.getUserProfile() } returns null

        val result = useCase()

        assertNull(result)
    }
}
