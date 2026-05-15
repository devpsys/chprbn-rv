package ng.com.chprbn.mobile.feature.exam.data.mappers

import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.data.local.ExamCandidateRowProjection
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.Center
import ng.com.chprbn.mobile.feature.exam.domain.model.Paper
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Round-trip tests for every Entity ↔ Domain shape plus the heavyweight
 * projection → row fold. Round-trip equality is the strongest invariant
 * — if Domain → Entity → Domain produces a different value, the mapper
 * is wrong.
 */
class ExamMappersTest {

    @Test
    fun `center round-trips`() {
        val domain = Center(
            id = "C-1",
            name = "Lagos Centre",
            code = "LAG-001",
            location = "10 Marina Rd",
            heroImageUrl = "https://x/hero.jpg",
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `center round-trips with null hero url`() {
        val domain = Center(id = "C-1", name = "x", code = "y", location = "z", heroImageUrl = null)

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `paper round-trips for every PaperKind`() {
        PaperKind.entries.forEach { kind ->
            val domain = Paper(
                id = "p1",
                centerId = "C-1",
                title = "Mathematics — Paper II",
                subtitle = "General Maths",
                paperKind = kind,
                startAt = 1_700_000_000_000L,
                endAt = 1_700_003_600_000L,
                hall = "Main Hall A",
                totalCandidates = 142,
            )
            assertEquals(domain, domain.toEntity().toDomain())
        }
    }

    @Test
    fun `attendance round-trips for every status`() {
        AttendanceStatus.entries.forEach { status ->
            val domain = Attendance(
                paperId = "p1",
                candidateId = "c1",
                status = status,
                markedAt = 1_700_000_000_000L,
                syncStatus = SyncStatus.Pending,
                syncError = null,
            )
            assertEquals(domain, domain.toEntity().toDomain())
        }
    }

    @Test
    fun `remark round-trips for every severity`() {
        RemarkSeverity.entries.forEach { severity ->
            val domain = Remark(
                id = "r1",
                candidateId = "c1",
                paperId = "p1",
                body = "Arrived late, no docs",
                severity = severity,
                createdAt = 1_700_000_000_000L,
                syncStatus = SyncStatus.Failed,
                syncError = "offline",
            )
            assertEquals(domain, domain.toEntity().toDomain())
        }
    }

    @Test
    fun `candidate round-trips`() {
        val domain = Candidate(
            id = "c1",
            examNumber = "EX-2024-001",
            fullName = "Jane Mukasa Doe",
            photoUrl = "data:image/jpeg;base64,/9j/4AAQSkZJRg==",
        )

        assertEquals(domain, domain.toExamCandidateEntity().toDomain())
    }

    @Test
    fun `candidate round-trips with null photoUrl`() {
        val domain = Candidate(
            id = "c1",
            examNumber = "EX-2024-001",
            fullName = "Jane Doe",
            photoUrl = null,
        )

        val back = domain.toExamCandidateEntity().toDomain()
        assertEquals(domain, back)
        assertNull(back.photoUrl)
    }

    @Test
    fun `remark round-trips with null paperId`() {
        val domain = Remark(
            id = "r1",
            candidateId = "c1",
            paperId = null,
            body = "centre-wide remark",
            severity = RemarkSeverity.Info,
            createdAt = 0L,
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    // Projection → Domain tests

    @Test
    fun `projection with attendance fields folds into a populated row`() {
        val projection = projection(
            attendanceStatus = AttendanceStatus.SignedIn.name,
            attendanceMarkedAt = 1_700_000_000_000L,
            attendanceSyncStatus = SyncStatus.Synced.name,
        )

        val row = projection.toDomain(paperId = "p1")

        assertEquals("c1", row.candidate.id)
        assertEquals("EX-001", row.candidate.examNumber)
        assertEquals(AttendanceStatus.SignedIn, row.attendance?.status)
        assertEquals(SyncStatus.Synced, row.attendance?.syncStatus)
        assertEquals("p1", row.attendance?.paperId)
        assertEquals(1_700_000_000_000L, row.attendance?.markedAt)
        assertEquals(2, row.remarkCount)
    }

    @Test
    fun `projection with null attendance fields yields a row whose attendance is null`() {
        val projection = projection(
            attendanceStatus = null,
            attendanceMarkedAt = null,
            attendanceSyncStatus = null,
            remarkCount = 0,
        )

        val row = projection.toDomain(paperId = "p1")

        assertNull("LEFT JOIN null on attendance must propagate", row.attendance)
        assertEquals(0, row.remarkCount)
    }

    @Test
    fun `projection with unknown attendance status degrades to SignedOut`() {
        val projection = projection(
            attendanceStatus = "Garbage",
            attendanceMarkedAt = 0L,
            attendanceSyncStatus = SyncStatus.Pending.name,
        )

        val row = projection.toDomain(paperId = "p1")

        assertEquals(AttendanceStatus.SignedOut, row.attendance?.status)
    }

    @Test
    fun `projection with null markedAt defaults to zero in the attendance domain row`() {
        val projection = projection(
            attendanceStatus = AttendanceStatus.SignedIn.name,
            attendanceMarkedAt = null,
            attendanceSyncStatus = SyncStatus.Pending.name,
        )

        val row = projection.toDomain(paperId = "p1")

        // null markedAt is a data-layer oddity (status without timestamp)
        // — mapper coerces to 0L rather than nulling out the attendance.
        assertEquals(0L, row.attendance?.markedAt)
    }

    private fun projection(
        attendanceStatus: String?,
        attendanceMarkedAt: Long?,
        attendanceSyncStatus: String?,
        remarkCount: Int = 2,
    ) = ExamCandidateRowProjection(
        candidateId = "c1",
        examNumber = "EX-001",
        fullName = "Jane Doe",
        photoUrl = "https://x/1.jpg",
        attendanceStatus = attendanceStatus,
        attendanceMarkedAt = attendanceMarkedAt,
        attendanceSyncStatus = attendanceSyncStatus,
        attendanceSyncError = null,
        remarkCount = remarkCount,
    )
}
