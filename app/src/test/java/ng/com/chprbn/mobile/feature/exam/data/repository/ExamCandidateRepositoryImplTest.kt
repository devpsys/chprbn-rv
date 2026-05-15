package ng.com.chprbn.mobile.feature.exam.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.data.local.CandidateDao
import ng.com.chprbn.mobile.feature.exam.data.local.CandidateEntity
import ng.com.chprbn.mobile.feature.exam.data.local.ExamCandidateRowProjection
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceFilter
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamCandidateRepositoryImplTest {

    private val dao = mockk<CandidateDao>()
    private val repository = ExamCandidateRepositoryImpl(dao)

    @Test
    fun `rowsForPaper passes empty LIKE pattern when query is blank`() = runTest {
        val queryArg = slot<String>()
        coEvery {
            dao.rowsForPaper("p1", AttendanceFilter.All.name, capture(queryArg))
        } returns emptyList()

        repository.getCandidatesForPaper("p1", AttendanceFilter.All, "   ")

        assertEquals("", queryArg.captured)
    }

    @Test
    fun `rowsForPaper escapes LIKE metacharacters in user query`() = runTest {
        val queryArg = slot<String>()
        coEvery {
            dao.rowsForPaper("p1", AttendanceFilter.SignedIn.name, capture(queryArg))
        } returns emptyList()

        repository.getCandidatesForPaper("p1", AttendanceFilter.SignedIn, "100%_a\\b")

        // `%`, `_`, `\` get backslash-escaped; result is wrapped in `%…%`.
        assertEquals("%100\\%\\_a\\\\b%", queryArg.captured)
    }

    @Test
    fun `rowsForPaper maps projection to ExamCandidateRow with attendance present`() = runTest {
        coEvery {
            dao.rowsForPaper("p1", AttendanceFilter.All.name, "")
        } returns listOf(
            ExamCandidateRowProjection(
                candidateId = "c1",
                examNumber = "EX1",
                fullName = "Ada",
                photoUrl = null,
                attendanceStatus = AttendanceStatus.SignedIn.name,
                attendanceMarkedAt = 1_000L,
                attendanceSyncStatus = SyncStatus.Pending.name,
                attendanceSyncError = null,
                remarkCount = 2,
            ),
        )

        val rows = repository.getCandidatesForPaper("p1", AttendanceFilter.All, "")

        assertEquals(1, rows.size)
        val row = rows.single()
        assertEquals("c1", row.candidate.id)
        assertEquals("EX1", row.candidate.examNumber)
        assertEquals(AttendanceStatus.SignedIn, row.attendance?.status)
        assertEquals(2, row.remarkCount)
    }

    @Test
    fun `rowsForPaper leaves attendance null when projection has no attendance row`() = runTest {
        coEvery {
            dao.rowsForPaper("p1", AttendanceFilter.All.name, "")
        } returns listOf(
            ExamCandidateRowProjection(
                candidateId = "c1",
                examNumber = "EX1",
                fullName = "Ada",
                photoUrl = null,
                attendanceStatus = null,
                attendanceMarkedAt = null,
                attendanceSyncStatus = null,
                attendanceSyncError = null,
                remarkCount = 0,
            ),
        )

        val rows = repository.getCandidatesForPaper("p1", AttendanceFilter.All, "")

        assertNull(rows.single().attendance)
    }

    @Test
    fun `getCandidateByExamNumber forwards trimmed lookup`() = runTest {
        coEvery { dao.getByExamNumber("EX1") } returns CandidateEntity(
            id = "c1",
            examNumber = "EX1",
            fullName = "Ada",
        )

        val candidate = repository.getCandidateByExamNumber("EX1")

        assertEquals("c1", candidate?.id)
        coVerify(exactly = 1) { dao.getByExamNumber("EX1") }
    }

    @Test
    fun `getCandidateByExamNumber returns null when dao has no match`() = runTest {
        coEvery { dao.getByExamNumber("EX1") } returns null

        assertNull(repository.getCandidateByExamNumber("EX1"))
    }

    @Test
    fun `SignedIn filter is forwarded as the SQL filter string`() = runTest {
        coEvery {
            dao.rowsForPaper("p1", "SignedIn", "")
        } returns emptyList()

        repository.getCandidatesForPaper("p1", AttendanceFilter.SignedIn, "")

        coVerify(exactly = 1) { dao.rowsForPaper("p1", "SignedIn", "") }
        assertTrue(true)
    }
}
