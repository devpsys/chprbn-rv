package ng.com.chprbn.mobile.feature.assessment.data.source

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaper
import ng.com.chprbn.mobile.feature.assessment.domain.model.Facility
import ng.com.chprbn.mobile.feature.assessment.domain.model.Hall
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Package-fetch composite: dedicated schedule-fetch tests were dropped
 * when `AssessmentPackageRemoteSource.fetchSchedules` was removed —
 * schedules are now the PE/PA subset of the exam dossier and read
 * through `ExamPaperRepository.getAssessmentPapers()` (see
 * `AssessmentScheduleRepositoryImpl.getSchedules`).
 */
class CompositeAssessmentPackageRemoteSourceTest {

    private val primary = mockk<AssessmentPackageRemoteSource>()
    private val fallback = mockk<AssessmentPackageRemoteSource>()
    private val composite = CompositeAssessmentPackageRemoteSource(primary, fallback)

    @Test
    fun `fetchPackage prefers primary and forwards scheduleId`() = runTest {
        val bundle = bundleFor("PE-2024")
        coEvery { primary.fetchPackage("PE-2024") } returns bundle

        val result = composite.fetchPackage("PE-2024")

        assertSame(bundle, result)
        coVerify(exactly = 0) { fallback.fetchPackage(any()) }
    }

    @Test
    fun `fetchPackage falls back when primary returns null`() = runTest {
        val bundle = bundleFor("PE-2024")
        coEvery { primary.fetchPackage("PE-2024") } returns null
        coEvery { fallback.fetchPackage("PE-2024") } returns bundle

        assertSame(bundle, composite.fetchPackage("PE-2024"))
    }

    @Test
    fun `fetchPackage primary error propagates and fallback is not called`() = runTest {
        // A-S1 audit: a live-API failure must surface as an error, not
        // silently degrade to Fake data.
        coEvery { primary.fetchPackage("X") } throws IllegalStateException("HTTP 500")

        val thrown = runCatching { composite.fetchPackage("X") }.exceptionOrNull()
        assertTrue(thrown is IllegalStateException)
        coVerify(exactly = 0) { fallback.fetchPackage(any()) }
    }

    private fun bundleFor(scheduleId: String) = AssessmentPackageBundle(
        paper = AssessmentPaper(
            scheduleId = scheduleId,
            title = "Paper",
            statusLabel = "Active",
            facility = Facility("F", "Addr"),
            hall = Hall("H", "Addr"),
            heroImageUrl = null,
        ),
        sections = emptyList(),
        questions = emptyList(),
        candidates = emptyList(),
    )
}
