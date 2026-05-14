package ng.com.chprbn.mobile.feature.assessment.data.source

import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaper
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSchedule
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSection
import ng.com.chprbn.mobile.feature.assessment.domain.model.SectionQuestion

/**
 * Read-side abstraction for the schedule list and the per-schedule package
 * download. Both methods return `null` on any error — the repository owns
 * mapping `null` to a specific [DownloadAssessmentPackageResult.Error].
 *
 * The interface exists so dev builds can swap in [FakeAssessmentPackageRemoteSource]
 * (no backend required) while production resolves [CompositeAssessmentPackageRemoteSource]
 * with the API impl as the primary.
 */
interface AssessmentPackageRemoteSource {

    suspend fun fetchSchedules(): List<AssessmentSchedule>?

    suspend fun fetchPackage(scheduleId: String): AssessmentPackageBundle?
}

/**
 * One-shot snapshot of everything a schedule needs to operate offline.
 * The bundle implies a single schedule (carried by `paper.scheduleId`);
 * every candidate is assumed assigned to it.
 *
 * Lives in the data layer because it's transport-shape (multi-aggregate
 * fan-out from one HTTP response). Use cases never see it — they call
 * `repository.downloadPackage(...)` which returns a counter-style result.
 */
data class AssessmentPackageBundle(
    val paper: AssessmentPaper,
    val sections: List<PracticalSection>,
    val questions: List<SectionQuestion>,
    val candidates: List<Candidate>,
)
