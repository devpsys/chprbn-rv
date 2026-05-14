package ng.com.chprbn.mobile.feature.exam.data.source

import kotlinx.coroutines.delay
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.feature.exam.domain.model.Center
import ng.com.chprbn.mobile.feature.exam.domain.model.Paper
import javax.inject.Inject

/**
 * In-memory golden data for offline UI development. Matches the screen
 * mockups closely enough that every Compose preview / E2E walkthrough
 * shows realistic content. Production builds resolve
 * [CompositeExamDossierRemoteSource] with this as the **fallback** —
 * it's never the primary.
 *
 * The 200 ms delay mimics network latency so loading skeletons render
 * meaningfully during development.
 */
class FakeExamDossierRemoteSource @Inject constructor() : ExamDossierRemoteSource {

    override suspend fun fetchDossier(): ExamDossierBundle {
        delay(SIMULATED_LATENCY_MS)
        return BUNDLE
    }

    private companion object {
        const val SIMULATED_LATENCY_MS = 200L

        private val CENTRE = Center(
            id = "C-1",
            name = "St. Jude Metropolitan Hospital",
            code = "SJMH-2024",
            location = "Regulatory District 04, North Campus",
            heroImageUrl = null,
        )

        private val PAPERS = listOf(
            Paper(
                id = "p-paper-i",
                centerId = CENTRE.id,
                title = "Paper I (P1)",
                subtitle = "General English & Literacy",
                paperKind = PaperKind.Theory,
                startAt = 1_730_000_000_000L,
                endAt = 1_730_007_200_000L,
                hall = "Main Hall A",
                totalCandidates = 142,
            ),
            Paper(
                id = "p-paper-ii",
                centerId = CENTRE.id,
                title = "Paper II (P2)",
                subtitle = "Mathematics",
                paperKind = PaperKind.Theory,
                startAt = 1_730_010_800_000L,
                endAt = 1_730_018_000_000L,
                hall = "Main Hall A",
                totalCandidates = 142,
            ),
            Paper(
                id = "p-paper-pe",
                centerId = CENTRE.id,
                title = "Paper III (PE)",
                subtitle = "Practical Examination",
                paperKind = PaperKind.Practical,
                startAt = 1_730_021_600_000L,
                endAt = 1_730_028_800_000L,
                hall = "Lab B-2",
                totalCandidates = 24,
            ),
        )

        private val CANDIDATES = listOf(
            Candidate(id = "ex-0001", examNumber = "EX-2024-0001", fullName = "Jonathan Smith"),
            Candidate(id = "ex-0002", examNumber = "EX-2024-0002", fullName = "Anita Meyer"),
            Candidate(id = "ex-0003", examNumber = "EX-2024-0003", fullName = "Michael Abiodun"),
        )

        // Every candidate is assigned to every paper today — keeps the
        // fake roster simple. A more realistic dataset would have
        // candidates split across papers; revisit when the backend ships.
        private val ASSIGNMENTS = CANDIDATES.flatMap { c ->
            PAPERS.map { p -> ExamPaperAssignment(paperId = p.id, candidateId = c.id) }
        }

        val BUNDLE = ExamDossierBundle(
            center = CENTRE,
            papers = PAPERS,
            candidates = CANDIDATES,
            assignments = ASSIGNMENTS,
        )
    }
}
