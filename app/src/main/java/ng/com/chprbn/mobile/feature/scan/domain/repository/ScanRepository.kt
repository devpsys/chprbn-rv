package ng.com.chprbn.mobile.feature.scan.domain.repository

import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecordResult

/**
 * Domain contract for license record lookup.
 * Strategy: local first, then remote; cache remote result and return.
 * When serving from cache, call [refreshLicenseRecord] in background to silently update cache/UI.
 */
interface ScanRepository {
    /**
     * Get license record by registration/license number.
     * Checks local DB first; if missing, fetches from API and caches.
     * @return [LicenseRecordResult] Success(record), NotFound, or Error(message)
     */
    suspend fun getLicenseRecord(registrationNumber: String): LicenseRecordResult

    /**
     * Fetch from remote and update cache. Call after displaying cached data for silent refresh.
     * @return updated [LicenseRecord] if API success, null on 404/network/error (cache unchanged).
     */
    suspend fun refreshLicenseRecord(registrationNumber: String): LicenseRecord?
}
