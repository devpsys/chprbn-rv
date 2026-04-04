package ng.com.chprbn.mobile.feature.scan.data.source

import ng.com.chprbn.mobile.feature.scan.data.api.ScanApiService
import ng.com.chprbn.mobile.feature.scan.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord
import javax.inject.Inject

/**
 * Calls GET practitioners/license on the mobile API (envelope + Bearer token).
 */
class ApiLicenseRecordRemoteSource @Inject constructor(
    private val scanApiService: ScanApiService
) : LicenseRecordRemoteSource {

    override suspend fun getLicenseRecord(registrationNumber: String): LicenseRecord? {
        val trimmed = registrationNumber.trim()
        if (trimmed.isEmpty()) return null
        val response = runCatching {
            scanApiService.getLicenseRecord(trimmed)
        }.getOrElse { _ -> return null }
        if (!response.isSuccessful || response.code() == 404) return null
        val envelope = response.body() ?: return null
        if (!envelope.status) return null
        val data = envelope.data ?: return null
        return data.toDomain()
    }
}
