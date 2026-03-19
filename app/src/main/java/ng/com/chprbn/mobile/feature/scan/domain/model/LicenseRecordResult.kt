package ng.com.chprbn.mobile.feature.scan.domain.model

/**
 * Result of a license record lookup.
 */
sealed class LicenseRecordResult {
    data class Success(val record: LicenseRecord) : LicenseRecordResult()
    data object NotFound : LicenseRecordResult()
    data class Error(val message: String) : LicenseRecordResult()
}
