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
    fun `successful practical-score batch returns Result success`() = runTest {
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
                            )
                        )
                    )
                )
            )

        val result = source.uploadPracticalScore(score())

        assertTrue("expected success but was $result", result.isSuccess)
    }

    @Test
    fun `single-item batch wraps domain row into items of size 1`() = runTest {
        val captured = slot<PracticalScoreSyncBatchRequestDto>()
        coEvery { api.uploadPracticalScoreBatch(capture(captured)) } returns
            Response.success(PracticalScoreSyncBatchEnvelopeDto(success = true))

        source.uploadPracticalScore(score())

        assertEquals(1, captured.captured.items.size)
        assertEquals("PE-2024:c1:q1", captured.captured.items.first().clientId)
    }

    @Test
    fun `per-row reject returns Result failure with error message`() = runTest {
        coEvery { api.uploadPracticalScoreBatch(any()) } returns
            Response.success(
                PracticalScoreSyncBatchEnvelopeDto(
                    success = true,
                    data = PracticalScoreSyncBatchResultsDto(
                        results = listOf(
                            PracticalScoreSyncResultDto(
                                clientId = "PE-2024:c1:q1",
                                accepted = false,
                                error = "score exceeds max_score",
                            )
                        )
                    )
                )
            )

        val result = source.uploadPracticalScore(score())

        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message.orEmpty()
        assertTrue("expected per-row error in message, was: $msg", msg.contains("exceeds max_score"))
    }

    @Test
    fun `non-2xx response returns Result failure with status detail`() = runTest {
        coEvery { api.uploadPracticalScoreBatch(any()) } returns
            Response.error(500, "boom".toResponseBody("text/plain".toMediaTypeOrNull()))

        val result = source.uploadPracticalScore(score())

        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message.orEmpty()
        assertTrue("expected HTTP 500 in error message, was: $msg", msg.contains("500"))
    }

    @Test
    fun `IOException from Retrofit returns Result failure`() = runTest {
        coEvery { api.uploadPracticalScoreBatch(any()) } throws IOException("offline")

        val result = source.uploadPracticalScore(score())

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    private fun score() = PracticalScore(
        scheduleId = "PE-2024",
        candidateId = "c1",
        questionId = "q1",
        score = 8,
        scoredAt = 1_700_000_000_000L,
        syncStatus = SyncStatus.Pending,
    )
}
