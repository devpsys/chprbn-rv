package ng.com.chprbn.mobile.feature.assessment.data.source

import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaper
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSection
import ng.com.chprbn.mobile.feature.assessment.domain.model.SectionQuestion

/**
 * Read-side abstraction for the per-schedule package download. The method
 * returns `null` on a successful-but-empty response and throws on a
 * transport/envelope error — the repository maps both into a
 * [DownloadAssessmentPackageResult].
 *
 * Schedule discovery is not part of this surface: assessment schedules
 * are the PE/PA subset of the exam dossier and come through
 * `ExamPaperRepository.getAssessmentPapers()`. See
 * `AssessmentScheduleRepositoryImpl.getSchedules` for the adapter.
 *
 * The interface exists so dev builds can swap in [FakeAssessmentPackageRemoteSource]
 * (no backend required) while production resolves [CompositeAssessmentPackageRemoteSource]
 * with the API impl as the primary.
 */
interface AssessmentPackageRemoteSource {

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
