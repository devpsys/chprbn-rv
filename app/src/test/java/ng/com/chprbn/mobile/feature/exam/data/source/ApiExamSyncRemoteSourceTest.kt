package ng.com.chprbn.mobile.feature.exam.data.source

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.data.api.ExamSyncApiService
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncBatchEnvelopeDto
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncBatchResultsDto
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncResultDto
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ApiExamSyncRemoteSourceTest {

    private val api = mockk<ExamSyncApiService>()
    private val source = ApiExamSyncRemoteSource(api)

    @Test
    fun `batch returns per-row Result keyed by clientId`() = runTest {
        coEvery { api.uploadAttendanceBatch(any()) } returns
            Response.success(
                AttendanceSyncBatchEnvelopeDto(
                    success = true,
                    data = AttendanceSyncBatchResultsDto(
                        results = listOf(
                            AttendanceSyncResultDto(
                                clientId = "p1:c1",
                                accepted = true,
                                serverId = "srv-1",
                            ),
                            AttendanceSyncResultDto(
                                clientId = "p1:c2",
                                accepted = false,
                                error = "candidate not assigned to paper",
                            ),
                        )
                    )
                )
            )

        val results = source.uploadAttendanceBatch(
            listOf(attendance("c1"), attendance("c2")),
        )

        assertTrue(results.getValue("p1:c1").isSuccess)
        assertTrue(results.getValue("p1:c2").isFailure)
        assertTrue(
            results.getValue("p1:c2").exceptionOrNull()!!.message!!.contains("candidate not assigned"),
        )
    }

    @Test
    fun `batch sends a single HTTP call carrying every row's clientId`() = runTest {
        val captured = slot<AttendanceSyncBatchRequestDto>()
        coEvery { api.uploadAttendanceBatch(capture(captured)) } returns
            Response.success(AttendanceSyncBatchEnvelopeDto(success = true))

        source.uploadAttendanceBatch(listOf(attendance("c1"), attendance("c2")))

        assertEquals(2, captured.captured.items.size)
        assertEquals(listOf("p1:c1", "p1:c2"), captured.captured.items.map { it.clientId })
    }

    @Test
    fun `non-2xx transport failure maps every input row to Result failure`() = runTest {
        coEvery { api.uploadAttendanceBatch(any()) } returns
            Response.error(500, "boom".toResponseBody("text/plain".toMediaTypeOrNull()))

        val results = source.uploadAttendanceBatch(
            listOf(attendance("c1"), attendance("c2")),
        )

        assertTrue(results.getValue("p1:c1").isFailure)
        assertTrue(results.getValue("p1:c2").isFailure)
        // Same transport exception applied to every row.
        assertEquals(
            results.getValue("p1:c1").exceptionOrNull()!!.message,
            results.getValue("p1:c2").exceptionOrNull()!!.message,
        )
        assertTrue(results.getValue("p1:c1").exceptionOrNull()!!.message!!.contains("500"))
    }

    @Test
    fun `IOException transport failure maps every input row to Result failure with the cause`() = runTest {
        coEvery { api.uploadAttendanceBatch(any()) } throws IOException("offline")

        val results = source.uploadAttendanceBatch(
            listOf(attendance("c1"), attendance("c2")),
        )

        assertTrue(results.getValue("p1:c1").isFailure)
        assertTrue(results.getValue("p1:c1").exceptionOrNull() is IOException)
        assertTrue(results.getValue("p1:c2").exceptionOrNull() is IOException)
    }

    @Test
    fun `empty input returns empty map without HTTP call`() = runTest {
        val results = source.uploadAttendanceBatch(emptyList())

        assertTrue(results.isEmpty())
    }

    @Test
    fun `clientId missing from server response surfaces as 'no result' failure`() = runTest {
        coEvery { api.uploadAttendanceBatch(any()) } returns
            Response.success(
                AttendanceSyncBatchEnvelopeDto(
                    success = true,
                    data = AttendanceSyncBatchResultsDto(
                        // c1 is acknowledged, c2 is missing from results.
                        results = listOf(
                            AttendanceSyncResultDto(clientId = "p1:c1", accepted = true),
                        )
                    )
                )
            )

        val results = source.uploadAttendanceBatch(
            listOf(attendance("c1"), attendance("c2")),
        )

        assertTrue(results.getValue("p1:c1").isSuccess)
        assertTrue(results.getValue("p1:c2").isFailure)
        assertTrue(
            results.getValue("p1:c2").exceptionOrNull()!!.message!!.contains("no result"),
        )
    }

    private fun attendance(candidate: String) = Attendance(
        paperId = "p1",
        candidateId = candidate,
        status = AttendanceStatus.SignedIn,
        markedAt = 1_700_000_000_000L,
        syncStatus = SyncStatus.Pending,
    )
}
