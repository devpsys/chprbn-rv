package ng.com.chprbn.mobile.feature.verification.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecordResult
import ng.com.chprbn.mobile.feature.verification.domain.model.SaveVerifiedLicenseResult
import ng.com.chprbn.mobile.feature.verification.domain.usecase.GetLicenseRecordUseCase
import ng.com.chprbn.mobile.feature.verification.domain.usecase.SaveVerifiedLicenseUseCase
import javax.inject.Inject

sealed interface VerificationFormLoadState {
    data object Loading : VerificationFormLoadState
    data object Loaded : VerificationFormLoadState
    data object NotFound : VerificationFormLoadState
    data class Error(val message: String) : VerificationFormLoadState
}

sealed interface SaveVerificationState {
    data object Idle : SaveVerificationState
    data object Saving : SaveVerificationState
    data object Success : SaveVerificationState
    data class Error(val message: String) : SaveVerificationState
}

data class VerificationFormUiState(
    val loadState: VerificationFormLoadState = VerificationFormLoadState.Loading,
    val licenseRecord: LicenseRecord? = null,
    val selectedOfficerRemark: String = "",
    val officerRemarkOptions: List<String> = emptyList(),
    val saveState: SaveVerificationState = SaveVerificationState.Idle
)

@HiltViewModel
class VerificationFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLicenseRecordUseCase: GetLicenseRecordUseCase,
    private val saveVerifiedLicenseUseCase: SaveVerifiedLicenseUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        VerificationFormUiState(
            officerRemarkOptions = context.resources
                .getStringArray(R.array.officer_remark_options)
                .toList()
        )
    )
    val uiState: StateFlow<VerificationFormUiState> = _uiState.asStateFlow()

    init {
        val registrationNumber = Uri.decode(
            savedStateHandle.get<String>("registrationNumber").orEmpty()
        ).trim()
        if (registrationNumber.isEmpty()) {
            _uiState.update { it.copy(loadState = VerificationFormLoadState.NotFound) }
        } else {
            viewModelScope.launch {
                val result = getLicenseRecordUseCase(registrationNumber)
                _uiState.update {
                    when (result) {
                        is LicenseRecordResult.Success -> it.copy(
                            loadState = VerificationFormLoadState.Loaded,
                            licenseRecord = result.record
                        )
                        LicenseRecordResult.NotFound -> it.copy(
                            loadState = VerificationFormLoadState.NotFound
                        )
                        is LicenseRecordResult.Error -> it.copy(
                            loadState = VerificationFormLoadState.Error(result.message)
                        )
                    }
                }
            }
        }
    }

    fun onOfficerRemarkSelected(value: String) {
        _uiState.update { it.copy(selectedOfficerRemark = value) }
    }

    fun saveVerification() {
        if (uiState.value.saveState is SaveVerificationState.Saving) return
        val record = uiState.value.licenseRecord
        if (record == null) {
            _uiState.update {
                it.copy(saveState = SaveVerificationState.Error(
                    context.getString(R.string.verification_form_error_no_record)
                ))
            }
            return
        }

        val officerRemark = uiState.value.selectedOfficerRemark

        _uiState.update { it.copy(saveState = SaveVerificationState.Saving) }
        viewModelScope.launch {
            val result = saveVerifiedLicenseUseCase(
                licenseRecord = record,
                remark = officerRemark
            )
            _uiState.update {
                when (result) {
                    SaveVerifiedLicenseResult.Success ->
                        it.copy(saveState = SaveVerificationState.Success)

                    is SaveVerifiedLicenseResult.Error ->
                        it.copy(saveState = SaveVerificationState.Error(result.message))
                }
            }
        }
    }

    fun consumeSaveState() {
        _uiState.update { it.copy(saveState = SaveVerificationState.Idle) }
    }
}
