package ng.com.chprbn.mobile.feature.verification.data.api

import ng.com.chprbn.mobile.feature.verification.data.dto.LicenseRecordEnvelopeDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Mobile API v1 — practitioner license card (Bearer required).
 */
interface ScanApiService {

    @GET("practitioners/license")
    suspend fun getLicenseRecord(
        @Query("license_number") licenseNumber: String
    ): Response<LicenseRecordEnvelopeDto>
}
