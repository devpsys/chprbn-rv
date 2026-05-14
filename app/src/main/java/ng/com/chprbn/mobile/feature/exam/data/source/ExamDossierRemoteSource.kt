package ng.com.chprbn.mobile.feature.exam.data.source

import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.feature.exam.domain.model.Center
import ng.com.chprbn.mobile.feature.exam.domain.model.Paper

/**
 * Read-side abstraction for the day's exam dossier. Returns `null` on
 * any error — the repository owns mapping `null` to a specific
 * [ng.com.chprbn.mobile.feature.exam.domain.model.DownloadDossierResult.Error].
 *
 * The interface exists so dev builds can swap in
 * [FakeExamDossierRemoteSource] (no backend required) while production
 * resolves [CompositeExamDossierRemoteSource] with the API impl as the
 * primary.
 */
interface ExamDossierRemoteSource {

    suspend fun fetchDossier(): ExamDossierBundle?
}

/**
 * One-shot snapshot of everything the day needs to operate offline:
 * the centre, all papers scheduled there, the candidate roster, and the
 * paper × candidate assignments.
 *
 * Lives in the data layer because it's transport-shape (multi-aggregate
 * fan-out from one HTTP response). Use cases never see it — they call
 * `repository.downloadDossier()` which returns a counter-style result.
 */
data class ExamDossierBundle(
    val center: Center,
    val papers: List<Paper>,
    val candidates: List<Candidate>,
    val assignments: List<ExamPaperAssignment>,
)
