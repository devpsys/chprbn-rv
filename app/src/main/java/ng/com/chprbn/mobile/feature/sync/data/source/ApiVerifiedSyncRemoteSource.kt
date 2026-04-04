package ng.com.chprbn.mobile.feature.sync.data.source

import ng.com.chprbn.mobile.feature.sync.data.api.VerifiedSyncApiService
import ng.com.chprbn.mobile.feature.sync.data.dto.VerifiedSyncRequestDto
import javax.inject.Inject

class ApiVerifiedSyncRemoteSource @Inject constructor(
    private val api: VerifiedSyncApiService
) : VerifiedSyncRemoteSource {

    override suspend fun uploadVerifiedRecord(request: VerifiedSyncRequestDto): Result<Unit> =
        runCatching {
            val response = api.syncVerifiedLicense(request)
            if (!response.isSuccessful) {
                val body = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                error(body ?: "HTTP ${response.code()}")
            }
            val envelope = response.body()
            if (envelope != null && !envelope.status) {
                error(envelope.message ?: "Sync rejected.")
            }
            Unit
        }
}
