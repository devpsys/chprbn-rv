package ng.com.chprbn.mobile.feature.auth.data.api

import ng.com.chprbn.mobile.feature.auth.data.dto.AdhocProfileEnvelopeDto
import ng.com.chprbn.mobile.feature.auth.data.dto.LoginEnvelopeDto
import ng.com.chprbn.mobile.feature.auth.data.dto.LoginRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Mobile API v1 auth for **adhoc** field-officer accounts (`adhoc_users`).
 * Same token envelope as tutor login; equivalent path: `POST auth/adhoc/login`.
 */
interface AuthApiService {

    /** Equivalent to POST `auth/adhoc/login`. */
    @POST("adhoc/login")
    suspend fun adhocLogin(
        @Body request: LoginRequestDto
    ): Response<LoginEnvelopeDto>

    @GET("adhoc/profile")
    suspend fun getAdhocProfile(): Response<AdhocProfileEnvelopeDto>
}
