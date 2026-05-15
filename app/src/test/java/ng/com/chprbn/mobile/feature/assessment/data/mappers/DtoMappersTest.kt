package ng.com.chprbn.mobile.feature.assessment.data.mappers

import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.data.dto.AssessmentCandidateDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.AssessmentPaperDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.AssessmentScheduleDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalSectionDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.SectionQuestionDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Covers two invariants the speculative DTO mappers must hold:
 *
 * 1. A well-formed DTO maps to the expected domain object.
 * 2. A DTO missing a required identity field (e.g. `id`, `schedule_id`)
 *    returns `null` — callers drop unmappable rows rather than passing
 *    placeholders down to the repository.
 */
class DtoMappersTest {

    @Test
    fun `schedule dto maps to domain with Synced default status`() {
        val dto = AssessmentScheduleDto(
            id = "PE-2024",
            title = "PE-2024 / Practical Exam",
            date = 1_700_000_000_000L,
            paperKind = "Practical",
            centerId = "C-1",
        )

        val domain = dto.toDomain()!!

        assertEquals("PE-2024", domain.id)
        assertEquals("PE-2024 / Practical Exam", domain.title)
        assertEquals(1_700_000_000_000L, domain.date)
        assertEquals(PaperKind.Practical, domain.paperKind)
        assertEquals("C-1", domain.centerId)
        assertEquals(SyncStatus.Synced, domain.syncStatus)
    }

    @Test
    fun `schedule dto missing id returns null`() {
        assertNull(AssessmentScheduleDto(id = null).toDomain())
        assertNull(AssessmentScheduleDto(id = "   ").toDomain())
    }

    @Test
    fun `schedule dto with unknown paper kind degrades to Theory`() {
        val domain = AssessmentScheduleDto(id = "x", paperKind = "garbage").toDomain()!!

        assertEquals(PaperKind.Theory, domain.paperKind)
    }

    @Test
    fun `paper dto maps to domain`() {
        val dto = AssessmentPaperDto(
            scheduleId = "PE-2024",
            title = "Paper A",
            statusLabel = "Active",
            facilityName = "Lagos",
            facilityAddress = "10 Marina",
            hallName = "Hall B",
            hallAddress = "Room 12",
            heroImageUrl = "https://x/hero.jpg",
        )

        val domain = dto.toDomain()!!

        assertEquals("PE-2024", domain.scheduleId)
        assertEquals("Paper A", domain.title)
        assertEquals("Lagos", domain.facility.name)
        assertEquals("Hall B", domain.hall.name)
        assertEquals("https://x/hero.jpg", domain.heroImageUrl)
    }

    @Test
    fun `paper dto missing scheduleId returns null`() {
        assertNull(AssessmentPaperDto(scheduleId = null).toDomain())
    }

    @Test
    fun `paper dto with missing optional fields defaults to empty strings`() {
        val domain = AssessmentPaperDto(scheduleId = "x").toDomain()!!

        assertEquals("", domain.title)
        assertEquals("", domain.facility.name)
        assertEquals("", domain.hall.address)
        assertNull(domain.heroImageUrl)
    }

    @Test
    fun `section dto maps to domain`() {
        val dto = PracticalSectionDto(
            id = "sec-A",
            scheduleId = "PE-2024",
            title = "A",
            subtitle = "Vitals",
            ordering = 1,
        )

        val domain = dto.toDomain()!!

        assertEquals("sec-A", domain.id)
        assertEquals("PE-2024", domain.scheduleId)
        assertEquals(1, domain.ordering)
    }

    @Test
    fun `section dto missing id or scheduleId returns null`() {
        assertNull(PracticalSectionDto(id = null, scheduleId = "x").toDomain())
        assertNull(PracticalSectionDto(id = "x", scheduleId = null).toDomain())
    }

    @Test
    fun `question dto maps to domain`() {
        val dto = SectionQuestionDto(
            id = "q1",
            sectionId = "sec-A",
            number = 1,
            prompt = "Take BP",
            imageUrl = "https://x/bp.jpg",
            maxScore = 10,
        )

        val domain = dto.toDomain()!!

        assertEquals("q1", domain.id)
        assertEquals("sec-A", domain.sectionId)
        assertEquals(1, domain.number)
        assertEquals(10, domain.maxScore)
    }

    @Test
    fun `question dto null number and maxScore default to zero`() {
        val domain = SectionQuestionDto(id = "q", sectionId = "s").toDomain()!!

        assertEquals(0, domain.number)
        assertEquals(0, domain.maxScore)
    }

    @Test
    fun `candidate dto maps to domain`() {
        val dto = AssessmentCandidateDto(
            id = "c1",
            examNumber = "EX-001",
            fullName = "Jane Doe",
            photoUrl = "/9j/4AAQSkZJRg==",
        )

        val domain = dto.toDomain()!!

        assertEquals("c1", domain.id)
        assertEquals("EX-001", domain.examNumber)
        assertEquals("Jane Doe", domain.fullName)
    }

    @Test
    fun `candidate dto missing id returns null`() {
        assertNull(AssessmentCandidateDto(id = null).toDomain())
    }

    @Test
    fun `candidate dto Base64 photo is wrapped as data URI`() {
        val domain = AssessmentCandidateDto(
            id = "c1",
            examNumber = "EX-001",
            fullName = "Jane Doe",
            photoUrl = "/9j/4AAQSkZJRg==",
        ).toDomain()!!

        assertEquals("data:image/jpeg;base64,/9j/4AAQSkZJRg==", domain.photoUrl)
    }

    @Test
    fun `candidate dto existing data URI photo passes through unchanged`() {
        val dataUri = "data:image/png;base64,iVBORw0KGgo="
        val domain = AssessmentCandidateDto(
            id = "c1",
            photoUrl = dataUri,
        ).toDomain()!!

        assertEquals(dataUri, domain.photoUrl)
    }

    @Test
    fun `candidate dto null photo stays null`() {
        val domain = AssessmentCandidateDto(id = "c1", photoUrl = null).toDomain()!!

        assertNull(domain.photoUrl)
    }
}
