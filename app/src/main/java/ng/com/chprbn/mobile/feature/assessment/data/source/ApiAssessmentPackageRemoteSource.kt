package ng.com.chprbn.mobile.feature.assessment.data.source

import ng.com.chprbn.mobile.feature.assessment.data.api.AssessmentPackageApiService
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSchedule
import javax.inject.Inject

/**
 * Retrofit-backed read source. Never throws; wraps each call in
 * `runCatching` and degrades any failure (network error, HTTP non-2xx,
 * empty body, JSON parse failure) to `null`. The repository decides what
 * user-facing message to show.
 *
 * Unmappable rows in a list response are dropped silently — better to show
 * the partial list than to fail the whole download.
 */
class ApiAssessmentPackageRemoteSource @Inject constructor(
    private val api: AssessmentPackageApiService,
) : AssessmentPackageRemoteSource {

    override suspend fun fetchSchedules(): List<AssessmentSchedule>? = runCatching {
        val response = api.fetchSchedules()
        if (!response.isSuccessful) return@runCatching null
        response.body()?.data
            ?.mapNotNull { it.toDomain() }
            .orEmpty()
    }.getOrNull()

    override suspend fun fetchPackage(scheduleId: String): AssessmentPackageBundle? = runCatching {
        val response = api.fetchPackage(scheduleId)
        if (!response.isSuccessful) return@runCatching null
        val data = response.body()?.data ?: return@runCatching null
        val paper = data.paper?.toDomain() ?: return@runCatching null
        AssessmentPackageBundle(
            paper = paper,
            sections = data.sections.orEmpty().mapNotNull { it.toDomain() },
            questions = data.questions.orEmpty().mapNotNull { it.toDomain() },
            candidates = data.candidates.orEmpty().mapNotNull { it.toDomain() },
        )
    }.getOrNull()
}
