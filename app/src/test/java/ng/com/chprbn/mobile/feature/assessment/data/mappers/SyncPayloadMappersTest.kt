package ng.com.chprbn.mobile.feature.assessment.data.mappers

import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncPayloadMappersTest {

    @Test
    fun `practical score maps to sync item without sync metadata`() {
        val domain = PracticalScore(
            scheduleId = "PE-2024",
            candidateId = "c1",
            questionId = "q1",
            score = 8,
            scoredAt = 1_700_000_000_000L,
            syncStatus = SyncStatus.Failed,
            syncError = "timeout",
        )

        val dto = domain.toSyncItemDto()

        assertEquals("PE-2024", dto.scheduleId)
        assertEquals("c1", dto.candidateId)
        assertEquals("q1", dto.questionId)
        assertEquals(8, dto.score)
        assertEquals(1_700_000_000_000L, dto.scoredAt)
        // No syncStatus / syncError fields on the DTO — bookkeeping is mobile-only.
    }

    @Test
    fun `practical score clientId is composite scheduleId+candidateId+questionId`() {
        val dto = PracticalScore(
            scheduleId = "PE-2024",
            candidateId = "c1",
            questionId = "q1",
            score = 0,
            scoredAt = 0L,
        ).toSyncItemDto()

        assertEquals("PE-2024:c1:q1", dto.clientId)
    }

    @Test
    fun `project score maps to sync item`() {
        val domain = ProjectScore(
            scheduleId = "PE-2024",
            candidateId = "c1",
            score = 8.75,
            maxScore = 10,
            scoredAt = 1_700_000_000_000L,
            syncStatus = SyncStatus.Pending,
        )

        val dto = domain.toSyncItemDto()

        assertEquals("PE-2024", dto.scheduleId)
        assertEquals("c1", dto.candidateId)
        assertEquals(8.75, dto.score, 0.0)
        assertEquals(10, dto.maxScore)
        assertEquals(1_700_000_000_000L, dto.scoredAt)
    }

    @Test
    fun `project score clientId is composite scheduleId+candidateId`() {
        val dto = ProjectScore(
            scheduleId = "PE-2024",
            candidateId = "c1",
            score = 0.0,
            maxScore = 10,
            scoredAt = 0L,
        ).toSyncItemDto()

        assertEquals("PE-2024:c1", dto.clientId)
    }
}
