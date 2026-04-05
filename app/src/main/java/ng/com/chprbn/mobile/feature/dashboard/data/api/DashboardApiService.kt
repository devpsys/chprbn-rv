package ng.com.chprbn.mobile.feature.dashboard.data.api

import ng.com.chprbn.mobile.feature.auth.data.dto.ProfileEnvelopeDto
import retrofit2.Response
import retrofit2.http.GET

/**
 * Practitioner (tutor) profile refresh — not used for adhoc field-officer sessions.
 */
interface DashboardApiService {

    @GET("dashboard/profile")
    suspend fun getProfile(): Response<ProfileEnvelopeDto>
}
