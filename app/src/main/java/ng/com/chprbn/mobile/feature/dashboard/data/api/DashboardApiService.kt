package ng.com.chprbn.mobile.feature.dashboard.data.api

import ng.com.chprbn.mobile.feature.dashboard.data.dto.ProfileResponseDto
import retrofit2.Response
import retrofit2.http.GET

/**
 * Dashboard remote API. Use for profile refresh and future dashboard endpoints.
 * Base URL shared with auth (e.g. https://chprbn.gov.ng/api/v1/).
 */
interface DashboardApiService {

    @GET("dashboard/profile")
    suspend fun getProfile(): Response<ProfileResponseDto>
}
