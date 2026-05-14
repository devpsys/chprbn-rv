package ng.com.chprbn.mobile.feature.exam.data.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SyncEntityKeysTest {

    @Test
    fun `attendance key round-trips`() {
        val encoded = AttendanceKey.encode("p-paper-i", "c1")
        assertEquals("p-paper-i/c1", encoded)
        assertEquals("p-paper-i" to "c1", AttendanceKey.decode(encoded))
    }

    @Test
    fun `attendance decode returns null for malformed keys`() {
        assertNull(AttendanceKey.decode("only-one-part"))
        assertNull(AttendanceKey.decode("three/parts/here"))
    }

    @Test
    fun `remark key round-trips`() {
        val uuid = "9c8d2f3a-7b1e-4d6f-8a5c-1234567890ab"
        assertEquals(uuid, RemarkKey.encode(uuid))
        assertEquals(uuid, RemarkKey.decode(uuid))
    }

    @Test
    fun `remark decode returns null for blank keys`() {
        assertNull(RemarkKey.decode(""))
        assertNull(RemarkKey.decode("   "))
    }
}
