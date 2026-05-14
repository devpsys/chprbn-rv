package ng.com.chprbn.mobile.feature.assessment.data.api

import ng.com.chprbn.mobile.feature.assessment.data.dto.AssessmentPackageEnvelopeDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.AssessmentSchedulesEnvelopeDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * **SPECULATIVE** — paths assumed pending backend contract confirmation
 * (plan §12, C1). Bearer auth required on both routes (same scheme as
 * the verification feature uses).
 */
interface AssessmentPackageApiService {

    @GET("assessments/schedules")
    suspend fun fetchSchedules(): Response<AssessmentSchedulesEnvelopeDto>

    @GET("assessments/schedules/{scheduleId}/package")
    suspend fun fetchPackage(
        @Path("scheduleId") scheduleId: String,
    ): Response<AssessmentPackageEnvelopeDto>
}
