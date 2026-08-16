package ng.com.chprbn.mobile.feature.assessment.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentCandidateDao
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentCandidateEntity
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentCandidateRowProjection
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreEntity
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamCandidateRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AssessmentCandidateRepositoryImplTest {

    private val candidateDao = mockk<AssessmentCandidateDao>()
    private val examCandidateRepository = mockk<ExamCandidateRepository>(relaxed = true).also {
        // Default: exam side has nothing — fallback path yields empty.
        // Individual tests override with coEvery when they exercise it.
        coEvery { it.getAssignedCandidates(any()) } returns emptyList()
        coEvery { it.getCandidateById(any()) } returns null
    }
    private val projectScoreDao = mockk<ProjectScoreDao> {
        coEvery { getForSchedule(any()) } returns emptyList()
    }
    private val repository = AssessmentCandidateRepositoryImpl(
        candidateDao, examCandidateRepository, projectScoreDao,
    )

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

    @Test
    fun `falls back to exam repo when assessment table has no rows`() = runTest {
        coEvery { candidateDao.rowsForSchedule("PE-2024", any()) } returns emptyList()
        coEvery { examCandidateRepository.getAssignedCandidates("PE-2024") } returns listOf(
            Candidate(id = "c1", examNumber = "EX-1", fullName = "Jane Doe"),
            Candidate(id = "c2", examNumber = "EX-2", fullName = "John Smith"),
        )

        val rows = repository.getCandidates("PE-2024", "")

        assertEquals(2, rows.size)
        assertEquals("c1", rows[0].candidate.id)
        // Shell rows start at 0 until score tables are overlaid.
        assertEquals(0, rows[0].aggregateScore)
        assertEquals(0, rows[0].totalQuestions)
        assertEquals(SyncStatus.Synced, rows[0].syncStatus)
    }

    @Test
    fun `stamps aggregateScore from the project table on exam fallback`() = runTest {
        coEvery { candidateDao.rowsForSchedule("PE-2024", any()) } returns emptyList()
        coEvery { examCandidateRepository.getAssignedCandidates("PE-2024") } returns listOf(
            Candidate(id = "c1", examNumber = "EX-1", fullName = "Jane Doe"),
            Candidate(id = "c2", examNumber = "EX-2", fullName = "John Smith"),
        )
        coEvery { projectScoreDao.getForSchedule("PE-2024") } returns listOf(
            ProjectScoreEntity(
                scheduleId = "PE-2024",
                candidateId = "c1",
                score = 6.4,
                maxScore = 20,
                scoredAt = 0L,
                syncStatus = SyncStatus.Pending.name,
            ),
        )

        val rows = repository.getCandidates("PE-2024", "")

        assertEquals(6, rows.first { it.candidate.id == "c1" }.aggregateScore)
        assertEquals(0, rows.first { it.candidate.id == "c2" }.aggregateScore)
    }

    @Test
    fun `exam fallback applies query filter in-memory`() = runTest {
        coEvery { candidateDao.rowsForSchedule("PE-2024", any()) } returns emptyList()
        coEvery { examCandidateRepository.getAssignedCandidates("PE-2024") } returns listOf(
            Candidate(id = "c1", examNumber = "EX-1", fullName = "Jane Doe"),
            Candidate(id = "c2", examNumber = "EX-2", fullName = "John Smith"),
        )

        val rows = repository.getCandidates("PE-2024", "jane")

        assertEquals(1, rows.size)
        assertEquals("c1", rows.single().candidate.id)
    }

    @Test
    fun `observeAssignedCount prefers assessment over exam when non-zero`() = runTest {
        coEvery { candidateDao.observeAssignedCandidateCount("PE-2024") } returns flowOf(3)
        coEvery { examCandidateRepository.observeAssignedCandidateCount("PE-2024") } returns flowOf(5)

        assertEquals(3, repository.observeAssignedCount("PE-2024").first())
    }

    @Test
    fun `observeAssignedCount falls back to exam when assessment is zero`() = runTest {
        coEvery { candidateDao.observeAssignedCandidateCount("PE-2024") } returns flowOf(0)
        coEvery { examCandidateRepository.observeAssignedCandidateCount("PE-2024") } returns flowOf(7)

        assertEquals(7, repository.observeAssignedCount("PE-2024").first())
    }

    @Test
    fun `getCandidate falls back to exam repo when assessment lookup is null`() = runTest {
        coEvery { candidateDao.getForSchedule("PE-2024", "c1") } returns null
        coEvery { examCandidateRepository.getCandidateById("c1") } returns
            Candidate(id = "c1", examNumber = "EX-1", fullName = "Jane Doe")

        val candidate = repository.getCandidate("PE-2024", "c1")

        assertTrue(candidate != null)
        assertEquals("Jane Doe", candidate?.fullName)
    }
}
