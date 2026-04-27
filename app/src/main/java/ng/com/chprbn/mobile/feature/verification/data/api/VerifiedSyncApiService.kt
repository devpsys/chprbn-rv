package ng.com.chprbn.mobile.feature.verification.data.api

import ng.com.chprbn.mobile.feature.verification.data.dto.VerifiedSyncEnvelopeDto
import ng.com.chprbn.mobile.feature.verification.data.dto.VerifiedSyncRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface VerifiedSyncApiService {

    @POST("practitioners/verified-sync")
    suspend fun syncVerifiedLicense(
        @Body body: VerifiedSyncRequestDto
    ): Response<VerifiedSyncEnvelopeDto>
}
