package ng.com.chprbn.mobile.feature.auth.data.api

import ng.com.chprbn.mobile.feature.auth.data.dto.LoginRequestDto
import ng.com.chprbn.mobile.feature.auth.data.dto.LoginEnvelopeDto
import ng.com.chprbn.mobile.feature.auth.data.dto.ProfileEnvelopeDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    /** Equivalent to POST `auth/login` on the same mobile base URL. */
    @POST("login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<LoginEnvelopeDto>

    @GET("user")
    suspend fun getCurrentUser(): Response<ProfileEnvelopeDto>
}
