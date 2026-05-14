package ng.com.chprbn.mobile.feature.assessment.data.mappers

import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The bridges are the project's only defence against a corrupted enum
 * column. Every persisted row must round-trip; an unknown value must
 * degrade to a documented default rather than throw `IllegalArgumentException`.
 */
class EnumBridgesTest {

    @Test
    fun `every SyncStatus round-trips`() {
        SyncStatus.entries.forEach { value ->
            assertEquals(value, value.toDbValue().toSyncStatus())
        }
    }

    @Test
    fun `unknown SyncStatus string degrades to Pending`() {
        assertEquals(SyncStatus.Pending, "".toSyncStatus())
        assertEquals(SyncStatus.Pending, "garbage".toSyncStatus())
        assertEquals(SyncStatus.Pending, "PENDING".toSyncStatus()) // case-sensitive valueOf
    }

    @Test
    fun `every PaperKind round-trips`() {
        PaperKind.entries.forEach { value ->
            assertEquals(value, value.toDbValue().toPaperKind())
        }
    }

    @Test
    fun `unknown PaperKind string degrades to Theory`() {
        assertEquals(PaperKind.Theory, "".toPaperKind())
        assertEquals(PaperKind.Theory, "garbage".toPaperKind())
    }
}
