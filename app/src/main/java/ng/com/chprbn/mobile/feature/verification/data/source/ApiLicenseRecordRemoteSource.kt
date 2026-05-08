package ng.com.chprbn.mobile.feature.verification.data.source

import ng.com.chprbn.mobile.feature.verification.data.api.LicenseApiService
import ng.com.chprbn.mobile.feature.verification.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord
import javax.inject.Inject

/**
 * Calls GET practitioners/license on the mobile API (envelope + Bearer token).
 */
class ApiLicenseRecordRemoteSource @Inject constructor(
    private val licenseApiService: LicenseApiService
) : LicenseRecordRemoteSource {

    override suspend fun getLicenseRecord(registrationNumber: String): LicenseRecord? {
        val trimmed = registrationNumber.trim()
        if (trimmed.isEmpty()) return null
        val response = runCatching {
            licenseApiService.getLicenseRecord(trimmed)
        }.getOrElse { _ -> return null }
        if (!response.isSuccessful || response.code() == 404) return null
        val envelope = response.body() ?: return null
        if (!envelope.status) return null
        val data = envelope.data ?: return null
        return data.toDomain()
    }
}
