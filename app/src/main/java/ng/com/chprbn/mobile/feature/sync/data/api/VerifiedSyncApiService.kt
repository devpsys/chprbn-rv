package ng.com.chprbn.mobile.feature.sync.data.api

import ng.com.chprbn.mobile.feature.sync.data.dto.VerifiedSyncRequestDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Remote API for pushing verified practitioner rows. Endpoint path is provisional until backend is fixed.
 */
interface VerifiedSyncApiService {

    @POST("practitioners/verified-sync")
    suspend fun syncVerifiedLicense(
        @Body body: VerifiedSyncRequestDto
    ): Response<ResponseBody>
}
