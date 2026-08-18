package ng.com.chprbn.mobile.feature.auth.data

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.auth.data.local.UserDao
import ng.com.chprbn.mobile.feature.auth.data.local.UserEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UserAssessorProviderTest {

    private val userDao = mockk<UserDao>()
    private val provider = UserAssessorProvider(userDao)

    @Test
    fun `returns null when no user is cached`() = runTest {
        every { userDao.getUser() } returns null

        assertNull(provider.current())
    }

    @Test
    fun `returns null when the cached row predates schema v10 (no numeric assessorId)`() = runTest {
        every { userDao.getUser() } returns baseEntity(assessorId = null)

        assertNull(
            "provider must not synthesise an id — the server would reject on 'id and username' anyway",
            provider.current(),
        )
    }

    @Test
    fun `returns null when the cached username is blank`() = runTest {
        every { userDao.getUser() } returns baseEntity(username = " ")

        assertNull(provider.current())
    }

    @Test
    fun `maps every AdhocProfile-derived field onto the wire shape`() = runTest {
        every { userDao.getUser() } returns baseEntity()

        val assessor = provider.current()

        assertNotNull(assessor)
        assertEquals(12L, assessor!!.id)
        assertEquals("jane.field", assessor.username)
        assertEquals("Jane Field Officer", assessor.name)
        assertEquals("jane.field@example.com", assessor.email)
        assertEquals("08012345678", assessor.phone)
        assertEquals(1, assessor.status)
        assertEquals("ACC", assessor.department) // sourced from UserEntity.unit
        assertEquals("Lagos", assessor.location)
        assertEquals(listOf("Inspector", "Verify Practitioners"), assessor.roles)
    }

    private fun baseEntity(
        assessorId: Long? = 12L,
        username: String = "jane.field",
    ) = UserEntity(
        id = "adhoc_12",
        username = username,
        email = "jane.field@example.com",
        fullName = "Jane Field Officer",
        permissions = listOf("Inspector", "Verify Practitioners"),
        userPhoto = null,
        role = "Inspector",
        staffId = null,
        unit = "ACC",
        organization = null,
        lastLoginAt = null,
        location = "Lagos",
        passwordSalt = null,
        passwordVerifier = null,
        passwordAlgorithm = null,
        phone = "08012345678",
        status = 1,
        assessorId = assessorId,
    )
}
