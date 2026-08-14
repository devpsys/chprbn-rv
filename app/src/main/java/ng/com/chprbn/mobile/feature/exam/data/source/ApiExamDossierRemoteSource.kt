package ng.com.chprbn.mobile.feature.exam.data.source

import android.util.Log
import ng.com.chprbn.mobile.feature.exam.data.api.ExamDossierApiService
import ng.com.chprbn.mobile.feature.exam.data.mappers.toDomain
import java.io.IOException
import javax.inject.Inject

/**
 * Retrofit-backed dossier source. Bound directly (no Fake fallback) by
 * `ExamDataModule.provideExamDossierRemoteSource` in every build type.
 *
 * Contract:
 *
 * - **Transport / envelope failure** (network throws, HTTP non-2xx,
 *   envelope `success:false`, unmappable center): throws
 *   [IllegalStateException] with a short message, surfaced to the officer
 *   as a real error rather than silently synthetic data.
 * - **Successful-but-empty** (HTTP 200 with a missing / null `data.center`):
 *   returns `null`, logged via [Log.w] since this used to be able to
 *   trigger a silent [CompositeExamDossierRemoteSource] fallback to
 *   [FakeExamDossierRemoteSource] before that path was disabled.
 * - **Successful with a valid centre**: returns the mapped bundle.
 *
 * Candidates and paper↔candidate assignments live nested per
 * [ng.com.chprbn.mobile.feature.exam.data.dto.ScheduleDto] on the wire,
 * not as flat top-level arrays — this flattens every schedule's
 * `candidates`/`paper_candidates` into the bundle's flat lists, deduping
 * candidates by id (the same person can appear in more than one
 * schedule) and deriving each paper's [Paper.totalCandidates] from the
 * assignment list, since the wire never provides that count directly.
 *
 * Unmappable rows are dropped — logged via [Log.w] with before/after
 * counts (better to persist the partial dossier than to fail the whole
 * download over a single malformed row, but silently is no longer
 * acceptable — this is exactly the class of bug that produced a "3
 * papers and 3 candidates" success message for a 150-candidate
 * download).
 *
 * `data.sections` collapses to `Center.hasSections` (non-emptiness only —
 * element shape is unconfirmed, always empty in every response seen so
 * far) so the dashboard can gate the Practical Assessment card on it.
 */
class ApiExamDossierRemoteSource @Inject constructor(
    private val api: ExamDossierApiService,
) : ExamDossierRemoteSource {

    override suspend fun fetchDossier(): ExamDossierBundle? {
        val response = try {
            api.fetchDossier()
        } catch (e: IOException) {
            error("Network error fetching exam dossier: ${e.message ?: e.javaClass.simpleName}")
        }
        if (!response.isSuccessful) {
            error("Exam dossier request failed: HTTP ${response.code()} ${response.message()}")
        }
        val envelope = response.body()
            ?: error("Exam dossier response had an empty body.")
        if (!envelope.success) {
            error(envelope.message ?: "Exam dossier request was rejected.")
        }
        val data = envelope.data
        if (data == null) {
            Log.w(TAG, "Dossier envelope had success=true but a null data block — returning null.")
            return null
        }
        val center = data.center?.toDomain()?.copy(hasSections = !data.sections.isNullOrEmpty())
        if (center == null) {
            Log.w(
                TAG,
                "Dossier response had no usable center (raw payload carried " +
                    "${data.papers?.size ?: 0} paper(s), ${data.schedules?.size ?: 0} " +
                    "schedule(s)) — returning null instead of persisting them.",
            )
            return null
        }

        val schedules = data.schedules.orEmpty()

        val rawAssignmentCount = schedules.sumOf { it.paperCandidates?.size ?: 0 }
        val assignments = schedules.flatMap { schedule ->
            schedule.paperCandidates.orEmpty().mapNotNull { it.toDomain() }
        }
        logDroppedRows("assignments", rawAssignmentCount, assignments.size)
        val assignmentCountByPaperId = assignments.groupingBy { it.paperId }.eachCount()

        val rawPapers = data.papers.orEmpty()
        val papers = rawPapers.mapNotNull {
            it.toDomain(centerId = center.id)
        }.map { paper ->
            paper.copy(totalCandidates = assignmentCountByPaperId[paper.id] ?: 0)
        }
        logDroppedRows("papers", rawPapers.size, papers.size)

        val rawCandidates = schedules.flatMap { it.candidates.orEmpty() }
        val mappedCandidates = rawCandidates.mapNotNull { it.toDomain() }
        logDroppedRows("candidates", rawCandidates.size, mappedCandidates.size)
        val candidates = mappedCandidates.distinctBy { it.id }
        if (candidates.size < mappedCandidates.size) {
            Log.w(
                TAG,
                "Collapsed ${mappedCandidates.size - candidates.size} duplicate candidate " +
                    "row(s) appearing in more than one schedule.",
            )
        }

        return ExamDossierBundle(
            center = center,
            papers = papers,
            candidates = candidates,
            assignments = assignments,
        )
    }

    private fun logDroppedRows(label: String, rawCount: Int, mappedCount: Int) {
        if (mappedCount < rawCount) {
            Log.w(
                TAG,
                "Dropped ${rawCount - mappedCount} of $rawCount $label row(s) from the " +
                    "dossier response — each was missing a required id field on the wire.",
            )
        }
    }

    private companion object {
        const val TAG = "ExamDossier"
    }
}
