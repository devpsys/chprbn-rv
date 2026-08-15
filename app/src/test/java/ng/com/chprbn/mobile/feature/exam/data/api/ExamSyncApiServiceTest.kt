package ng.com.chprbn.mobile.feature.exam.data.api

import com.google.gson.JsonParser
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncItemDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncItemDto
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
 * JSON-wire integration tests for [ExamSyncApiService]. Spins up
 * MockWebServer per test so we can assert the captured request body's
 * field names + values (catches `@SerializedName` drift), the path /
 * method (catches Retrofit annotation drift), and the response envelope
 * parsing round-trip (catches DTO-shape drift on the server contract).
 *
 * Complements the lower-level [ApiExamSyncRemoteSource] unit tests
 * (which mock this interface).
 */
class ExamSyncApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ExamSyncApiService

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = mockServerApi(server, ExamSyncApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // region uploadAttendanceBatch

    @Test
    fun `uploadAttendanceBatch posts a bare JSON array with snake_case fields, no client_id status or marked_at`() =
        runTest {
            server.enqueue(jsonOk("""{"status":true,"message":"Successful","data":[101]}"""))

            api.uploadAttendanceBatch(
                body = listOf(
                    AttendanceSyncItemDto(
                        scheduledCandidateId = 501L,
                        scheduleId = 45L,
                        candidateId = 101L,
                        paperId = 8L,
                        signIn = 1,
                        signOut = 0,
                        remark = "AE",
                        year = 2026,
                    ),
                ),
            )

            val recorded = server.takeRequest()
            assertEquals("POST", recorded.method)
            assertEquals("/attendance/push-record", recorded.path)

            // Confirmed live contract (docs/mobile-api-guide.html §5): the
            // body IS the array, not `{ "items": [...] }`.
            val body = JsonParser.parseString(recorded.body.readUtf8()).asJsonArray
            val item = body.single().asJsonObject
            assertEquals(501L, item["scheduled_candidate_id"].asLong)
            assertEquals(45L, item["schedule_id"].asLong)
            assertEquals(101L, item["candidate_id"].asLong)
            assertEquals(8L, item["paper_id"].asLong)
            assertEquals(1, item["sign_in"].asInt)
            assertEquals(0, item["sign_out"].asInt)
            assertEquals("AE", item["remark"].asString)
            assertEquals(2026, item["year"].asInt)
            // Explicitly forbidden shapes per the docs' warning note.
            assertFalse("must not send client_id", item.has("client_id"))
            assertFalse("must not send status", item.has("status"))
            assertFalse("must not send marked_at", item.has("marked_at"))
            // `body` was already parsed with .asJsonArray above — a JsonObject
            // (the old `{ "items": [...] }` wrapper) would have thrown there,
            // so reaching this line already proves the bare-array shape.
        }

    @Test
    fun `uploadAttendanceBatch parses the whole-batch status and processed candidate_ids`() = runTest {
        server.enqueue(jsonOk("""{"status":true,"message":"Successful","data":[101,102]}"""))

        val response = api.uploadAttendanceBatch(
            body = listOf(
                AttendanceSyncItemDto(501L, 45L, 101L, 8L, 1, 0, null, 2026),
                AttendanceSyncItemDto(502L, 45L, 102L, 8L, 1, 1, null, 2026),
            ),
        )

        assertTrue(response.isSuccessful)
        val envelope = response.body()!!
        assertTrue(envelope.status)
        assertEquals(listOf(101L, 102L), envelope.data)
    }

    @Test
    fun `uploadAttendanceBatch 500 surfaces as non-successful response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("boom"))

        val response = api.uploadAttendanceBatch(
            body = listOf(AttendanceSyncItemDto(501L, 45L, 101L, 8L, 1, 0, null, 2026)),
        )

        assertFalse(response.isSuccessful)
        assertEquals(500, response.code())
        assertEquals("boom", response.errorBody()?.string())
    }

    @Test
    fun `uploadAttendanceBatch tolerates missing data field`() = runTest {
        server.enqueue(jsonOk("""{"status":true,"message":"empty"}"""))

        val response = api.uploadAttendanceBatch(body = emptyList())

        assertTrue(response.isSuccessful)
        val body = response.body()!!
        assertTrue(body.status)
        assertNull(body.data)
    }

    @Test
    fun `uploadAttendanceBatch tolerates a legacy success key in place of status`() = runTest {
        server.enqueue(jsonOk("""{"success":true,"message":"ok","data":[101]}"""))

        val response = api.uploadAttendanceBatch(
            body = listOf(AttendanceSyncItemDto(501L, 45L, 101L, 8L, 1, 0, null, 2026)),
        )

        assertTrue(response.body()!!.status)
    }

    // endregion

    // region uploadRemarkBatch

    @Test
    fun `uploadRemarkBatch posts to exam-remarks-batch with snake_case body fields`() = runTest {
        server.enqueue(jsonOk("""{"success":true,"data":{"results":[]}}"""))

        api.uploadRemarkBatch(
            idempotencyKey = "test-idempotency-key",
            body = RemarkSyncBatchRequestDto(
                items = listOf(
                    RemarkSyncItemDto(
                        clientId = "r1",
                        id = "r1",
                        candidateId = "c1",
                        paperId = "p1",
                        body = "Arrived late",
                        severity = "warning",
                        createdAt = 1_700_000_000_000L,
                    ),
                ),
            ),
        )

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/attendance-remarks", recorded.path)

        val body = JsonParser.parseString(recorded.body.readUtf8()).asJsonObject
        val item = body.getAsJsonArray("items").single().asJsonObject
        assertEquals("r1", item["client_id"].asString)
        assertEquals("r1", item["id"].asString)
        assertEquals("c1", item["candidate_id"].asString)
        assertEquals("p1", item["paper_id"].asString)
        assertEquals("Arrived late", item["body"].asString)
        assertEquals("warning", item["severity"].asString)
        assertEquals(1_700_000_000_000L, item["created_at"].asLong)
    }

    @Test
    fun `uploadRemarkBatch serialises null paperId as JSON null`() = runTest {
        server.enqueue(jsonOk("""{"success":true,"data":{"results":[]}}"""))

        api.uploadRemarkBatch(
            idempotencyKey = "test-idempotency-key",
            body = RemarkSyncBatchRequestDto(
                items = listOf(
                    RemarkSyncItemDto(
                        clientId = "r1",
                        id = "r1",
                        candidateId = "c1",
                        paperId = null,
                        body = "centre-wide",
                        severity = "info",
                        createdAt = 0L,
                    ),
                ),
            ),
        )

        val recorded = server.takeRequest()
        val body = JsonParser.parseString(recorded.body.readUtf8()).asJsonObject
        val item = body.getAsJsonArray("items").single().asJsonObject
        // Gson default behaviour: omits null fields. Server tolerates absent paper_id (= null).
        assertFalse(
            "paper_id should be omitted (or null) when the remark is centre-wide",
            item.has("paper_id") && !item["paper_id"].isJsonNull,
        )
    }

    @Test
    fun `uploadRemarkBatch parses per-row results envelope`() = runTest {
        server.enqueue(
            jsonOk(
                """
                {"success":true,"data":{"results":[
                  {"client_id":"r1","accepted":true,"server_id":"srv-1"}
                ]}}
                """.trimIndent(),
            ),
        )

        val response = api.uploadRemarkBatch(
            idempotencyKey = "test-idempotency-key",
            body = RemarkSyncBatchRequestDto(
                items = listOf(
                    RemarkSyncItemDto("r1", "r1", "c1", "p1", "x", "info", 0L),
                ),
            ),
        )

        assertTrue(response.isSuccessful)
        val results = response.body()!!.data!!.results!!
        assertEquals(1, results.size)
        assertEquals("r1", results.single().clientId)
        assertTrue(results.single().accepted)
        assertEquals("srv-1", results.single().serverId)
    }

    // endregion

    private fun jsonOk(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(body)
            .also { assertNotNull(it) }
}
