package ng.com.chprbn.mobile.feature.exam.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceFilter
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamCandidateRow
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamCandidateRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class GetExamCandidatesUseCaseTest {

    private val repository = mockk<ExamCandidateRepository>()
    private val useCase = GetExamCandidatesUseCase(repository)

    @Test
    fun `blank paperId returns empty list without touching repository`() = runTest {
        val result = useCase(paperId = "   ")

        assertEquals(emptyList<ExamCandidateRow>(), result)
        coVerify(exactly = 0) { repository.getCandidatesForPaper(any(), any(), any()) }
    }

    @Test
    fun `trims paperId and query before forwarding`() = runTest {
        val rows = listOf(
            ExamCandidateRow(
                candidate = Candidate(id = "c1", examNumber = "EX1", fullName = "Ada"),
                attendance = null,
                remarkCount = 0,
            ),
        )
        coEvery {
            repository.getCandidatesForPaper("p1", AttendanceFilter.SignedIn, "ada")
        } returns rows

        val result = useCase(
            paperId = "  p1 ",
            filter = AttendanceFilter.SignedIn,
            query = "  ada  ",
        )

        assertEquals(rows, result)
        coVerify(exactly = 1) {
            repository.getCandidatesForPaper("p1", AttendanceFilter.SignedIn, "ada")
        }
    }

    @Test
    fun `default filter and query are forwarded`() = runTest {
        coEvery {
            repository.getCandidatesForPaper("p1", AttendanceFilter.All, "")
        } returns emptyList()

        useCase(paperId = "p1")

        coVerify(exactly = 1) {
            repository.getCandidatesForPaper("p1", AttendanceFilter.All, "")
        }
    }
}
