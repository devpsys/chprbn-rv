package ng.com.chprbn.mobile.feature.assessment.data.source

import kotlinx.coroutines.delay
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaper
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSchedule
import ng.com.chprbn.mobile.feature.assessment.domain.model.Facility
import ng.com.chprbn.mobile.feature.assessment.domain.model.Hall
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSection
import ng.com.chprbn.mobile.feature.assessment.domain.model.SectionQuestion
import javax.inject.Inject

/**
 * In-memory golden data for offline UI development. Matches the screen
 * mockups closely enough that every Compose preview / E2E walkthrough
 * shows realistic content. Production builds resolve
 * [CompositeAssessmentPackageRemoteSource] with this as the **fallback**
 * — it's never the primary.
 *
 * The 200 ms delays mimic network latency so loading skeletons render
 * meaningfully during development.
 */
class FakeAssessmentPackageRemoteSource @Inject constructor() :
    AssessmentPackageRemoteSource {

    override suspend fun fetchSchedules(): List<AssessmentSchedule> {
        delay(SIMULATED_LATENCY_MS)
        return SCHEDULES
    }

    override suspend fun fetchPackage(scheduleId: String): AssessmentPackageBundle? {
        delay(SIMULATED_LATENCY_MS)
        return PACKAGE_BY_SCHEDULE[scheduleId]
    }

    private companion object {
        const val SIMULATED_LATENCY_MS = 200L

        val SCHEDULES = listOf(
            AssessmentSchedule(
                id = "PE-2024",
                title = "PE-2024 / Practical Exam",
                date = 1_730_000_000_000L,
                paperKind = PaperKind.Practical,
                centerId = "C-1",
                syncStatus = SyncStatus.Synced,
            ),
            AssessmentSchedule(
                id = "MD-801",
                title = "MD-801 / Medical Diagnostics",
                date = 1_731_000_000_000L,
                paperKind = PaperKind.Practical,
                centerId = "C-1",
                syncStatus = SyncStatus.Pending,
            ),
        )

        private val SECTION_A = PracticalSection(
            id = "PE-2024-sec-A",
            scheduleId = "PE-2024",
            title = "A",
            subtitle = "Patient Assessment",
            ordering = 1,
        )
        private val SECTION_B = PracticalSection(
            id = "PE-2024-sec-B",
            scheduleId = "PE-2024",
            title = "B",
            subtitle = "Clinical Diagnosis",
            ordering = 2,
        )
        private val SECTION_C = PracticalSection(
            id = "PE-2024-sec-C",
            scheduleId = "PE-2024",
            title = "C",
            subtitle = "Ethical Standards",
            ordering = 3,
        )

        private fun question(sectionId: String, number: Int, prompt: String) = SectionQuestion(
            id = "$sectionId-q$number",
            sectionId = sectionId,
            number = number,
            prompt = prompt,
            imageUrl = null,
            maxScore = 10,
        )

        private val PE_2024 = AssessmentPackageBundle(
            paper = AssessmentPaper(
                scheduleId = "PE-2024",
                title = "Regulatory Medical Paper A-14",
                statusLabel = "Active",
                facility = Facility(name = "Lagos State Centre", address = "10 Marina Rd, Lagos"),
                hall = Hall(name = "Hall B", address = "Room 12, Block 4"),
                heroImageUrl = null,
            ),
            sections = listOf(SECTION_A, SECTION_B, SECTION_C),
            questions = listOf(
                question(SECTION_A.id, 1, "Measure blood pressure accurately."),
                question(SECTION_A.id, 2, "Take pulse and respiratory rate."),
                question(SECTION_B.id, 1, "Identify likely diagnosis from symptoms."),
                question(SECTION_B.id, 2, "Order appropriate investigations."),
                question(SECTION_C.id, 1, "Apply informed consent principles."),
            ),
            candidates = listOf(
                Candidate(id = "c1", examNumber = "EX-2024-0092", fullName = "Johnathan Doe"),
                Candidate(id = "c2", examNumber = "EX-2024-0093", fullName = "Jane Smith"),
                Candidate(id = "c3", examNumber = "EX-2024-0094", fullName = "Alice Anderson"),
            ),
        )

        val PACKAGE_BY_SCHEDULE = mapOf(
            "PE-2024" to PE_2024,
        )
    }
}
