package ng.com.chprbn.mobile.feature.auth.data.api

import com.google.gson.JsonParser
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.auth.data.dto.LoginRequestDto
import ng.com.chprbn.mobile.testing.mockServerApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * JSON-wire integration tests for [AuthApiService]. Pins the camelCase
 * field names on the login request, the path / method, and the envelope
 * parsing for both `adhocLogin` and `getAdhocProfile`.
 */
class AuthApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: AuthApiService

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = mockServerApi(server, AuthApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // region adhocLogin

    @Test
    fun `adhocLogin posts to adhoc-login with camelCase username and password fields`() = runTest {
        server.enqueue(jsonOk("""{"status":true,"data":{"token":"tok"}}"""))

        api.adhocLogin(LoginRequestDto(username = "field.officer", password = "S3cret"))

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/adhoc/login", recorded.path)
        assertEquals("application/json; charset=UTF-8", recorded.getHeader("Content-Type"))

        val body = JsonParser.parseString(recorded.body.readUtf8()).asJsonObject
        assertEquals("field.officer", body["username"].asString)
        assertEquals("S3cret", body["password"].asString)
    }

    @Test
    fun `adhocLogin parses status + message + data token envelope`() = runTest {
        server.enqueue(
            jsonOk(
                """
                {
                  "status": true,
                  "message": "Login successful.",
                  "data": { "token": "1|aBcDeFgHiJkLmNoPqRsTuVwXyZ0123456789ABCDEFG" }
                }
                """.trimIndent(),
            ),
        )

        val response = api.adhocLogin(LoginRequestDto("u", "p"))

        assertTrue(response.isSuccessful)
        val body = response.body()!!
        assertTrue(body.status)
        assertEquals("Login successful.", body.message)
        assertEquals("1|aBcDeFgHiJkLmNoPqRsTuVwXyZ0123456789ABCDEFG", body.data!!.token)
    }

    @Test
    fun `adhocLogin 401 surfaces error body without throwing`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"message":"Invalid email or password."}"""),
        )

        val response = api.adhocLogin(LoginRequestDto("wrong", "creds"))

        assertFalse(response.isSuccessful)
        assertEquals(401, response.code())
        assertTrue(
            response.errorBody()?.string().orEmpty().contains("Invalid email or password."),
        )
    }

    // endregion

    // region getAdhocProfile

    @Test
    fun `getAdhocProfile GETs adhoc-profile and parses every documented field`() = runTest {
        server.enqueue(
            jsonOk(
                """
                {
                  "status": true,
                  "message": "successful",
                  "data": {
                    "id": 4,
                    "name": "Abba El-Muqaddas",
                    "email": "abba@chprbn.gpv.ng",
                    "phone": "08022334455",
                    "username": "abba",
                    "status": 1,
                    "department": "ACC",
                    "roles": ["Inspector", "Verify Practitioners"]
                  }
                }
                """.trimIndent(),
            ),
        )

        val response = api.getAdhocProfile()

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/adhoc/profile", recorded.path)

        assertTrue(response.isSuccessful)
        val envelope = response.body()!!
        assertTrue(envelope.status)
        assertEquals("successful", envelope.message)
        val data = envelope.data!!
        assertEquals(4.0, data.id)
        assertEquals("Abba El-Muqaddas", data.name)
        assertEquals("abba@chprbn.gpv.ng", data.email)
        assertEquals("08022334455", data.phone)
        assertEquals("abba", data.username)
        assertEquals(1, data.status)
        assertEquals("ACC", data.department)
        assertEquals(listOf("Inspector", "Verify Practitioners"), data.roles)
    }

    @Test
    fun `getAdhocProfile tolerates missing optional fields`() = runTest {
        server.enqueue(
            jsonOk(
                """
                {
                  "status": true,
                  "data": { "name": "Field Officer", "email": "f@x.gov", "username": "f" }
                }
                """.trimIndent(),
            ),
        )

        val response = api.getAdhocProfile()

        val data = response.body()!!.data!!
        assertEquals("Field Officer", data.name)
        assertNull(data.phone)
        assertNull(data.status)
        assertNull(data.department)
        assertNull(data.id)
        // `roles` is declared nullable on the DTO precisely to dodge the
        // Gson + Kotlin-defaults gotcha — when omitted on the wire it lands as
        // null, and the mapper `.orEmpty()`s it before the domain `User` sees it.
        assertNull(data.roles)
    }

    // endregion

    private fun jsonOk(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(body)
            .also { assertNotNull(it) }
}
