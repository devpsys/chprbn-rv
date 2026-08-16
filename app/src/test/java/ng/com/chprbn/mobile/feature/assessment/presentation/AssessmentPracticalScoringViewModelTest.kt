package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSection
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionStatus as DomainSectionStatus
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionSummary
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.SectionQuestion
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.CommitPracticalSectionUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetPracticalQuestionsUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetPracticalSectionsUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.LookupAssessmentCandidateUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.RecordPracticalScoreUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AssessmentPracticalScoringViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // The route arg named `candidateId` is actually the QR-scanned exam
    // number. The VM resolves it via LookupAssessmentCandidateUseCase to
    // the domain DB id ("c1") that scores are keyed on.
    private val lookupCandidate = mockk<LookupAssessmentCandidateUseCase> {
        coEvery { this@mockk("PE-2024", "EX-001") } returns
            Candidate(id = "c1", examNumber = "EX-001", fullName = "Jane Doe")
    }
    private val getQuestions = mockk<GetPracticalQuestionsUseCase>()
    private val recordScore = mockk<RecordPracticalScoreUseCase>(relaxed = true) {
        coEvery {
            this@mockk(any(), any(), any(), any(), any())
        } returns SaveResult.Success
    }
    private val getSections = mockk<GetPracticalSectionsUseCase> {
        coEvery { this@mockk(any(), any()) } returns emptyList()
    }
    private val commitSection = mockk<CommitPracticalSectionUseCase> {
        coEvery { this@mockk(any(), any(), any()) } returns SaveResult.Success
    }

    private val savedState = SavedStateHandle(
        mapOf(
            "scheduleId" to "PE-2024",
            "candidateId" to "EX-001",
            "sectionId" to "PE-2024-sec-A",
        ),
    )

    private fun makeVm(
        handle: SavedStateHandle = savedState,
    ) = AssessmentPracticalScoringViewModel(
        handle, lookupCandidate, getQuestions, recordScore, getSections, commitSection,
    )

    @Test
    fun `init loads questions and seeds scores from existing PracticalScore rows`() = runTest {
        coEvery { getQuestions("PE-2024", "c1", "PE-2024-sec-A") } returns listOf(
            question("q1", number = 1, prompt = "Take BP") to null,
            question("q2", number = 2, prompt = "Take pulse") to existingScore(score = 7),
        )

        val state = makeVm().uiState.value
        assertEquals(2, state.questions.size)
        assertEquals(0, state.questions.first { it.id == "q1" }.score)
        assertEquals(7, state.questions.first { it.id == "q2" }.score)
    }

    @Test
    fun `init sets sectionTitle to the downloaded section name`() = runTest {
        coEvery { getQuestions(any(), any(), any()) } returns emptyList()
        coEvery { getSections("PE-2024", "c1") } returns listOf(
            sectionSummary("PE-2024-sec-A", "CHEW - Patient Assessment"),
            sectionSummary("PE-2024-sec-B", "JCHEW - Common Complaints"),
        )

        assertEquals("CHEW - Patient Assessment", makeVm().uiState.value.sectionTitle)
    }

    @Test
    fun `onIncrement clamps at maxScore and calls recordScore exactly once per delta`() = runTest {
        coEvery { getQuestions(any(), any(), any()) } returns listOf(
            question("q1", number = 1, prompt = "x", maxScore = 2) to existingScore(score = 1),
        )
        val vm = makeVm()

        vm.onIncrement("q1") // 1 → 2
        vm.onIncrement("q1") // clamped: stays at 2; no recordScore call

        assertEquals(2, vm.uiState.value.questions.single().score)
        coVerify(exactly = 1) {
            recordScore(
                scheduleId = "PE-2024",
                candidateId = "c1",
                questionId = "q1",
                score = 2,
                maxScore = 2,
            )
        }
    }

    @Test
    fun `onDecrement does not go below zero and skips persisting when value unchanged`() = runTest {
        coEvery { getQuestions(any(), any(), any()) } returns listOf(
            question("q1", number = 1, prompt = "x", maxScore = 10) to existingScore(score = 0),
        )
        val vm = makeVm()

        vm.onDecrement("q1") // already 0, no-op

        assertEquals(0, vm.uiState.value.questions.single().score)
        coVerify(exactly = 0) { recordScore(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `unresolved scan payload never persists a score`() = runTest {
        // Simulate a scan of an exam number that isn't on this schedule's
        // roster. The VM must not write scores under the raw payload.
        val unresolvedState = SavedStateHandle(
            mapOf(
                "scheduleId" to "PE-2024",
                "candidateId" to "UNKNOWN-EX",
                "sectionId" to "PE-2024-sec-A",
            ),
        )
        coEvery { lookupCandidate("PE-2024", "UNKNOWN-EX") } returns null
        coEvery { getQuestions(any(), any(), any()) } returns listOf(
            question("q1", number = 1, prompt = "x", maxScore = 5) to existingScore(score = 0),
        )
        val vm = makeVm(unresolvedState)

        vm.onIncrement("q1")

        coVerify(exactly = 0) { recordScore(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `onSaveScores commits the section then emits sectionSaved`() = runTest {
        coEvery { getQuestions(any(), any(), any()) } returns emptyList()
        val vm = makeVm()

        var emitted = false
        val job = launch { vm.sectionSaved.collect { emitted = true } }
        advanceUntilIdle()

        vm.onSaveScores()
        advanceUntilIdle()

        assertTrue(emitted)
        assertTrue(vm.uiState.value.isSaving)
        coVerify(exactly = 1) { commitSection("PE-2024", "c1", "PE-2024-sec-A") }
        job.cancel()
    }

    @Test
    fun `onSaveScores failure clears the spinner and does not emit`() = runTest {
        coEvery { getQuestions(any(), any(), any()) } returns emptyList()
        coEvery { commitSection(any(), any(), any()) } returns SaveResult.Error("disk full")
        val vm = makeVm()

        var emitted = false
        val job = launch { vm.sectionSaved.collect { emitted = true } }
        advanceUntilIdle()

        vm.onSaveScores()
        advanceUntilIdle()

        assertFalse(emitted)
        assertFalse(vm.uiState.value.isSaving)
        job.cancel()
    }

    @Test
    fun `onSaveScores is a no-op when the candidate never resolved`() = runTest {
        val unresolvedState = SavedStateHandle(
            mapOf(
                "scheduleId" to "PE-2024",
                "candidateId" to "UNKNOWN-EX",
                "sectionId" to "PE-2024-sec-A",
            ),
        )
        coEvery { lookupCandidate("PE-2024", "UNKNOWN-EX") } returns null
        val vm = makeVm(unresolvedState)

        vm.onSaveScores()

        coVerify(exactly = 0) { commitSection(any(), any(), any()) }
        assertFalse(vm.uiState.value.isSaving)
    }

    private fun question(
        id: String,
        number: Int,
        prompt: String,
        maxScore: Int = 10,
    ) = SectionQuestion(
        id = id,
        sectionId = "PE-2024-sec-A",
        number = number,
        prompt = prompt,
        imageUrl = null,
        maxScore = maxScore,
    )

    private fun existingScore(score: Int) =
        ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore(
            scheduleId = "PE-2024",
            candidateId = "c1",
            questionId = "q-stub",
            score = score,
            scoredAt = 0L,
        )

    private fun sectionSummary(id: String, title: String) = PracticalSectionSummary(
        section = PracticalSection(
            id = id,
            scheduleId = "PE-2024",
            title = title,
            subtitle = "",
            ordering = 0,
        ),
        status = DomainSectionStatus.NotStarted,
        scoredCount = 0,
        totalCount = 1,
        lastUpdatedAt = null,
    )
}
