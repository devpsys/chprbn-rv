package ng.com.chprbn.mobile.feature.assessment.data.source

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaper
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSchedule
import ng.com.chprbn.mobile.feature.assessment.domain.model.Facility
import ng.com.chprbn.mobile.feature.assessment.domain.model.Hall
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CompositeAssessmentPackageRemoteSourceTest {

    private val primary = mockk<AssessmentPackageRemoteSource>()
    private val fallback = mockk<AssessmentPackageRemoteSource>()
    private val composite = CompositeAssessmentPackageRemoteSource(primary, fallback)

    @Test
    fun `fetchSchedules returns primary result without calling fallback when primary succeeds`() = runTest {
        val expected = listOf(schedule("PE-2024"))
        coEvery { primary.fetchSchedules() } returns expected

        val result = composite.fetchSchedules()

        assertEquals(expected, result)
        coVerify(exactly = 0) { fallback.fetchSchedules() }
    }

    @Test
    fun `fetchSchedules falls back when primary returns null`() = runTest {
        val expected = listOf(schedule("MD-801"))
        coEvery { primary.fetchSchedules() } returns null
        coEvery { fallback.fetchSchedules() } returns expected

        val result = composite.fetchSchedules()

        assertEquals(expected, result)
    }

    @Test
    fun `fetchSchedules returns null when both sources fail`() = runTest {
        coEvery { primary.fetchSchedules() } returns null
        coEvery { fallback.fetchSchedules() } returns null

        assertNull(composite.fetchSchedules())
    }

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
    fun `fetchSchedules primary error propagates and fallback is not called`() = runTest {
        // A-S1 audit: a live-API failure must surface as an error, not
        // silently degrade to Fake data.
        coEvery { primary.fetchSchedules() } throws IllegalStateException("HTTP 500")

        val thrown = runCatching { composite.fetchSchedules() }.exceptionOrNull()
        assertTrue(thrown is IllegalStateException)
        coVerify(exactly = 0) { fallback.fetchSchedules() }
    }

    @Test
    fun `fetchPackage primary error propagates and fallback is not called`() = runTest {
        coEvery { primary.fetchPackage("X") } throws IllegalStateException("HTTP 500")

        val thrown = runCatching { composite.fetchPackage("X") }.exceptionOrNull()
        assertTrue(thrown is IllegalStateException)
        coVerify(exactly = 0) { fallback.fetchPackage(any()) }
    }

    private fun schedule(id: String) = AssessmentSchedule(
        id = id,
        title = id,
        date = 0L,
        paperKind = PaperKind.Practical,
        centerId = "C-1",
        syncStatus = SyncStatus.Synced,
    )

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
