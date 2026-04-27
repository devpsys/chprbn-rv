package ng.com.chprbn.mobile.feature.verification.domain.model

sealed interface SaveVerifiedLicenseResult {
    data object Success : SaveVerifiedLicenseResult
    data class Error(val message: String) : SaveVerifiedLicenseResult
}

