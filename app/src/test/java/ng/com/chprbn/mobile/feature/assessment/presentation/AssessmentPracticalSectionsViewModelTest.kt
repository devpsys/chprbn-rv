package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSection
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionStatus as DomainSectionStatus
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionSummary
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetPracticalSectionsUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.LookupAssessmentCandidateUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AssessmentPracticalSectionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lookupCandidate = mockk<LookupAssessmentCandidateUseCase>()
    private val getSections = mockk<GetPracticalSectionsUseCase>()

    @Test
    fun `hub cards and stats include only the candidate's cadre sections`() = runTest {
        coEvery { lookupCandidate("PE-2024", "B/213/010/21") } returns Candidate(
            id = "c1",
            examNumber = "B/213/010/21",
            fullName = "Jane Doe",
        )
        every { getSections.observe("PE-2024", "c1") } returns flowOf(
            listOf(
                summary("sec-chew", "CHEW - Patient Assessment", DomainSectionStatus.Complete),
                summary("sec-jchew", "JCHEW - Common Complaints", DomainSectionStatus.NotStarted),
                summary("sec-cho", "CHO - Midwifery", DomainSectionStatus.Incomplete),
                summary("sec-chew-2", "CHEW - Clinical Diagnosis", DomainSectionStatus.NotStarted),
            ),
        )

        val vm = AssessmentPracticalSectionsViewModel(
            SavedStateHandle(
                mapOf("scheduleId" to "PE-2024", "candidateId" to "B/213/010/21"),
            ),
            lookupCandidate,
            getSections,
        )

        val state = vm.uiState.value
        assertEquals(listOf("sec-chew", "sec-chew-2"), state.sections.map { it.id })
        assertEquals(1, state.sectionsDone)
        assertEquals(2, state.sectionsTotal)
        assertEquals(1, state.sectionsRemaining)
    }

    @Test
    fun `JCHEW candidate only sees JCHEW sections`() = runTest {
        coEvery { lookupCandidate("PE-2024", "C/213/010/21") } returns Candidate(
            id = "c2",
            examNumber = "C/213/010/21",
            fullName = "Anita Meyer",
        )
        every { getSections.observe("PE-2024", "c2") } returns flowOf(
            listOf(
                summary("sec-chew", "CHEW - Patient Assessment"),
                summary("sec-jchew", "JCHEW - Common Complaints"),
            ),
        )

        val vm = AssessmentPracticalSectionsViewModel(
            SavedStateHandle(
                mapOf("scheduleId" to "PE-2024", "candidateId" to "C/213/010/21"),
            ),
            lookupCandidate,
            getSections,
        )

        assertEquals(listOf("sec-jchew"), vm.uiState.value.sections.map { it.id })
        assertEquals(1, vm.uiState.value.sectionsTotal)
    }

    @Test
    fun `unknown exam-number prefix yields an empty section list`() = runTest {
        coEvery { lookupCandidate("PE-2024", "EX-001") } returns Candidate(
            id = "c1",
            examNumber = "EX-001",
            fullName = "Jane Doe",
        )
        every { getSections.observe("PE-2024", "c1") } returns flowOf(
            listOf(summary("sec-chew", "CHEW - Patient Assessment")),
        )

        val vm = AssessmentPracticalSectionsViewModel(
            SavedStateHandle(mapOf("scheduleId" to "PE-2024", "candidateId" to "EX-001")),
            lookupCandidate,
            getSections,
        )

        assertTrue(vm.uiState.value.sections.isEmpty())
        assertEquals(0, vm.uiState.value.sectionsTotal)
    }

    @Test
    fun `candidate not on the roster never observes sections`() = runTest {
        coEvery { lookupCandidate("PE-2024", "B/000/000/00") } returns null

        val vm = AssessmentPracticalSectionsViewModel(
            SavedStateHandle(
                mapOf("scheduleId" to "PE-2024", "candidateId" to "B/000/000/00"),
            ),
            lookupCandidate,
            getSections,
        )

        assertTrue(vm.uiState.value.candidateNotFound)
        verify(exactly = 0) { getSections.observe(any(), any()) }
    }

    private fun summary(
        id: String,
        title: String,
        status: DomainSectionStatus = DomainSectionStatus.NotStarted,
    ) = PracticalSectionSummary(
        section = PracticalSection(
            id = id,
            scheduleId = "PE-2024",
            title = title,
            subtitle = "",
            ordering = 0,
        ),
        status = status,
        scoredCount = if (status == DomainSectionStatus.Complete) 2 else 0,
        totalCount = 2,
        lastUpdatedAt = if (status == DomainSectionStatus.Complete) 1_730_000_000_000L else null,
    )
}
