package ng.com.chprbn.mobile.feature.scan.data.api

import ng.com.chprbn.mobile.feature.scan.data.dto.LicenseRecordResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API for license record lookup. Uses same base URL as auth (chprbn.gov.ng/api/v1/).
 */
interface ScanApiService {

    @GET("practitioners/license")
    suspend fun getLicenseRecord(
        @Query("license_number") licenseNumber: String
    ): Response<LicenseRecordResponseDto>
}
