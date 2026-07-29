package ng.com.chprbn.mobile.feature.exam.data.source

import ng.com.chprbn.mobile.feature.exam.data.api.ExamDossierApiService
import ng.com.chprbn.mobile.feature.exam.data.mappers.toDomain
import java.io.IOException
import javax.inject.Inject

/**
 * Retrofit-backed dossier source.
 *
 * Contract:
 *
 * - **Transport / envelope failure** (network throws, HTTP non-2xx,
 *   envelope `success:false`, unmappable center): throws
 *   [IllegalStateException] with a short message. This is what lets the
 *   [CompositeExamDossierRemoteSource] tell "server broke" apart from
 *   "server returned empty" — the composite falls back to the Fake source
 *   only on the latter (see the E1 audit note).
 * - **Successful-but-empty** (HTTP 200 with a missing / null `data.center`):
 *   returns `null` so the composite can decide whether to serve a Fake in
 *   dev builds.
 * - **Successful with a valid centre**: returns the mapped bundle.
 *
 * Unmappable rows in the papers / candidates / assignments arrays are
 * dropped silently — better to persist the partial dossier than to fail
 * the whole download over a single malformed row.
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
        val data = envelope.data ?: return null
        val center = data.center?.toDomain() ?: return null
        return ExamDossierBundle(
            center = center,
            papers = data.papers.orEmpty().mapNotNull { it.toDomain() },
            candidates = data.candidates.orEmpty().mapNotNull { it.toDomain() },
            assignments = data.assignments.orEmpty().mapNotNull { it.toDomain() },
        )
    }
}
