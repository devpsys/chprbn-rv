package ng.com.chprbn.mobile.feature.exam.data.mappers

import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.feature.exam.data.dto.CandidateDto
import ng.com.chprbn.mobile.feature.exam.data.dto.CenterDto
import ng.com.chprbn.mobile.feature.exam.data.dto.PaperCandidateAssignmentDto
import ng.com.chprbn.mobile.feature.exam.data.dto.PaperDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DtoMappersTest {

    @Test
    fun `center dto maps to domain`() {
        val dto = CenterDto(
            id = "C-1",
            name = "Lagos Centre",
            code = "LAG-001",
            location = "10 Marina Rd",
            heroImageUrl = "https://x/hero.jpg",
        )

        val center = dto.toDomain()!!

        assertEquals("C-1", center.id)
        assertEquals("Lagos Centre", center.name)
        assertEquals("https://x/hero.jpg", center.heroImageUrl)
    }

    @Test
    fun `center dto missing id returns null`() {
        assertNull(CenterDto(id = null).toDomain())
        assertNull(CenterDto(id = "   ").toDomain())
    }

    @Test
    fun `center dto missing optional strings defaults to empty`() {
        val center = CenterDto(id = "C-1").toDomain()!!

        assertEquals("", center.name)
        assertEquals("", center.code)
        assertEquals("", center.location)
        assertNull(center.heroImageUrl)
    }

    @Test
    fun `paper dto maps to domain`() {
        val dto = PaperDto(
            id = "p1",
            centerId = "C-1",
            title = "Mathematics — Paper II",
            subtitle = "General",
            paperKind = "Practical",
            startAt = 1_700_000_000_000L,
            endAt = 1_700_003_600_000L,
            hall = "Main Hall A",
            totalCandidates = 142,
        )

        val paper = dto.toDomain()!!

        assertEquals("p1", paper.id)
        assertEquals(PaperKind.Practical, paper.paperKind)
        assertEquals(1_700_000_000_000L, paper.startAt)
        assertEquals(142, paper.totalCandidates)
    }

    @Test
    fun `paper dto missing id returns null`() {
        assertNull(PaperDto(id = null).toDomain())
    }

    @Test
    fun `paper dto with unknown paper kind degrades to Theory`() {
        val paper = PaperDto(id = "p1", paperKind = "garbage").toDomain()!!

        assertEquals(PaperKind.Theory, paper.paperKind)
    }

    @Test
    fun `paper dto with null counts defaults to zero`() {
        val paper = PaperDto(id = "p1").toDomain()!!

        assertEquals(0L, paper.startAt)
        assertEquals(0L, paper.endAt)
        assertEquals(0, paper.totalCandidates)
    }

    @Test
    fun `candidate dto maps to domain`() {
        val dto = CandidateDto(
            id = "c1",
            examNumber = "EX-001",
            fullName = "Jane Doe",
            photoUrl = "https://x/1.jpg",
        )

        val candidate = dto.toDomain()!!

        assertEquals("c1", candidate.id)
        assertEquals("EX-001", candidate.examNumber)
        assertEquals("Jane Doe", candidate.fullName)
    }

    @Test
    fun `candidate dto missing id returns null`() {
        assertNull(CandidateDto(id = null).toDomain())
    }

    @Test
    fun `assignment dto maps to domain`() {
        val dto = PaperCandidateAssignmentDto(paperId = "p1", candidateId = "c1")

        val assignment = dto.toDomain()!!

        assertEquals("p1", assignment.paperId)
        assertEquals("c1", assignment.candidateId)
    }

    @Test
    fun `assignment dto missing either id returns null`() {
        assertNull(PaperCandidateAssignmentDto(paperId = null, candidateId = "c1").toDomain())
        assertNull(PaperCandidateAssignmentDto(paperId = "p1", candidateId = null).toDomain())
    }
}
