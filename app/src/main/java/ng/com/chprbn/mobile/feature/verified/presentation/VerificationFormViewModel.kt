package ng.com.chprbn.mobile.feature.verified.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord
import javax.inject.Inject

data class VerificationFormUiState(
    val licenseRecord: LicenseRecord? = null,
    val verificationLocation: String = "",
    val practitionerPresent: Boolean = false,
    val officerRemarks: String = ""
) {
    /** True when license status is "Active", so the "Mark as Verified" switch can be enabled. */
    val isVerifiedSwitchEnabled: Boolean =
        licenseRecord?.licenseStatus.equals("Active", ignoreCase = true) == true
}

@HiltViewModel
class VerificationFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(run {
        val encodedJson = savedStateHandle.get<String>("licenseRecordJson").orEmpty()
        val json = Uri.decode(encodedJson)
        val record = runCatching {
            if (json.isBlank()) null else gson.fromJson(json, LicenseRecord::class.java)
        }.getOrNull()
        val withRecord = VerificationFormUiState(licenseRecord = record)
        withRecord.copy(practitionerPresent = withRecord.isVerifiedSwitchEnabled)
    })
    val uiState: StateFlow<VerificationFormUiState> = _uiState.asStateFlow()

    fun onVerificationLocationChange(value: String) {
        _uiState.update { it.copy(verificationLocation = value) }
    }

    fun onPractitionerPresentChange(value: Boolean) {
        _uiState.update { it.copy(practitionerPresent = value) }
    }

    fun onOfficerRemarksChange(value: String) {
        _uiState.update { it.copy(officerRemarks = value) }
    }

    fun saveVerification() {
        // TODO: domain/data layer — submit verification using uiState.value.licenseRecord
    }
}
