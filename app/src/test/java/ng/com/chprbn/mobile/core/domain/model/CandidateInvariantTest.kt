package ng.com.chprbn.mobile.core.domain.model

import ng.com.chprbn.mobile.feature.assessment.data.dto.AssessmentCandidateDto
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentCandidateEntity
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toAssessmentCandidateEntity
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toDomain as toAssessmentDomain
import ng.com.chprbn.mobile.feature.exam.data.dto.CandidateDto
import ng.com.chprbn.mobile.feature.exam.data.local.CandidateEntity
import ng.com.chprbn.mobile.feature.exam.data.mappers.toExamCandidateEntity
import ng.com.chprbn.mobile.feature.exam.data.mappers.toDomain as toExamDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Action checklist L2: catches cross-feature divergence on the shared
 * [Candidate] identity. Both feature/exam and feature/assessment carry
 * their own DTO + Entity + mapper chain pointed at the same domain type;
 * the contract is that `examNumber == indexingNumber` (the
 * presentation-layer alias) and that every other field round-trips
 * byte-identical between the two sides.
 *
 * If someone renames `exam_number` → `indexing_number` on one DTO (or
 * shifts a field on one Entity), the assertions here fail before the
 * regression ships.
 */
class CandidateInvariantTest {

    private val canonicalId = "CAND-001"
    private val canonicalExamNumber = "EX-2024-001"
    private val canonicalFullName = "Jane Mukasa Doe"
    // Already-normalized data URI — both Candidate DTO mappers run
    // `.normalizeApiPhotoToDataUri()`, which is a passthrough for inputs
    // that already start with `data:image`. A raw `https://…` here would
    // (correctly) get wrapped with the `data:image/jpeg;base64,` prefix
    // per the API's Base64 contract, breaking equality on the entity
    // round-trip below.
    private val canonicalPhotoUrl = "data:image/jpeg;base64,/9j/4AAQSkZJRg=="

    @Test
    fun `DTOs from both features parse identical source into the same Candidate`() {
        val examDto = CandidateDto(
            id = canonicalId,
            examNumber = canonicalExamNumber,
            fullName = canonicalFullName,
            photoUrl = canonicalPhotoUrl,
        )
        val assessmentDto = AssessmentCandidateDto(
            id = canonicalId,
            examNumber = canonicalExamNumber,
            fullName = canonicalFullName,
            photoUrl = canonicalPhotoUrl,
        )

        val examCandidate = examDto.toExamDomain()
        val assessmentCandidate = assessmentDto.toAssessmentDomain()

        assertNotNull(examCandidate)
        assertNotNull(assessmentCandidate)
        assertEquals(examCandidate, assessmentCandidate)
        assertEquals(canonicalExamNumber, examCandidate!!.examNumber)
        assertEquals(canonicalExamNumber, assessmentCandidate!!.examNumber)
    }

    @Test
    fun `entities from both features map to the same Candidate`() {
        val examEntity = CandidateEntity(
            id = canonicalId,
            examNumber = canonicalExamNumber,
            fullName = canonicalFullName,
            photoUrl = canonicalPhotoUrl,
        )
        val assessmentEntity = AssessmentCandidateEntity(
            id = canonicalId,
            examNumber = canonicalExamNumber,
            fullName = canonicalFullName,
            photoUrl = canonicalPhotoUrl,
        )

        assertEquals(examEntity.toExamDomain(), assessmentEntity.toAssessmentDomain())
    }

    @Test
    fun `Candidate round-trips through both feature Entities identically`() {
        val candidate = Candidate(
            id = canonicalId,
            examNumber = canonicalExamNumber,
            fullName = canonicalFullName,
            photoUrl = canonicalPhotoUrl,
        )

        val backFromExam = candidate.toExamCandidateEntity().toExamDomain()
        val backFromAssessment = candidate.toAssessmentCandidateEntity().toAssessmentDomain()

        assertEquals(candidate, backFromExam)
        assertEquals(candidate, backFromAssessment)
    }

    @Test
    fun `blank id is rejected consistently by both DTO mappers`() {
        val blank = "   "
        val examDto = CandidateDto(
            id = blank,
            examNumber = canonicalExamNumber,
            fullName = canonicalFullName,
        )
        val assessmentDto = AssessmentCandidateDto(
            id = blank,
            examNumber = canonicalExamNumber,
            fullName = canonicalFullName,
        )

        assertNull(examDto.toExamDomain())
        assertNull(assessmentDto.toAssessmentDomain())
    }

    @Test
    fun `null optional fields default identically on both sides`() {
        val examDto = CandidateDto(
            id = canonicalId,
            examNumber = null,
            fullName = null,
            photoUrl = null,
        )
        val assessmentDto = AssessmentCandidateDto(
            id = canonicalId,
            examNumber = null,
            fullName = null,
            photoUrl = null,
        )

        val examCandidate = examDto.toExamDomain()
        val assessmentCandidate = assessmentDto.toAssessmentDomain()

        assertEquals(examCandidate, assessmentCandidate)
        // Both null examNumber/fullName collapse to "" per the mappers'
        // .orEmpty() contract; photoUrl stays null.
        assertEquals("", examCandidate!!.examNumber)
        assertEquals("", examCandidate.fullName)
        assertNull(examCandidate.photoUrl)
    }
}
