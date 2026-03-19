package ng.com.chprbn.mobile.feature.scan.data.source

import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord
import javax.inject.Inject

/**
 * Tries the primary remote source (API); if it returns null, falls back to the fake source.
 * Enables development without a live backend while keeping real API when available.
 */
class CompositeLicenseRecordRemoteSource @Inject constructor(
    private val primary: LicenseRecordRemoteSource,
    private val fallback: LicenseRecordRemoteSource
) : LicenseRecordRemoteSource {

    override suspend fun getLicenseRecord(registrationNumber: String): LicenseRecord? =
        primary.getLicenseRecord(registrationNumber)
            ?: fallback.getLicenseRecord(registrationNumber)
}
