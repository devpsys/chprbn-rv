package ng.com.chprbn.mobile.feature.exam.data.source

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The fake source keeps UI development unblocked. Three invariants
 * worth pinning: a non-null bundle is always returned, papers reference
 * the centre, and every assignment names a real paper × real candidate.
 */
class FakeExamDossierRemoteSourceTest {

    private val source = FakeExamDossierRemoteSource()

    @Test
    fun `fetchDossier returns a non-null bundle with centre and at least one paper`() = runTest {
        val bundle = source.fetchDossier()

        assertNotNull(bundle)
        assertTrue("expected papers", bundle!!.papers.isNotEmpty())
        assertTrue("expected candidates", bundle.candidates.isNotEmpty())
    }

    @Test
    fun `every paper references the centre`() = runTest {
        val bundle = source.fetchDossier()!!

        val centreId = bundle.center.id
        bundle.papers.forEach {
            assertEquals("paper ${it.id} must belong to the centre", centreId, it.centerId)
        }
    }

    @Test
    fun `every assignment names a real paper and a real candidate`() = runTest {
        val bundle = source.fetchDossier()!!

        val paperIds = bundle.papers.map { it.id }.toSet()
        val candidateIds = bundle.candidates.map { it.id }.toSet()

        bundle.assignments.forEach { a ->
            assertTrue("assignment paper ${a.paperId} must exist", a.paperId in paperIds)
            assertTrue("assignment candidate ${a.candidateId} must exist", a.candidateId in candidateIds)
        }
    }
}
