package ng.com.chprbn.mobile.feature.assessment.data.source

import ng.com.chprbn.mobile.feature.assessment.data.api.AssessmentPackageApiService
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSchedule
import java.io.IOException
import javax.inject.Inject

/**
 * Retrofit-backed read source.
 *
 * Contract (same shape as `ApiExamDossierRemoteSource` — E1 audit finding):
 *
 * - **Transport / envelope failure** (network throws, HTTP non-2xx,
 *   envelope `success:false`, unmappable centre): throws
 *   [IllegalStateException]. Lets [CompositeAssessmentPackageRemoteSource]
 *   tell "server broke" apart from "server returned empty."
 * - **Successful-but-empty** (HTTP 200 with a missing `data` / `data.paper`):
 *   returns `null` for the composite to consider.
 * - **Successful with data**: returns the mapped list / bundle.
 *
 * Unmappable rows in list responses are dropped silently — better to show
 * the partial list than to fail the whole download.
 */
class ApiAssessmentPackageRemoteSource @Inject constructor(
    private val api: AssessmentPackageApiService,
) : AssessmentPackageRemoteSource {

    override suspend fun fetchSchedules(): List<AssessmentSchedule>? {
        val response = try {
            api.fetchSchedules()
        } catch (e: IOException) {
            error("Network error fetching assessment schedules: ${e.message ?: e.javaClass.simpleName}")
        }
        if (!response.isSuccessful) {
            error("Assessment schedules request failed: HTTP ${response.code()} ${response.message()}")
        }
        val envelope = response.body()
            ?: error("Assessment schedules response had an empty body.")
        if (!envelope.success) {
            error(envelope.message ?: "Assessment schedules request was rejected.")
        }
        val rows = envelope.data ?: return null
        return rows.mapNotNull { it.toDomain() }
    }

    override suspend fun fetchPackage(scheduleId: String): AssessmentPackageBundle? {
        val response = try {
            api.fetchPackage(scheduleId)
        } catch (e: IOException) {
            error("Network error fetching assessment package: ${e.message ?: e.javaClass.simpleName}")
        }
        if (!response.isSuccessful) {
            error("Assessment package request failed: HTTP ${response.code()} ${response.message()}")
        }
        val envelope = response.body()
            ?: error("Assessment package response had an empty body.")
        if (!envelope.success) {
            error(envelope.message ?: "Assessment package request was rejected.")
        }
        val data = envelope.data ?: return null
        val paper = data.paper?.toDomain() ?: return null
        return AssessmentPackageBundle(
            paper = paper,
            sections = data.sections.orEmpty().mapNotNull { it.toDomain() },
            questions = data.questions.orEmpty().mapNotNull { it.toDomain() },
            candidates = data.candidates.orEmpty().mapNotNull { it.toDomain() },
        )
    }
}
