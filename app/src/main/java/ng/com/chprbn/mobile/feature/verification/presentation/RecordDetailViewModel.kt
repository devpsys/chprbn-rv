package ng.com.chprbn.mobile.feature.verification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecordResult
import ng.com.chprbn.mobile.feature.verification.domain.usecase.GetLicenseRecordUseCase
import ng.com.chprbn.mobile.feature.verification.domain.usecase.RefreshLicenseRecordUseCase
import javax.inject.Inject

@HiltViewModel
class RecordDetailViewModel @Inject constructor(
    private val getLicenseRecordUseCase: GetLicenseRecordUseCase,
    private val refreshLicenseRecordUseCase: RefreshLicenseRecordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<RecordDetailUiState>(RecordDetailUiState.Loading)
    val state: StateFlow<RecordDetailUiState> = _state.asStateFlow()

    fun loadRecord(registrationNumber: String) {
        viewModelScope.launch {
            _state.value = RecordDetailUiState.Loading
            when (val result = getLicenseRecordUseCase(registrationNumber)) {
                is LicenseRecordResult.Success -> {
                    _state.value = RecordDetailUiState.Success(result.record)
                    // Silent refresh: fetch from API in background and update UI if we get fresh data
                    launch {
                        refreshLicenseRecordUseCase(registrationNumber)?.let { updated ->
                            _state.value = RecordDetailUiState.Success(updated)
                        }
                    }
                }

                is LicenseRecordResult.NotFound -> _state.value = RecordDetailUiState.NotFound
                is LicenseRecordResult.Error -> _state.value =
                    RecordDetailUiState.Error(result.message)
            }
        }
    }

    fun retry(registrationNumber: String) = loadRecord(registrationNumber)
}
