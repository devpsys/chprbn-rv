package ng.com.chprbn.mobile.feature.exam.data.mappers

import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity
import org.junit.Assert.assertEquals
import org.junit.Test

class EnumBridgesTest {

    @Test
    fun `every SyncStatus round-trips`() {
        SyncStatus.entries.forEach {
            assertEquals(it, it.toDbValue().toSyncStatus())
        }
    }

    @Test
    fun `unknown SyncStatus string degrades to Pending`() {
        assertEquals(SyncStatus.Pending, "garbage".toSyncStatus())
        assertEquals(SyncStatus.Pending, "".toSyncStatus())
    }

    @Test
    fun `every PaperKind round-trips`() {
        PaperKind.entries.forEach {
            assertEquals(it, it.toDbValue().toPaperKind())
        }
    }

    @Test
    fun `unknown PaperKind string degrades to Theory`() {
        assertEquals(PaperKind.Theory, "garbage".toPaperKind())
    }

    @Test
    fun `every AttendanceStatus round-trips`() {
        AttendanceStatus.entries.forEach {
            assertEquals(it, it.toDbValue().toAttendanceStatus())
        }
    }

    @Test
    fun `unknown AttendanceStatus string degrades to SignedOut`() {
        // SignedOut is the documented safe fallback — a corrupted row
        // shouldn't claim someone is signed-in when we can't tell.
        assertEquals(AttendanceStatus.SignedOut, "garbage".toAttendanceStatus())
    }

    @Test
    fun `every RemarkSeverity round-trips`() {
        RemarkSeverity.entries.forEach {
            assertEquals(it, it.toDbValue().toRemarkSeverity())
        }
    }

    @Test
    fun `unknown RemarkSeverity string degrades to Info`() {
        assertEquals(RemarkSeverity.Info, "garbage".toRemarkSeverity())
    }
}
