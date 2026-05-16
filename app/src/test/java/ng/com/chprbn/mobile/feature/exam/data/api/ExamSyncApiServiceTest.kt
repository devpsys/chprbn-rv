package ng.com.chprbn.mobile.feature.exam.data.api

import com.google.gson.JsonParser
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncBatchRequestDto
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
    fun `uploadAttendanceBatch posts to exam-attendance-batch with snake_case body fields`() = runTest {
        server.enqueue(jsonOk("""{"success":true,"data":{"results":[]}}"""))

        api.uploadAttendanceBatch(
            AttendanceSyncBatchRequestDto(
                items = listOf(
                    AttendanceSyncItemDto(
                        clientId = "p1:c1",
                        paperId = "p1",
                        candidateId = "c1",
                        status = "signed_in",
                        markedAt = 1_700_000_000_000L,
                    ),
                ),
            ),
        )

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/exam/attendance/batch", recorded.path)
        assertEquals("application/json; charset=UTF-8", recorded.getHeader("Content-Type"))

        val body = JsonParser.parseString(recorded.body.readUtf8()).asJsonObject
        val item = body.getAsJsonArray("items").single().asJsonObject
        assertEquals("p1:c1", item["client_id"].asString)
        assertEquals("p1", item["paper_id"].asString)
        assertEquals("c1", item["candidate_id"].asString)
        assertEquals("signed_in", item["status"].asString)
        assertEquals(1_700_000_000_000L, item["marked_at"].asLong)
    }

    @Test
    fun `uploadAttendanceBatch parses per-row results envelope`() = runTest {
        server.enqueue(
            jsonOk(
                """
                {
                  "success": true,
                  "message": "Batch processed.",
                  "data": {
                    "results": [
                      {"client_id": "p1:c1", "accepted": true,  "server_id": "att_1"},
                      {"client_id": "p1:c2", "accepted": false, "error": "candidate not assigned to paper"}
                    ]
                  }
                }
                """.trimIndent(),
            ),
        )

        val response = api.uploadAttendanceBatch(
            AttendanceSyncBatchRequestDto(
                items = listOf(
                    AttendanceSyncItemDto("p1:c1", "p1", "c1", "signed_in", 0L),
                    AttendanceSyncItemDto("p1:c2", "p1", "c2", "signed_in", 0L),
                ),
            ),
        )

        assertTrue(response.isSuccessful)
        val results = response.body()!!.data!!.results!!
        assertEquals(2, results.size)
        assertEquals("p1:c1", results[0].clientId)
        assertTrue(results[0].accepted)
        assertEquals("att_1", results[0].serverId)
        assertNull(results[0].error)
        assertEquals("p1:c2", results[1].clientId)
        assertFalse(results[1].accepted)
        assertEquals("candidate not assigned to paper", results[1].error)
    }

    @Test
    fun `uploadAttendanceBatch 500 surfaces as non-successful response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("boom"))

        val response = api.uploadAttendanceBatch(
            AttendanceSyncBatchRequestDto(
                items = listOf(AttendanceSyncItemDto("p1:c1", "p1", "c1", "signed_in", 0L)),
            ),
        )

        assertFalse(response.isSuccessful)
        assertEquals(500, response.code())
        assertEquals("boom", response.errorBody()?.string())
    }

    @Test
    fun `uploadAttendanceBatch tolerates missing data field`() = runTest {
        server.enqueue(jsonOk("""{"success":true,"message":"empty"}"""))

        val response = api.uploadAttendanceBatch(
            AttendanceSyncBatchRequestDto(items = emptyList()),
        )

        assertTrue(response.isSuccessful)
        val body = response.body()!!
        assertTrue(body.success)
        assertNull(body.data)
    }

    // endregion

    // region uploadRemarkBatch

    @Test
    fun `uploadRemarkBatch posts to exam-remarks-batch with snake_case body fields`() = runTest {
        server.enqueue(jsonOk("""{"success":true,"data":{"results":[]}}"""))

        api.uploadRemarkBatch(
            RemarkSyncBatchRequestDto(
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
        assertEquals("/exam/remarks/batch", recorded.path)

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
            RemarkSyncBatchRequestDto(
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
            RemarkSyncBatchRequestDto(
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
