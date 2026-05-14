package ng.com.chprbn.mobile.feature.assessment.data.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SyncEntityKeysTest {

    @Test
    fun `practical score key round-trips`() {
        val encoded = PracticalScoreKey.encode("PE-2024", "c1", "PE-2024-sec-A-q1")
        assertEquals("PE-2024/c1/PE-2024-sec-A-q1", encoded)
        assertEquals(Triple("PE-2024", "c1", "PE-2024-sec-A-q1"), PracticalScoreKey.decode(encoded))
    }

    @Test
    fun `practical score decode returns null for malformed keys`() {
        assertNull(PracticalScoreKey.decode("only-two/parts"))
        assertNull(PracticalScoreKey.decode("four/parts/here/x"))
        assertNull(PracticalScoreKey.decode("no-separators"))
    }

    @Test
    fun `project score key round-trips`() {
        val encoded = ProjectScoreKey.encode("PE-2024", "c1")
        assertEquals("PE-2024/c1", encoded)
        assertEquals("PE-2024" to "c1", ProjectScoreKey.decode(encoded))
    }

    @Test
    fun `project score decode returns null for malformed keys`() {
        assertNull(ProjectScoreKey.decode("only-one"))
        assertNull(ProjectScoreKey.decode("three/parts/here"))
    }
}
