package ng.com.chprbn.mobile.feature.sync.data.source

import com.google.gson.Gson
import ng.com.chprbn.mobile.feature.auth.data.dto.ApiErrorDto
import ng.com.chprbn.mobile.feature.sync.data.api.VerifiedSyncApiService
import ng.com.chprbn.mobile.feature.sync.data.dto.VerifiedSyncRequestDto
import javax.inject.Inject

class ApiVerifiedSyncRemoteSource @Inject constructor(
    private val api: VerifiedSyncApiService,
    private val gson: Gson
) : VerifiedSyncRemoteSource {

    override suspend fun uploadVerifiedRecord(request: VerifiedSyncRequestDto): Result<Unit> =
        runCatching {
            val response = api.syncVerifiedLicense(request)
            if (!response.isSuccessful) {
                val raw = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                val message = parseApiErrorMessage(raw)
                    ?: raw
                    ?: "HTTP ${response.code()}"
                error(message)
            }
            val envelope = response.body()
            if (envelope != null && !envelope.status) {
                error(envelope.message ?: "Sync rejected.")
            }
            Unit
        }

    private fun parseApiErrorMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return runCatching { gson.fromJson(raw, ApiErrorDto::class.java).message }.getOrNull()
    }
}
