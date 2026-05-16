package ng.com.chprbn.mobile.feature.assessment.data.api

import com.google.gson.JsonParser
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncItemDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ProjectScoreSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ProjectScoreSyncItemDto
import ng.com.chprbn.mobile.testing.mockServerApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * JSON-wire integration tests for [AssessmentSyncApiService]. Same
 * shape as `ExamSyncApiServiceTest` — pins the snake_case field names
 * on the wire and the response envelope parsing.
 */
class AssessmentSyncApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: AssessmentSyncApiService

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = mockServerApi(server, AssessmentSyncApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // region uploadPracticalScoreBatch

    @Test
    fun `uploadPracticalScoreBatch posts to practical-scores-batch with snake_case body fields`() = runTest {
        server.enqueue(jsonOk("""{"success":true,"data":{"results":[]}}"""))

        api.uploadPracticalScoreBatch(
            PracticalScoreSyncBatchRequestDto(
                items = listOf(
                    PracticalScoreSyncItemDto(
                        clientId = "PE-2024:c1:q1",
                        scheduleId = "PE-2024",
                        candidateId = "c1",
                        questionId = "q1",
                        score = 8,
                        scoredAt = 1_700_000_000_000L,
                    ),
                ),
            ),
        )

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/assessments/practical-scores/batch", recorded.path)

        val body = JsonParser.parseString(recorded.body.readUtf8()).asJsonObject
        val item = body.getAsJsonArray("items").single().asJsonObject
        assertEquals("PE-2024:c1:q1", item["client_id"].asString)
        assertEquals("PE-2024", item["schedule_id"].asString)
        assertEquals("c1", item["candidate_id"].asString)
        assertEquals("q1", item["question_id"].asString)
        assertEquals(8, item["score"].asInt)
        assertEquals(1_700_000_000_000L, item["scored_at"].asLong)
    }

    @Test
    fun `uploadPracticalScoreBatch parses per-row results envelope`() = runTest {
        server.enqueue(
            jsonOk(
                """
                {"success":true,"data":{"results":[
                  {"client_id":"PE-2024:c1:q1","accepted":true,"server_id":"psc_1"},
                  {"client_id":"PE-2024:c1:q2","accepted":false,"error":"score exceeds max_score"}
                ]}}
                """.trimIndent(),
            ),
        )

        val response = api.uploadPracticalScoreBatch(
            PracticalScoreSyncBatchRequestDto(
                items = listOf(
                    PracticalScoreSyncItemDto("PE-2024:c1:q1", "PE-2024", "c1", "q1", 8, 0L),
                    PracticalScoreSyncItemDto("PE-2024:c1:q2", "PE-2024", "c1", "q2", 9, 0L),
                ),
            ),
        )

        assertTrue(response.isSuccessful)
        val results = response.body()!!.data!!.results!!
        assertEquals(2, results.size)
        assertTrue(results[0].accepted)
        assertEquals("psc_1", results[0].serverId)
        assertFalse(results[1].accepted)
        assertEquals("score exceeds max_score", results[1].error)
    }

    @Test
    fun `uploadPracticalScoreBatch 500 surfaces as non-successful response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("boom"))

        val response = api.uploadPracticalScoreBatch(
            PracticalScoreSyncBatchRequestDto(
                items = listOf(
                    PracticalScoreSyncItemDto("PE-2024:c1:q1", "PE-2024", "c1", "q1", 8, 0L),
                ),
            ),
        )

        assertFalse(response.isSuccessful)
        assertEquals(500, response.code())
    }

    // endregion

    // region uploadProjectScoreBatch

    @Test
    fun `uploadProjectScoreBatch posts to project-scores-batch with snake_case body fields`() = runTest {
        server.enqueue(jsonOk("""{"success":true,"data":{"results":[]}}"""))

        api.uploadProjectScoreBatch(
            ProjectScoreSyncBatchRequestDto(
                items = listOf(
                    ProjectScoreSyncItemDto(
                        clientId = "PE-2024:c1",
                        scheduleId = "PE-2024",
                        candidateId = "c1",
                        score = 78.50,
                        maxScore = 100,
                        scoredAt = 1_700_000_000_000L,
                    ),
                ),
            ),
        )

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/assessments/project-scores/batch", recorded.path)

        val body = JsonParser.parseString(recorded.body.readUtf8()).asJsonObject
        val item = body.getAsJsonArray("items").single().asJsonObject
        assertEquals("PE-2024:c1", item["client_id"].asString)
        assertEquals("PE-2024", item["schedule_id"].asString)
        assertEquals("c1", item["candidate_id"].asString)
        // Decimal scores must round-trip with full precision; the doc requires this.
        assertEquals(78.50, item["score"].asDouble, 0.0)
        assertEquals(100, item["max_score"].asInt)
        assertEquals(1_700_000_000_000L, item["scored_at"].asLong)
    }

    @Test
    fun `uploadProjectScoreBatch parses per-row results envelope`() = runTest {
        server.enqueue(
            jsonOk(
                """
                {"success":true,"data":{"results":[
                  {"client_id":"PE-2024:c1","accepted":true,"server_id":"prj_1"}
                ]}}
                """.trimIndent(),
            ),
        )

        val response = api.uploadProjectScoreBatch(
            ProjectScoreSyncBatchRequestDto(
                items = listOf(
                    ProjectScoreSyncItemDto("PE-2024:c1", "PE-2024", "c1", 78.5, 100, 0L),
                ),
            ),
        )

        assertTrue(response.isSuccessful)
        val result = response.body()!!.data!!.results!!.single()
        assertEquals("PE-2024:c1", result.clientId)
        assertTrue(result.accepted)
        assertEquals("prj_1", result.serverId)
        assertNull(result.error)
    }

    // endregion

    private fun jsonOk(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(body)
}
