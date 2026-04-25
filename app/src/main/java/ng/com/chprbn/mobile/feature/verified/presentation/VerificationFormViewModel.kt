package ng.com.chprbn.mobile.feature.verified.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.verified.domain.model.SaveVerifiedLicenseResult
import ng.com.chprbn.mobile.feature.verified.domain.usecase.SaveVerifiedLicenseUseCase
import javax.inject.Inject

sealed interface SaveVerificationState {
    data object Idle : SaveVerificationState
    data object Saving : SaveVerificationState
    data object Success : SaveVerificationState
    data class Error(val message: String) : SaveVerificationState
}

data class VerificationFormUiState(
    val licenseRecord: LicenseRecord? = null,
    val selectedOfficerRemark: String = "",
    val saveState: SaveVerificationState = SaveVerificationState.Idle
)

@HiltViewModel
class VerificationFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gson: Gson,
    private val saveVerifiedLicenseUseCase: SaveVerifiedLicenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(run {
        val encodedJson = savedStateHandle.get<String>("licenseRecordJson").orEmpty()
        val json = Uri.decode(encodedJson)
        val record = runCatching {
            if (json.isBlank()) null else gson.fromJson(json, LicenseRecord::class.java)
        }.getOrNull()
        VerificationFormUiState(licenseRecord = record)
    })
    val uiState: StateFlow<VerificationFormUiState> = _uiState.asStateFlow()

    fun onOfficerRemarkSelected(value: String) {
        _uiState.update { it.copy(selectedOfficerRemark = value) }
    }

    fun saveVerification() {
        if (uiState.value.saveState is SaveVerificationState.Saving) return
        val record = uiState.value.licenseRecord
        if (record == null) {
            _uiState.update { it.copy(saveState = SaveVerificationState.Error("No license record found to verify.")) }
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

    companion object {
        val officerRemarkOptions = listOf(
            "Documents verified; identity matches register",
            "Practitioner present; credentials checked",
            "Routine verification completed",
            "License confirmed valid for practice"
        )
    }
}
