package ng.com.chprbn.mobile.feature.profile.data.repository

import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.auth.data.local.UserDao
import ng.com.chprbn.mobile.feature.auth.data.local.UserEntity
import ng.com.chprbn.mobile.feature.auth.data.network.AuthTokenStore
import ng.com.chprbn.mobile.feature.auth.data.network.SessionTokenPolicy
import ng.com.chprbn.mobile.feature.auth.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ProfileRepositoryImplTest {

    private lateinit var userDao: UserDao
    private lateinit var authTokenStore: AuthTokenStore
    private lateinit var repository: ProfileRepositoryImpl

    @Before
    fun setUp() {
        userDao = mockk(relaxed = true)
        authTokenStore = mockk(relaxed = true)
        repository = ProfileRepositoryImpl(userDao, authTokenStore)
    }

    @Test
    fun `getUserProfile returns null when DAO has no user`() = runTest {
        every { userDao.getUser() } returns null

        val result = repository.getUserProfile()

        assertNull(result)
        // Token lookup should be short-circuited when no cached user exists.
        verify(exactly = 0) { authTokenStore.peekToken() }
    }

    @Test
    fun `getUserProfile returns null when token is missing`() = runTest {
        every { userDao.getUser() } returns sampleEntity()
        every { authTokenStore.peekToken() } returns null

        val result = repository.getUserProfile()

        assertNull(result)
    }

    @Test
    fun `getUserProfile returns null when token is the legacy seed placeholder`() = runTest {
        every { userDao.getUser() } returns sampleEntity()
        every { authTokenStore.peekToken() } returns SessionTokenPolicy.LEGACY_SEED_PLACEHOLDER

        val result = repository.getUserProfile()

        assertNull(result)
    }

    @Test
    fun `getUserProfile returns null when token is blank`() = runTest {
        every { userDao.getUser() } returns sampleEntity()
        every { authTokenStore.peekToken() } returns "   "

        val result = repository.getUserProfile()

        assertNull(result)
    }

    @Test
    fun `getUserProfile trims whitespace around the token`() = runTest {
        every { userDao.getUser() } returns sampleEntity()
        every { authTokenStore.peekToken() } returns "  real-token  "

        val result = repository.getUserProfile()

        assertEquals("real-token", result?.accessToken)
    }

    @Test
    fun `getUserProfile maps entity with token to domain User`() = runTest {
        every { userDao.getUser() } returns sampleEntity()
        every { authTokenStore.peekToken() } returns "real-token"

        val result = repository.getUserProfile()

        assertEquals("johndoe", result?.username)
        assertEquals("real-token", result?.accessToken)
        assertEquals("John Doe", result?.fullName)
        assertEquals("examiner", result?.role)
    }

    @Test
    fun `updateUserProfile maps user to entity and upserts via DAO`() = runTest {
        val captured = slot<UserEntity>()
        every { userDao.upsertUser(capture(captured)) } returns Unit

        val user = User(
            id = "adhoc_1",
            username = "johndoe",
            email = "john@example.com",
            fullName = "John Doe",
            accessToken = "tok",
            permissions = emptyList(),
            userPhoto = null,
            role = "examiner",
            unit = "Cardiology"
        )

        repository.updateUserProfile(user)

        assertEquals("adhoc_1", captured.captured.id)
        assertEquals("johndoe", captured.captured.username)
        assertEquals("examiner", captured.captured.role)
        assertEquals("Cardiology", captured.captured.unit)
    }

    @Test
    fun `logout clears DAO then token store in order`() = runTest {
        repository.logout()

        coVerifyOrder {
            userDao.clearUser()
            authTokenStore.clear()
        }
    }

    private fun sampleEntity() = UserEntity(
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
        lastLoginAt = null
    )
}
