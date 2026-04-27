package ng.com.chprbn.mobile.feature.verification.presentation

import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord

sealed class RecordDetailUiState {
    data object Loading : RecordDetailUiState()
    data class Success(val record: LicenseRecord) : RecordDetailUiState()
    data object NotFound : RecordDetailUiState()
    data class Error(val message: String) : RecordDetailUiState()
}
