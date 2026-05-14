package ng.com.chprbn.mobile.feature.exam.data.source

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.exam.domain.model.Center
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class CompositeExamDossierRemoteSourceTest {

    private val primary = mockk<ExamDossierRemoteSource>()
    private val fallback = mockk<ExamDossierRemoteSource>()
    private val composite = CompositeExamDossierRemoteSource(primary, fallback)

    @Test
    fun `returns primary result without calling fallback when primary succeeds`() = runTest {
        val expected = bundle()
        coEvery { primary.fetchDossier() } returns expected

        val result = composite.fetchDossier()

        assertSame(expected, result)
        coVerify(exactly = 0) { fallback.fetchDossier() }
    }

    @Test
    fun `falls back when primary returns null`() = runTest {
        val expected = bundle()
        coEvery { primary.fetchDossier() } returns null
        coEvery { fallback.fetchDossier() } returns expected

        assertSame(expected, composite.fetchDossier())
    }

    @Test
    fun `returns null when both sources fail`() = runTest {
        coEvery { primary.fetchDossier() } returns null
        coEvery { fallback.fetchDossier() } returns null

        assertNull(composite.fetchDossier())
    }

    private fun bundle() = ExamDossierBundle(
        center = Center(id = "C-1", name = "X", code = "Y", location = "Z", heroImageUrl = null),
        papers = emptyList(),
        candidates = emptyList(),
        assignments = emptyList(),
    )
}
