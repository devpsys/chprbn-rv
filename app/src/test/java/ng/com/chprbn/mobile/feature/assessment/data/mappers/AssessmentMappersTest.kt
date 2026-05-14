package ng.com.chprbn.mobile.feature.assessment.data.mappers

import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentCandidateRowProjection
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaper
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSchedule
import ng.com.chprbn.mobile.feature.assessment.domain.model.Facility
import ng.com.chprbn.mobile.feature.assessment.domain.model.Hall
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSection
import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.ScoreLevel
import ng.com.chprbn.mobile.feature.assessment.domain.model.SectionQuestion
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Round-trip tests for every Entity ↔ Domain mapper plus the
 * projection-to-row fold. Round-trip equality is the strongest invariant
 * available — if Domain → Entity → Domain produces a different value, the
 * mapper is wrong somewhere.
 */
class AssessmentMappersTest {

    @Test
    fun `schedule round-trips`() {
        val domain = AssessmentSchedule(
            id = "PE-2024",
            title = "PE-2024 / Practical Exam",
            date = 1_700_000_000_000L,
            paperKind = PaperKind.Practical,
            centerId = "C-1",
            syncStatus = SyncStatus.Pending,
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `paper round-trips`() {
        val domain = AssessmentPaper(
            scheduleId = "PE-2024",
            title = "Regulatory Medical Paper A-14",
            statusLabel = "Active",
            facility = Facility(name = "Lagos Centre", address = "10 Marina Rd"),
            hall = Hall(name = "Hall B", address = "Room 12"),
            heroImageUrl = "https://example/hero.jpg",
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `paper round-trips with null heroImageUrl`() {
        val domain = AssessmentPaper(
            scheduleId = "PE-2024",
            title = "x",
            statusLabel = "y",
            facility = Facility("a", "b"),
            hall = Hall("c", "d"),
            heroImageUrl = null,
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `section round-trips`() {
        val domain = PracticalSection(
            id = "sec-A",
            scheduleId = "PE-2024",
            title = "A",
            subtitle = "Patient Assessment",
            ordering = 1,
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `question round-trips`() {
        val domain = SectionQuestion(
            id = "q1",
            sectionId = "sec-A",
            number = 1,
            prompt = "Measure blood pressure accurately.",
            imageUrl = null,
            maxScore = 10,
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `practical score round-trips`() {
        val domain = PracticalScore(
            scheduleId = "PE-2024",
            candidateId = "c1",
            questionId = "q1",
            score = 8,
            scoredAt = 1_700_000_000_000L,
            syncStatus = SyncStatus.Failed,
            syncError = "timeout",
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `project score round-trips`() {
        val domain = ProjectScore(
            scheduleId = "PE-2024",
            candidateId = "c1",
            score = 8.75,
            maxScore = 10,
            scoredAt = 1_700_000_000_000L,
            syncStatus = SyncStatus.Pending,
            syncError = null,
        )

        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `candidate row projection maps to domain row with default Normal threshold`() {
        val projection = AssessmentCandidateRowProjection(
            candidateId = "c1",
            examNumber = "EX-001",
            fullName = "Jane Doe",
            photoUrl = "https://x/1.jpg",
            aggregateScore = 75,
            scoredQuestions = 6,
            totalQuestions = 8,
            syncStatus = SyncStatus.Pending.name,
        )

        val row = projection.toDomain()

        assertEquals("c1", row.candidate.id)
        assertEquals("EX-001", row.candidate.examNumber)
        assertEquals("Jane Doe", row.candidate.fullName)
        assertEquals(75, row.aggregateScore)
        assertEquals(ScoreLevel.Normal, row.level)
        assertEquals(6, row.scoredQuestions)
        assertEquals(8, row.totalQuestions)
        assertEquals(SyncStatus.Pending, row.syncStatus)
    }

    @Test
    fun `score below 50 yields Low level under default threshold`() {
        val projection = projectionWithScore(49)

        assertEquals(ScoreLevel.Low, projection.toDomain().level)
    }

    @Test
    fun `score equal to threshold yields Normal`() {
        val projection = projectionWithScore(50)

        assertEquals(ScoreLevel.Normal, projection.toDomain().level)
    }

    @Test
    fun `custom threshold overrides default`() {
        val projection = projectionWithScore(70)

        assertEquals(ScoreLevel.Normal, projection.toDomain(threshold = 60).level)
        assertEquals(ScoreLevel.Low, projection.toDomain(threshold = 80).level)
    }

    @Test
    fun `unknown sync status in projection degrades to Pending`() {
        val projection = projectionWithScore(50).copy(syncStatus = "Garbage")

        assertEquals(SyncStatus.Pending, projection.toDomain().syncStatus)
    }

    private fun projectionWithScore(score: Int) = AssessmentCandidateRowProjection(
        candidateId = "c1",
        examNumber = "EX-1",
        fullName = "x",
        photoUrl = null,
        aggregateScore = score,
        scoredQuestions = 1,
        totalQuestions = 2,
        syncStatus = SyncStatus.Synced.name,
    )
}
