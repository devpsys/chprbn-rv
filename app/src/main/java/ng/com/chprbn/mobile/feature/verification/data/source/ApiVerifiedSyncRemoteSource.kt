package ng.com.chprbn.mobile.feature.verification.data.source

import com.google.gson.Gson
import ng.com.chprbn.mobile.feature.auth.data.dto.ApiErrorDto
import ng.com.chprbn.mobile.feature.verification.data.api.VerifiedSyncApiService
import ng.com.chprbn.mobile.feature.verification.data.dto.VerifiedSyncRequestDto
import javax.inject.Inject

/**
 * Verification-side sync impl. **The wire is still per-row** —
 * `/practitioners/verified-sync` accepts one row per HTTP request — so
 * the batch contract is fulfilled by looping internally. Each row
 * independently produces a `Result<Unit>` entry in the returned map
 * keyed by `license_number`. The handler is free to treat the map as a
 * black box; the per-row vs. batched wire is invisible to it.
 *
 * Replace this loop with a single batched call when the
 * `POST /practitioners/verified-sync/batch` endpoint ships (see
 * `docs/api/full-api-documentation.md` §7.4).
 */
class ApiVerifiedSyncRemoteSource @Inject constructor(
    private val api: VerifiedSyncApiService,
    private val gson: Gson,
) : VerifiedSyncRemoteSource {

    override suspend fun uploadVerifiedBatch(
        rows: List<VerifiedSyncRequestDto>,
    ): Map<String, Result<Unit>> {
        if (rows.isEmpty()) return emptyMap()
        val outcomes = LinkedHashMap<String, Result<Unit>>(rows.size)
        for (row in rows) {
            outcomes[row.licenseNumber] = uploadSingle(row)
        }
        return outcomes
    }

    private suspend fun uploadSingle(request: VerifiedSyncRequestDto): Result<Unit> = runCatching {
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
