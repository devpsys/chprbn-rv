package ng.com.chprbn.mobile.feature.exam.data.source

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.sync.AssessorProvider
import ng.com.chprbn.mobile.core.sync.dto.AssessorDto
import ng.com.chprbn.mobile.feature.exam.data.api.ExamSyncApiService
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendancePushRequestDto
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncResponseDto
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
    private val assessor = AssessorDto(
        id = 12L,
        name = "Jane Field Officer",
        email = "jane.field@example.com",
        phone = "08012345678",
        username = "jane.field",
        status = 1,
        department = "ACC",
        location = "Lagos",
        roles = listOf("Inspector"),
    )
    private val assessorProvider = mockk<AssessorProvider> {
        coEvery { current() } returns assessor
    }
    private val source = ApiExamSyncRemoteSource(api, assessorProvider)

    @Test
    fun `successful batch marks every well-formed row Success — server gives no per-row results`() = runTest {
        coEvery { api.uploadAttendanceBatch(any()) } returns
            Response.success(AttendanceSyncResponseDto(status = true, data = listOf(101L, 102L)))

        val results = source.uploadAttendanceBatch(listOf(row("c1"), row("c2")))

        assertTrue(results.getValue(key("c1")).isSuccess)
        assertTrue(results.getValue(key("c2")).isSuccess)
    }

    @Test
    fun `single HTTP call carries every row + the assessor block`() = runTest {
        val captured = slot<AttendancePushRequestDto>()
        coEvery { api.uploadAttendanceBatch(capture(captured)) } returns
            Response.success(AttendanceSyncResponseDto(status = true))

        source.uploadAttendanceBatch(listOf(row("c1"), row("c2")))

        assertEquals(2, captured.captured.attendances.size)
        assertEquals(listOf(101L, 102L), captured.captured.attendances.map { it.candidateId })
        assertEquals(assessor, captured.captured.assessor)
    }

    @Test
    fun `missing assessor identity fails every row without making the HTTP call`() = runTest {
        coEvery { assessorProvider.current() } returns null

        val results = source.uploadAttendanceBatch(listOf(row("c1"), row("c2")))

        assertTrue(results.getValue(key("c1")).isFailure)
        assertTrue(results.getValue(key("c2")).isFailure)
        val message = results.getValue(key("c1")).exceptionOrNull()!!.message!!
        assertTrue(
            "expected an assessor-related failure, was: $message",
            message.contains("assessor", ignoreCase = true),
        )
    }

    @Test
    fun `server status false fails every row with the server message`() = runTest {
        coEvery { api.uploadAttendanceBatch(any()) } returns
            Response.success(AttendanceSyncResponseDto(status = false, message = "duplicate submission"))

        val results = source.uploadAttendanceBatch(listOf(row("c1"), row("c2")))

        assertTrue(results.getValue(key("c1")).isFailure)
        assertTrue(results.getValue(key("c2")).isFailure)
        assertEquals(
            "duplicate submission",
            results.getValue(key("c1")).exceptionOrNull()!!.message,
        )
    }

    @Test
    fun `non-2xx transport failure maps every input row to Result failure`() = runTest {
        coEvery { api.uploadAttendanceBatch(any()) } returns
            Response.error(500, "boom".toResponseBody("text/plain".toMediaTypeOrNull()))

        val results = source.uploadAttendanceBatch(listOf(row("c1"), row("c2")))

        assertTrue(results.getValue(key("c1")).isFailure)
        assertTrue(results.getValue(key("c2")).isFailure)
        assertEquals(
            results.getValue(key("c1")).exceptionOrNull()!!.message,
            results.getValue(key("c2")).exceptionOrNull()!!.message,
        )
        assertTrue(results.getValue(key("c1")).exceptionOrNull()!!.message!!.contains("500"))
    }

    @Test
    fun `IOException transport failure maps every input row to Result failure with the cause`() = runTest {
        coEvery { api.uploadAttendanceBatch(any()) } throws IOException("offline")

        val results = source.uploadAttendanceBatch(listOf(row("c1"), row("c2")))

        assertTrue(results.getValue(key("c1")).isFailure)
        assertTrue(results.getValue(key("c1")).exceptionOrNull() is IOException)
        assertTrue(results.getValue(key("c2")).exceptionOrNull() is IOException)
    }

    @Test
    fun `empty input returns empty map without HTTP call`() = runTest {
        val results = source.uploadAttendanceBatch(emptyList())

        assertTrue(results.isEmpty())
    }

    @Test
    fun `row with non-numeric id fails individually and is excluded from the HTTP call`() = runTest {
        val captured = slot<AttendancePushRequestDto>()
        coEvery { api.uploadAttendanceBatch(capture(captured)) } returns
            Response.success(AttendanceSyncResponseDto(status = true))

        val results = source.uploadAttendanceBatch(
            listOf(row("c1"), row("c2", scheduledCandidateId = "not-a-number")),
        )

        assertTrue(results.getValue(key("c1")).isSuccess)
        assertTrue(results.getValue(key("c2")).isFailure)
        assertEquals(1, captured.captured.attendances.size)
    }

    @Test
    fun `all rows unparseable skips the HTTP call entirely`() = runTest {
        val results = source.uploadAttendanceBatch(
            listOf(row("c1", scheduledCandidateId = "nope")),
        )

        assertTrue(results.getValue(key("c1")).isFailure)
    }

    private fun row(
        candidate: String,
        paperId: String = "8",
        scheduledCandidateId: String = if (candidate == "c1") "501" else "502",
        scheduleId: String = "45",
        year: Int = 2026,
    ) = AttendanceUploadRow(
        paperId = paperId,
        candidateId = if (candidate == "c1") "101" else "102",
        scheduledCandidateId = scheduledCandidateId,
        scheduleId = scheduleId,
        year = year,
        status = AttendanceStatus.SignedIn,
        remark = "",
    )

    /** Client key mirrors [ApiExamSyncRemoteSource]'s `attendanceClientId(row.paperId, row.candidateId)`. */
    private fun key(candidate: String) = "8:${if (candidate == "c1") "101" else "102"}"
}
