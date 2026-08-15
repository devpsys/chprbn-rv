package ng.com.chprbn.mobile.feature.assessment.data.api

import ng.com.chprbn.mobile.feature.assessment.data.dto.AssessmentPackageEnvelopeDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Assessment feature — per-schedule package download.
 *
 * The dedicated `GET assessments/schedules` list endpoint has been
 * **removed**: assessment schedules are now the PE/PA subset of the exam
 * dossier (`/exam/dossier`). See `AssessmentScheduleRepositoryImpl.getSchedules`
 * for the adapter that reads them off the shared `papers` table.
 *
 * Bearer auth required on this route (same scheme as the verification
 * feature uses).
 */
interface AssessmentPackageApiService {

    @GET("assessments/schedules/{scheduleId}/package")
    suspend fun fetchPackage(
        @Path("scheduleId") scheduleId: String,
    ): Response<AssessmentPackageEnvelopeDto>
}
