package ng.com.chprbn.mobile.feature.scan.domain.usecase

import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecordResult
import ng.com.chprbn.mobile.feature.scan.domain.repository.ScanRepository
import javax.inject.Inject

/**
 * Use case: get a license record by registration number (local-first, then remote; caches result).
 * Returns Success(record), NotFound, or Error(message).
 */
class GetLicenseRecordUseCase @Inject constructor(
    private val repository: ScanRepository
) {
    suspend operator fun invoke(registrationNumber: String): LicenseRecordResult {
        val trimmed = registrationNumber.trim()
        if (trimmed.isEmpty()) return LicenseRecordResult.Error("License number is required.")
        return repository.getLicenseRecord(trimmed)
    }
}
