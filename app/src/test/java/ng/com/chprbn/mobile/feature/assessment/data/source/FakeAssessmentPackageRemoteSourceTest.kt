package ng.com.chprbn.mobile.feature.assessment.data.source

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The fake source exists to keep UI development unblocked. Its package
 * data must be internally consistent (sections referenced by questions
 * all exist; candidates have non-blank identity). The former
 * `fetchSchedules` test was dropped when schedule discovery moved to
 * `ExamPaperRepository.getAssessmentPapers()`.
 */
class FakeAssessmentPackageRemoteSourceTest {

    private val source = FakeAssessmentPackageRemoteSource()

    @Test
    fun `fetchPackage returns a bundle for PE-2024 with consistent references`() = runTest {
        val bundle = source.fetchPackage("PE-2024")

        assertNotNull(bundle)
        bundle!!
        assertEquals("PE-2024", bundle.paper.scheduleId)
        assertTrue("expected sections", bundle.sections.isNotEmpty())
        assertTrue("expected questions", bundle.questions.isNotEmpty())
        assertTrue("expected candidates", bundle.candidates.isNotEmpty())

        val sectionIds = bundle.sections.map { it.id }.toSet()
        val orphanQuestions = bundle.questions.filterNot { it.sectionId in sectionIds }
        assertTrue("questions must reference an existing section: $orphanQuestions", orphanQuestions.isEmpty())

        val blankCandidates = bundle.candidates.filter { it.id.isBlank() || it.examNumber.isBlank() }
        assertTrue("candidates must have non-blank identity: $blankCandidates", blankCandidates.isEmpty())
    }

    @Test
    fun `fetchPackage returns null for unknown schedule ids`() = runTest {
        assertNull(source.fetchPackage("does-not-exist"))
    }
}
