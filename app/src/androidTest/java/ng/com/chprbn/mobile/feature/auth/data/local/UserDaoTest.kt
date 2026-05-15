package ng.com.chprbn.mobile.feature.auth.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    private lateinit var db: AuthDatabase
    private lateinit var dao: UserDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AuthDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.userDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertReplacesExistingRow() {
        dao.upsertUser(user(id = "u1", fullName = "Original"))
        dao.upsertUser(user(id = "u1", fullName = "Renamed"))

        val row = dao.getUser()

        assertNotNull(row)
        assertEquals("Renamed", row!!.fullName)
    }

    @Test
    fun getUserReturnsNullWhenEmpty() {
        assertNull(dao.getUser())
    }

    @Test
    fun getUserReturnsTheSingleCachedSession() {
        // The DAO query is `LIMIT 1`; the table is treated as single-row.
        dao.upsertUser(user(id = "u1"))

        assertEquals("u1", dao.getUser()?.id)
    }

    @Test
    fun clearUserWipesTheCachedSession() {
        dao.upsertUser(user(id = "u1"))

        dao.clearUser()

        assertNull(dao.getUser())
    }

    @Test
    fun permissionsListPersistsAcrossRoundTrip() {
        dao.upsertUser(user(id = "u1", permissions = listOf("scan", "verify", "sync")))

        val row = dao.getUser()!!

        assertEquals(listOf("scan", "verify", "sync"), row.permissions)
    }

    private fun user(
        id: String,
        fullName: String? = "Officer Michael Chen",
        permissions: List<String> = emptyList(),
    ) = UserEntity(
        id = id,
        username = "MED-12345",
        email = "officer@regulator.gov",
        fullName = fullName,
        permissions = permissions,
        userPhoto = null,
        role = "Senior Field Officer",
        staffId = "S-001",
        unit = "Lagos Field Office",
        organization = "CHPRBN",
        lastLoginAt = "2026-05-15T10:30:00Z",
    )
}
