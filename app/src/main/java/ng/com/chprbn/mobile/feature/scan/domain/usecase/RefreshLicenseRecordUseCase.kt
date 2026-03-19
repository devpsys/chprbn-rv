package ng.com.chprbn.mobile.feature.scan.domain.usecase

import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.scan.domain.repository.ScanRepository
import javax.inject.Inject

/**
 * Use case: silently refresh a license record from the remote API and update cache.
 * Returns updated record if API success, null otherwise. Caller can update UI with the result.
 */
class RefreshLicenseRecordUseCase @Inject constructor(
    private val repository: ScanRepository
) {
    suspend operator fun invoke(registrationNumber: String): LicenseRecord? {
        val trimmed = registrationNumber.trim()
        if (trimmed.isEmpty()) return null
        return repository.refreshLicenseRecord(trimmed)
    }
}
