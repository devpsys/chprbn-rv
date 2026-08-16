package ng.com.chprbn.mobile.feature.assessment.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticalSectionCadreTest {

    @Test
    fun `leading letter maps onto cadre prefix`() {
        assertEquals("CHO", PracticalSectionCadre.prefixForExamNumber("A/213/010/21"))
        assertEquals("CHEW", PracticalSectionCadre.prefixForExamNumber("B/213/010/21"))
        assertEquals("JCHEW", PracticalSectionCadre.prefixForExamNumber("C/213/010/21"))
        assertEquals("BCHS", PracticalSectionCadre.prefixForExamNumber("D/213/010/21"))
    }

    @Test
    fun `letter is case-insensitive and leading whitespace is ignored`() {
        assertEquals("CHEW", PracticalSectionCadre.prefixForExamNumber("  b/213/010/21"))
        assertEquals("CHO", PracticalSectionCadre.prefixForExamNumber("a/100/001/20"))
    }

    @Test
    fun `unknown or blank exam number has no cadre`() {
        assertNull(PracticalSectionCadre.prefixForExamNumber(""))
        assertNull(PracticalSectionCadre.prefixForExamNumber("   "))
        assertNull(PracticalSectionCadre.prefixForExamNumber("EX-2024-0001"))
        assertNull(PracticalSectionCadre.prefixForExamNumber("1/213/010/21"))
    }

    @Test
    fun `CHEW candidate matches CHEW sections only`() {
        val examNumber = "B/213/010/21"
        assertTrue(PracticalSectionCadre.matches("CHEW - Patient Assessment", examNumber))
        assertTrue(PracticalSectionCadre.matches("chew - counselling", examNumber))
        assertTrue(PracticalSectionCadre.matches("CHEW", examNumber))
        assertFalse(PracticalSectionCadre.matches("JCHEW - Common Complaints", examNumber))
        assertFalse(PracticalSectionCadre.matches("CHO - Midwifery", examNumber))
        assertFalse(PracticalSectionCadre.matches("BCHS - Community Health", examNumber))
    }

    @Test
    fun `JCHEW candidate matches JCHEW sections only`() {
        val examNumber = "C/213/010/21"
        assertTrue(PracticalSectionCadre.matches("JCHEW - Common Complaints", examNumber))
        assertFalse(PracticalSectionCadre.matches("CHEW - Patient Assessment", examNumber))
    }

    @Test
    fun `CHO and BCHS candidates match their own prefixes`() {
        assertTrue(PracticalSectionCadre.matches("CHO - Clinical Skills", "A/100/001/20"))
        assertFalse(PracticalSectionCadre.matches("CHEW - Clinical Skills", "A/100/001/20"))
        assertTrue(PracticalSectionCadre.matches("BCHS - Field Work", "D/090/012/19"))
        assertFalse(PracticalSectionCadre.matches("CHO - Field Work", "D/090/012/19"))
    }

    @Test
    fun `filter keeps only the candidate's cadre`() {
        val chew = summary("sec-chew", "CHEW - Patient Assessment")
        val jchew = summary("sec-jchew", "JCHEW - Common Complaints")
        val cho = summary("sec-cho", "CHO - Midwifery")
        val bchs = summary("sec-bchs", "BCHS - Community Health")
        val all = listOf(chew, jchew, cho, bchs)

        assertEquals(listOf(chew), PracticalSectionCadre.filter(all, "B/213/010/21"))
        assertEquals(listOf(jchew), PracticalSectionCadre.filter(all, "C/213/010/21"))
        assertEquals(listOf(cho), PracticalSectionCadre.filter(all, "A/100/001/20"))
        assertEquals(listOf(bchs), PracticalSectionCadre.filter(all, "D/090/012/19"))
        assertEquals(emptyList<PracticalSectionSummary>(), PracticalSectionCadre.filter(all, "EX-001"))
    }

    private fun summary(id: String, title: String) = PracticalSectionSummary(
        section = PracticalSection(
            id = id,
            scheduleId = "PE-2024",
            title = title,
            subtitle = "",
            ordering = 0,
        ),
        status = PracticalSectionStatus.NotStarted,
        scoredCount = 0,
        totalCount = 2,
        lastUpdatedAt = null,
    )
}
