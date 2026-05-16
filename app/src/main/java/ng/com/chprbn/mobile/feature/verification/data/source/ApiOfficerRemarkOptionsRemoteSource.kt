package ng.com.chprbn.mobile.feature.verification.data.source

import ng.com.chprbn.mobile.feature.verification.data.api.OfficerRemarkOptionsApiService
import javax.inject.Inject

/**
 * Retrofit-backed remote source. Never throws; any failure
 * (network error, non-2xx, parse error, empty `data`) degrades to
 * `null` so the repository can fall back to bundled defaults.
 */
class ApiOfficerRemarkOptionsRemoteSource @Inject constructor(
    private val api: OfficerRemarkOptionsApiService,
) : OfficerRemarkOptionsRemoteSource {

    override suspend fun fetchOptions(): List<String>? = runCatching {
        val response = api.getOfficerRemarkOptions()
        if (!response.isSuccessful) return@runCatching null
        response.body()?.data?.options
    }.getOrNull()
}
