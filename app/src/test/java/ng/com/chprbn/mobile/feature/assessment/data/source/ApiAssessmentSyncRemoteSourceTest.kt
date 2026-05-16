package ng.com.chprbn.mobile.feature.assessment.data.source

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.data.api.AssessmentSyncApiService
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncBatchEnvelopeDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncBatchResultsDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncResultDto
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ApiAssessmentSyncRemoteSourceTest {

    private val api = mockk<AssessmentSyncApiService>()
    private val source = ApiAssessmentSyncRemoteSource(api)

    @Test
    fun `batch returns per-row Result keyed by clientId`() = runTest {
        coEvery { api.uploadPracticalScoreBatch(any()) } returns
            Response.success(
                PracticalScoreSyncBatchEnvelopeDto(
                    success = true,
                    data = PracticalScoreSyncBatchResultsDto(
                        results = listOf(
                            PracticalScoreSyncResultDto(
                                clientId = "PE-2024:c1:q1",
                                accepted = true,
                                serverId = "srv-1",
                            ),
                            PracticalScoreSyncResultDto(
                                clientId = "PE-2024:c1:q2",
                                accepted = false,
                                error = "score exceeds max_score",
                            ),
                        )
                    )
                )
            )

        val results = source.uploadPracticalScoreBatch(
            listOf(score(questionId = "q1"), score(questionId = "q2")),
        )

        assertTrue(results.getValue("PE-2024:c1:q1").isSuccess)
        assertTrue(results.getValue("PE-2024:c1:q2").isFailure)
        assertTrue(
            results.getValue("PE-2024:c1:q2").exceptionOrNull()!!.message!!.contains("exceeds max_score"),
        )
    }

    @Test
    fun `batch sends a single HTTP call carrying every row's clientId`() = runTest {
        val captured = slot<PracticalScoreSyncBatchRequestDto>()
        coEvery { api.uploadPracticalScoreBatch(capture(captured)) } returns
            Response.success(PracticalScoreSyncBatchEnvelopeDto(success = true))

        source.uploadPracticalScoreBatch(
            listOf(score(questionId = "q1"), score(questionId = "q2")),
        )

        assertEquals(2, captured.captured.items.size)
        assertEquals(
            listOf("PE-2024:c1:q1", "PE-2024:c1:q2"),
            captured.captured.items.map { it.clientId },
        )
    }

    @Test
    fun `non-2xx transport failure maps every input row to Result failure`() = runTest {
        coEvery { api.uploadPracticalScoreBatch(any()) } returns
            Response.error(500, "boom".toResponseBody("text/plain".toMediaTypeOrNull()))

        val results = source.uploadPracticalScoreBatch(
            listOf(score(questionId = "q1"), score(questionId = "q2")),
        )

        assertTrue(results.getValue("PE-2024:c1:q1").isFailure)
        assertTrue(results.getValue("PE-2024:c1:q2").isFailure)
        assertTrue(
            results.getValue("PE-2024:c1:q1").exceptionOrNull()!!.message!!.contains("500"),
        )
    }

    @Test
    fun `IOException transport failure maps every input row to Result failure with the cause`() = runTest {
        coEvery { api.uploadPracticalScoreBatch(any()) } throws IOException("offline")

        val results = source.uploadPracticalScoreBatch(
            listOf(score(questionId = "q1"), score(questionId = "q2")),
        )

        assertTrue(results.getValue("PE-2024:c1:q1").exceptionOrNull() is IOException)
        assertTrue(results.getValue("PE-2024:c1:q2").exceptionOrNull() is IOException)
    }

    @Test
    fun `empty input returns empty map without HTTP call`() = runTest {
        val results = source.uploadPracticalScoreBatch(emptyList())

        assertTrue(results.isEmpty())
    }

    private fun score(questionId: String) = PracticalScore(
        scheduleId = "PE-2024",
        candidateId = "c1",
        questionId = questionId,
        score = 8,
        scoredAt = 1_700_000_000_000L,
        syncStatus = SyncStatus.Pending,
    )
}
