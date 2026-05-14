package ng.com.chprbn.mobile.feature.assessment.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentCandidateDao
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentCandidateEntity
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentCandidateRowProjection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AssessmentCandidateRepositoryImplTest {

    private val candidateDao = mockk<AssessmentCandidateDao>()
    private val repository = AssessmentCandidateRepositoryImpl(candidateDao)

    @Test
    fun `empty query forwards empty LIKE pattern`() = runTest {
        val capturedQuery = slot<String>()
        coEvery {
            candidateDao.rowsForSchedule("PE-2024", capture(capturedQuery))
        } returns emptyList()

        repository.getCandidates("PE-2024", "")

        assertEquals(
            "empty filter must use empty string to short-circuit the SQL OR",
            "",
            capturedQuery.captured,
        )
    }

    @Test
    fun `non-empty query wraps in percent signs`() = runTest {
        val capturedQuery = slot<String>()
        coEvery {
            candidateDao.rowsForSchedule("PE-2024", capture(capturedQuery))
        } returns emptyList()

        repository.getCandidates("PE-2024", "Jane")

        assertEquals("%Jane%", capturedQuery.captured)
    }

    @Test
    fun `LIKE metacharacters are escaped`() = runTest {
        val capturedQuery = slot<String>()
        coEvery {
            candidateDao.rowsForSchedule("PE-2024", capture(capturedQuery))
        } returns emptyList()

        // A user search for "100%" must not match every candidate.
        repository.getCandidates("PE-2024", "100%")
        assertEquals("%100\\%%", capturedQuery.captured)

        repository.getCandidates("PE-2024", "A_B")
        assertEquals("%A\\_B%", capturedQuery.captured)

        repository.getCandidates("PE-2024", "back\\slash")
        assertEquals("%back\\\\slash%", capturedQuery.captured)
    }

    @Test
    fun `getCandidate returns null when DAO has nothing`() = runTest {
        coEvery { candidateDao.getForSchedule("PE-2024", "c1") } returns null

        assertNull(repository.getCandidate("PE-2024", "c1"))
    }

    @Test
    fun `getCandidate maps DAO row to domain Candidate`() = runTest {
        coEvery { candidateDao.getForSchedule("PE-2024", "c1") } returns AssessmentCandidateEntity(
            id = "c1",
            examNumber = "EX-001",
            fullName = "Jane Doe",
            photoUrl = "https://x.png",
        )

        val candidate = repository.getCandidate("PE-2024", "c1")

        assertEquals("c1", candidate?.id)
        assertEquals("EX-001", candidate?.examNumber)
        assertEquals("Jane Doe", candidate?.fullName)
    }

    @Test
    fun `getCandidates maps projection rows to domain`() = runTest {
        coEvery {
            candidateDao.rowsForSchedule("PE-2024", any())
        } returns listOf(
            AssessmentCandidateRowProjection(
                candidateId = "c1",
                examNumber = "EX-1",
                fullName = "Jane",
                photoUrl = null,
                aggregateScore = 75,
                scoredQuestions = 5,
                totalQuestions = 8,
                syncStatus = SyncStatus.Pending.name,
            ),
        )

        val rows = repository.getCandidates("PE-2024", "Jane")

        assertEquals(1, rows.size)
        assertEquals("c1", rows.single().candidate.id)
        assertEquals(75, rows.single().aggregateScore)
        assertEquals(SyncStatus.Pending, rows.single().syncStatus)

        // Sanity: the DAO is hit (covered indirectly above) — guard against
        // an accidental no-op path.
        coVerify { candidateDao.rowsForSchedule("PE-2024", any()) }
    }
}
