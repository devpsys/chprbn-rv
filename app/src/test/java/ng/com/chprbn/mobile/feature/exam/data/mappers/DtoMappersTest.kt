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
    fun `paper dto maps to domain using code and name, with centerId and count supplied by the caller`() {
        val dto = PaperDto(id = "p1", code = "P1", name = "PAPER 1")

        val paper = dto.toDomain(centerId = "C-1", totalCandidates = 142)

        assertEquals("p1", paper?.id)
        assertEquals("C-1", paper?.centerId)
        assertEquals("PAPER 1", paper?.title)
        assertEquals("P1", paper?.subtitle)
        assertEquals(142, paper?.totalCandidates)
    }

    @Test
    fun `paper dto missing id returns null`() {
        assertNull(PaperDto(id = null).toDomain(centerId = "C-1"))
    }

    @Test
    fun `paper dto has no wire signal for kind, timing, or hall, so those default`() {
        val paper = PaperDto(id = "p1").toDomain(centerId = "C-1")!!

        assertEquals(PaperKind.Theory, paper.paperKind)
        assertEquals(0L, paper.startAt)
        assertEquals(0L, paper.endAt)
        assertEquals("", paper.hall)
    }

    @Test
    fun `paper dto totalCandidates defaults to zero when the caller doesn't supply a count`() {
        val paper = PaperDto(id = "p1").toDomain(centerId = "C-1")!!

        assertEquals(0, paper.totalCandidates)
    }

    @Test
    fun `candidate dto maps to domain`() {
        val dto = CandidateDto(
            id = "c1",
            examNumber = "EX-001",
            fullName = "Jane Doe",
            photoUrl = "/9j/4AAQSkZJRg==",
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
    fun `candidate dto Base64 photo is wrapped as data URI`() {
        val candidate = CandidateDto(
            id = "c1",
            examNumber = "EX-001",
            fullName = "Jane Doe",
            photoUrl = "/9j/4AAQSkZJRg==",
        ).toDomain()!!

        assertEquals("data:image/jpeg;base64,/9j/4AAQSkZJRg==", candidate.photoUrl)
    }

    @Test
    fun `candidate dto existing data URI photo passes through unchanged`() {
        val dataUri = "data:image/png;base64,iVBORw0KGgo="
        val candidate = CandidateDto(
            id = "c1",
            photoUrl = dataUri,
        ).toDomain()!!

        assertEquals(dataUri, candidate.photoUrl)
    }

    @Test
    fun `candidate dto null photo stays null`() {
        val candidate = CandidateDto(id = "c1", photoUrl = null).toDomain()!!

        assertNull(candidate.photoUrl)
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
