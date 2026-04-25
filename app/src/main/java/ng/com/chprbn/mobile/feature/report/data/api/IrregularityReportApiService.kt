package ng.com.chprbn.mobile.feature.report.data.api

import ng.com.chprbn.mobile.feature.report.data.dto.IrregularityReportEnvelopeDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Mobile API v1 — submit an officer irregularity report (Bearer required).
 *
 * Multipart field names follow snake_case for parity with other mobile endpoints.
 */
interface IrregularityReportApiService {

    @Multipart
    @POST("practitioners/license-irregularity-reports")
    suspend fun submitIrregularityReport(
        @Part("name_on_card") nameOnCard: RequestBody,
        @Part("license_number") licenseNumber: RequestBody,
        @Part("cadre") cadre: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("remark") remark: RequestBody,
        @Part("reported_at") reportedAt: RequestBody,
        @Part snapshot: MultipartBody.Part
    ): Response<IrregularityReportEnvelopeDto>
}
