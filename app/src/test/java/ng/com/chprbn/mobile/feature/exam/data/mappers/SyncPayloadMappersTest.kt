package ng.com.chprbn.mobile.feature.exam.data.mappers

import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncPayloadMappersTest {

    @Test
    fun `attendance maps to sync request with lowercase wire status`() {
        val pairs = mapOf(
            AttendanceStatus.SignedIn to "signed_in",
            AttendanceStatus.SignedOut to "signed_out",
            AttendanceStatus.Flagged to "flagged",
        )

        pairs.forEach { (status, wireString) ->
            val dto = Attendance(
                paperId = "p1",
                candidateId = "c1",
                status = status,
                markedAt = 1_700_000_000_000L,
                syncStatus = SyncStatus.Pending,
            ).toSyncRequestDto()

            assertEquals(wireString, dto.status)
            assertEquals("p1", dto.paperId)
            assertEquals("c1", dto.candidateId)
            assertEquals(1_700_000_000_000L, dto.markedAt)
        }
    }

    @Test
    fun `remark maps to sync request with lowercase wire severity`() {
        val pairs = mapOf(
            RemarkSeverity.Info to "info",
            RemarkSeverity.Warning to "warning",
            RemarkSeverity.Critical to "critical",
        )

        pairs.forEach { (severity, wireString) ->
            val dto = Remark(
                id = "r1",
                candidateId = "c1",
                paperId = "p1",
                body = "x",
                severity = severity,
                createdAt = 1_700_000_000_000L,
                syncStatus = SyncStatus.Pending,
            ).toSyncRequestDto()

            assertEquals(wireString, dto.severity)
        }
    }

    @Test
    fun `remark sync request preserves null paperId`() {
        val dto = Remark(
            id = "r1",
            candidateId = "c1",
            paperId = null,
            body = "centre-wide",
            severity = RemarkSeverity.Info,
            createdAt = 0L,
        ).toSyncRequestDto()

        assertEquals(null, dto.paperId)
    }
}
