package ng.com.chprbn.mobile.feature.exam.data.source

import ng.com.chprbn.mobile.feature.exam.data.api.ExamDossierApiService
import ng.com.chprbn.mobile.feature.exam.data.mappers.toDomain
import javax.inject.Inject

/**
 * Retrofit-backed dossier source. Never throws; wraps each call in
 * `runCatching` and degrades any failure (network error, HTTP non-2xx,
 * empty body, JSON parse failure) to `null`. The repository decides
 * what user-facing message to show.
 *
 * Unmappable rows in list responses are dropped silently — better to
 * persist the partial dossier than to fail the whole download.
 */
class ApiExamDossierRemoteSource @Inject constructor(
    private val api: ExamDossierApiService,
) : ExamDossierRemoteSource {

    override suspend fun fetchDossier(): ExamDossierBundle? = runCatching {
        val response = api.fetchDossier()
        if (!response.isSuccessful) return@runCatching null
        val data = response.body()?.data ?: return@runCatching null
        val center = data.center?.toDomain() ?: return@runCatching null
        ExamDossierBundle(
            center = center,
            papers = data.papers.orEmpty().mapNotNull { it.toDomain() },
            candidates = data.candidates.orEmpty().mapNotNull { it.toDomain() },
            assignments = data.assignments.orEmpty().mapNotNull { it.toDomain() },
        )
    }.getOrNull()
}
