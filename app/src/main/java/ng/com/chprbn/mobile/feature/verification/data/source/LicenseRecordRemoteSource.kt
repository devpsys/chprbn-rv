package ng.com.chprbn.mobile.feature.verification.data.source

import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord

/**
 * Abstraction for fetching a license record from a remote source (API or fake).
 * Allows swapping real API for fake data in development without changing repository.
 */
interface LicenseRecordRemoteSource {
    /**
     * Fetches a license record by registration/license number.
     * @return the record if found, null if not found or on error
     */
    suspend fun getLicenseRecord(registrationNumber: String): LicenseRecord?
}
