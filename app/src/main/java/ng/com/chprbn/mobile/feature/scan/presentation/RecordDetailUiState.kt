package ng.com.chprbn.mobile.feature.scan.presentation

import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord

sealed class RecordDetailUiState {
    data object Loading : RecordDetailUiState()
    data class Success(val record: LicenseRecord) : RecordDetailUiState()
    data object NotFound : RecordDetailUiState()
    data class Error(val message: String) : RecordDetailUiState()
}
