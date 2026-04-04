package ng.com.chprbn.mobile.feature.dashboard.data.api

import ng.com.chprbn.mobile.feature.auth.data.api.AuthApiService
import ng.com.chprbn.mobile.feature.auth.data.dto.ProfileEnvelopeDto
import retrofit2.Response
import retrofit2.http.GET

/**
 * Dashboard mobile API. [getProfile] returns the same JSON as [AuthApiService.getCurrentUser].
 */
interface DashboardApiService {

    @GET("dashboard/profile")
    suspend fun getProfile(): Response<ProfileEnvelopeDto>
}
