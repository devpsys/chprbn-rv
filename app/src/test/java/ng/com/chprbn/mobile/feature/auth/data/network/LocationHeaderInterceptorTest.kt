package ng.com.chprbn.mobile.feature.auth.data.network

import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import ng.com.chprbn.mobile.feature.auth.data.local.UserDao
import ng.com.chprbn.mobile.feature.auth.data.local.UserEntity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Regression coverage for [LocationHeaderInterceptor] — it's the sole
 * request credential on the jarabawa client (`JarabawaNetworkModule`),
 * shared by every exam endpoint (dossier fetch, attendance batch, remarks
 * batch), so a bug here silently breaks all three identically.
 */
class LocationHeaderInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var userDao: UserDao

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        userDao = mockk()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun clientWith(userDao: UserDao): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(LocationHeaderInterceptor(userDao))
            .build()

    @Test
    fun `attaches x-location from the cached user`() {
        every { userDao.getUser() } returns userEntity(location = "mchst213")
        server.enqueue(MockResponse().setResponseCode(200))

        clientWith(userDao)
            .newCall(Request.Builder().url(server.url("/attendance/fetch-record")).build())
            .execute()
            .use { assertEquals(200, it.code) }

        assertEquals("mchst213", server.takeRequest().getHeader("x-location"))
    }

    @Test
    fun `throws instead of silently sending a request with no cached location`() {
        every { userDao.getUser() } returns userEntity(location = null)

        try {
            clientWith(userDao)
                .newCall(Request.Builder().url(server.url("/attendance/fetch-record")).build())
                .execute()
            fail("Expected IOException when no location is cached.")
        } catch (e: IOException) {
            // expected — see LocationHeaderInterceptor's fail-fast contract.
        }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `throws instead of silently sending a request with a blank location`() {
        every { userDao.getUser() } returns userEntity(location = "   ")

        try {
            clientWith(userDao)
                .newCall(Request.Builder().url(server.url("/attendance/fetch-record")).build())
                .execute()
            fail("Expected IOException when the cached location is blank.")
        } catch (e: IOException) {
            // expected
        }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `throws instead of silently sending a request when no user is cached at all`() {
        every { userDao.getUser() } returns null

        try {
            clientWith(userDao)
                .newCall(Request.Builder().url(server.url("/attendance/fetch-record")).build())
                .execute()
            fail("Expected IOException when no user row exists.")
        } catch (e: IOException) {
            // expected
        }
        assertEquals(0, server.requestCount)
    }

    private fun userEntity(location: String?) = UserEntity(
        id = "adhoc_1",
        username = "officer",
        email = "officer@chprbn.gov.ng",
        fullName = "Officer Name",
        permissions = emptyList(),
        userPhoto = null,
        location = location,
    )
}
