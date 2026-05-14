package ng.com.chprbn.mobile.feature.assessment.data.mappers

import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.data.dto.AssessmentCandidateDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.AssessmentPaperDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.AssessmentScheduleDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalSectionDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ScheduleCandidateAssignmentDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.SectionQuestionDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Covers two invariants the speculative DTO mappers must hold:
 *
 * 1. A well-formed DTO maps to the expected entity.
 * 2. A DTO missing a required identity field (e.g. `id`, `schedule_id`)
 *    returns `null` — the repository drops unmappable rows rather than
 *    persisting placeholders.
 */
class DtoMappersTest {

    @Test
    fun `schedule dto maps to entity with Synced default status`() {
        val dto = AssessmentScheduleDto(
            id = "PE-2024",
            title = "PE-2024 / Practical Exam",
            date = 1_700_000_000_000L,
            paperKind = "Practical",
            centerId = "C-1",
        )

        val entity = dto.toEntity()!!

        assertEquals("PE-2024", entity.id)
        assertEquals("PE-2024 / Practical Exam", entity.title)
        assertEquals(1_700_000_000_000L, entity.date)
        assertEquals(PaperKind.Practical.name, entity.paperKind)
        assertEquals("C-1", entity.centerId)
        assertEquals(SyncStatus.Synced.name, entity.syncStatus)
    }

    @Test
    fun `schedule dto missing id returns null`() {
        assertNull(AssessmentScheduleDto(id = null).toEntity())
        assertNull(AssessmentScheduleDto(id = "   ").toEntity())
    }

    @Test
    fun `schedule dto with unknown paper kind degrades to Theory`() {
        val entity = AssessmentScheduleDto(id = "x", paperKind = "garbage").toEntity()!!

        assertEquals(PaperKind.Theory.name, entity.paperKind)
    }

    @Test
    fun `paper dto maps to entity`() {
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

        val entity = dto.toEntity()!!

        assertEquals("PE-2024", entity.scheduleId)
        assertEquals("Paper A", entity.title)
        assertEquals("Hall B", entity.hallName)
        assertEquals("https://x/hero.jpg", entity.heroImageUrl)
    }

    @Test
    fun `paper dto missing scheduleId returns null`() {
        assertNull(AssessmentPaperDto(scheduleId = null).toEntity())
    }

    @Test
    fun `paper dto with missing optional fields defaults to empty strings`() {
        val entity = AssessmentPaperDto(scheduleId = "x").toEntity()!!

        assertEquals("", entity.title)
        assertEquals("", entity.facilityName)
        assertNull(entity.heroImageUrl)
    }

    @Test
    fun `section dto maps to entity`() {
        val dto = PracticalSectionDto(
            id = "sec-A",
            scheduleId = "PE-2024",
            title = "A",
            subtitle = "Vitals",
            ordering = 1,
        )

        val entity = dto.toEntity()!!

        assertEquals("sec-A", entity.id)
        assertEquals("PE-2024", entity.scheduleId)
        assertEquals(1, entity.ordering)
    }

    @Test
    fun `section dto missing id or scheduleId returns null`() {
        assertNull(PracticalSectionDto(id = null, scheduleId = "x").toEntity())
        assertNull(PracticalSectionDto(id = "x", scheduleId = null).toEntity())
    }

    @Test
    fun `question dto maps to entity`() {
        val dto = SectionQuestionDto(
            id = "q1",
            sectionId = "sec-A",
            number = 1,
            prompt = "Take BP",
            imageUrl = "https://x/bp.jpg",
            maxScore = 10,
        )

        val entity = dto.toEntity()!!

        assertEquals("q1", entity.id)
        assertEquals("sec-A", entity.sectionId)
        assertEquals(1, entity.number)
        assertEquals(10, entity.maxScore)
    }

    @Test
    fun `question dto null number and maxScore default to zero`() {
        val entity = SectionQuestionDto(id = "q", sectionId = "s").toEntity()!!

        assertEquals(0, entity.number)
        assertEquals(0, entity.maxScore)
    }

    @Test
    fun `candidate dto maps to entity`() {
        val dto = AssessmentCandidateDto(
            id = "c1",
            examNumber = "EX-001",
            fullName = "Jane Doe",
            photoUrl = "https://x/1.jpg",
        )

        val entity = dto.toEntity()!!

        assertEquals("c1", entity.id)
        assertEquals("EX-001", entity.examNumber)
        assertEquals("Jane Doe", entity.fullName)
        assertEquals("https://x/1.jpg", entity.photoUrl)
    }

    @Test
    fun `candidate dto missing id returns null`() {
        assertNull(AssessmentCandidateDto(id = null).toEntity())
    }

    @Test
    fun `assignment dto maps to entity`() {
        val dto = ScheduleCandidateAssignmentDto(scheduleId = "PE-2024", candidateId = "c1")

        val entity = dto.toEntity()!!

        assertEquals("PE-2024", entity.scheduleId)
        assertEquals("c1", entity.candidateId)
    }

    @Test
    fun `assignment dto missing either id returns null`() {
        assertNull(ScheduleCandidateAssignmentDto(scheduleId = null, candidateId = "c").toEntity())
        assertNull(ScheduleCandidateAssignmentDto(scheduleId = "s", candidateId = null).toEntity())
    }
}
