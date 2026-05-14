package ng.com.chprbn.mobile.feature.assessment.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PracticalSectionStatusTest {

    @Test
    fun `zero scored is NotStarted`() {
        assertEquals(PracticalSectionStatus.NotStarted, PracticalSectionStatus.from(0, 5))
    }

    @Test
    fun `negative scored treated as NotStarted`() {
        assertEquals(PracticalSectionStatus.NotStarted, PracticalSectionStatus.from(-3, 5))
    }

    @Test
    fun `fully scored is Complete`() {
        assertEquals(PracticalSectionStatus.Complete, PracticalSectionStatus.from(5, 5))
    }

    @Test
    fun `over-scored stays Complete`() {
        // Defensive — the SQL shouldn't produce this, but the function
        // shouldn't break if it does.
        assertEquals(PracticalSectionStatus.Complete, PracticalSectionStatus.from(7, 5))
    }

    @Test
    fun `partially scored is Incomplete`() {
        assertEquals(PracticalSectionStatus.Incomplete, PracticalSectionStatus.from(1, 5))
        assertEquals(PracticalSectionStatus.Incomplete, PracticalSectionStatus.from(4, 5))
    }
}
