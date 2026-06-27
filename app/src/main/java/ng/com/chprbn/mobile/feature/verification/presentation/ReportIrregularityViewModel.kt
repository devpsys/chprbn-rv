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
import ng.com.chprbn.mobile.feature.verification.domain.model.IrregularityRemark
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecordResult
import ng.com.chprbn.mobile.feature.verification.domain.model.SubmitIrregularityReportResult
import ng.com.chprbn.mobile.feature.verification.domain.usecase.GetLicenseRecordUseCase
import ng.com.chprbn.mobile.feature.verification.domain.usecase.SubmitIrregularityReportUseCase
import javax.inject.Inject

sealed interface ReportIrregularitySubmitState {
    data object Idle : ReportIrregularitySubmitState
    data object Submitting : ReportIrregularitySubmitState
    data object Success : ReportIrregularitySubmitState
    data class Error(val message: String) : ReportIrregularitySubmitState
}

data class ReportIrregularityFieldErrors(
    val nameOnCard: String? = null,
    val licenseNumber: String? = null,
    val cadre: String? = null,
    val gender: String? = null,
    val remark: String? = null,
    val snapshot: String? = null
)

data class ReportIrregularityUiState(
    val nameOnCard: String = "",
    val licenseNumber: String = "",
    val cadre: String = "",
    val gender: String = "",
    val selectedRemark: IrregularityRemark? = null,
    val snapshotContentUri: String? = null,
    val fieldErrors: ReportIrregularityFieldErrors = ReportIrregularityFieldErrors(),
    val submitState: ReportIrregularitySubmitState = ReportIrregularitySubmitState.Idle
)

@HiltViewModel
class ReportIrregularityViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLicenseRecordUseCase: GetLicenseRecordUseCase,
    private val submitIrregularityReportUseCase: SubmitIrregularityReportUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportIrregularityUiState())
    val uiState: StateFlow<ReportIrregularityUiState> = _uiState.asStateFlow()

    init {
        val registrationNumber = Uri.decode(
            savedStateHandle.get<String>("registrationNumber").orEmpty()
        ).trim()
        if (registrationNumber.isNotEmpty()) {
            // Always pre-fill the license number from the nav arg so the "No record
            // found" report-irregularity flow lands with the scanned/entered number
            // already in place. The remaining fields are pre-populated from the
            // cached record on Success; on NotFound / Error they stay empty for the
            // user to fill in manually.
            _uiState.update { it.copy(licenseNumber = registrationNumber.uppercase()) }
            viewModelScope.launch {
                val result = getLicenseRecordUseCase(registrationNumber)
                if (result is LicenseRecordResult.Success) {
                    val record = result.record
                    _uiState.update {
                        it.copy(
                            nameOnCard = record.fullName,
                            licenseNumber = record.registrationNumber.uppercase(),
                            cadre = record.profession,
                            gender = record.gender
                        )
                    }
                }
            }
        }
    }

    fun onNameOnCardChange(value: String) {
        _uiState.update {
            it.copy(
                nameOnCard = value,
                fieldErrors = it.fieldErrors.copy(nameOnCard = null)
            )
        }
    }

    fun onLicenseNumberChange(value: String) {
        _uiState.update {
            it.copy(
                licenseNumber = value,
                fieldErrors = it.fieldErrors.copy(licenseNumber = null)
            )
        }
    }

    fun onCadreChange(value: String) {
        _uiState.update { it.copy(cadre = value, fieldErrors = it.fieldErrors.copy(cadre = null)) }
    }

    fun onGenderChange(value: String) {
        _uiState.update {
            it.copy(
                gender = value,
                fieldErrors = it.fieldErrors.copy(gender = null)
            )
        }
    }

    fun onRemarkSelected(remark: IrregularityRemark) {
        _uiState.update {
            it.copy(
                selectedRemark = remark,
                fieldErrors = it.fieldErrors.copy(remark = null)
            )
        }
    }

    fun onSnapshotUriChange(uriString: String?) {
        _uiState.update {
            it.copy(
                snapshotContentUri = uriString,
                fieldErrors = it.fieldErrors.copy(snapshot = null)
            )
        }
    }

    fun clearSnapshot() {
        _uiState.update {
            it.copy(
                snapshotContentUri = null,
                fieldErrors = it.fieldErrors.copy(snapshot = null)
            )
        }
    }

    fun submit() {
        if (_uiState.value.submitState is ReportIrregularitySubmitState.Submitting) return
        val s = _uiState.value
        val requiredMessage = context.getString(R.string.irregularity_error_required)
        val selectRemarkMessage = context.getString(R.string.irregularity_error_select_remark)
        val attachImageMessage = context.getString(R.string.irregularity_error_attach_image)
        val errors = ReportIrregularityFieldErrors(
            nameOnCard = if (s.nameOnCard.isBlank()) requiredMessage else null,
            licenseNumber = if (s.licenseNumber.isBlank()) requiredMessage else null,
            cadre = if (s.cadre.isBlank()) requiredMessage else null,
            gender = if (s.gender.isBlank()) requiredMessage else null,
            remark = if (s.selectedRemark == null) selectRemarkMessage else null,
            snapshot = if (s.snapshotContentUri.isNullOrBlank()) attachImageMessage else null
        )
        val hasError = listOfNotNull(
            errors.nameOnCard,
            errors.licenseNumber,
            errors.cadre,
            errors.gender,
            errors.remark,
            errors.snapshot
        ).isNotEmpty()
        if (hasError) {
            _uiState.update {
                it.copy(fieldErrors = errors, submitState = ReportIrregularitySubmitState.Idle)
            }
            return
        }

        _uiState.update {
            it.copy(
                submitState = ReportIrregularitySubmitState.Submitting,
                fieldErrors = ReportIrregularityFieldErrors()
            )
        }
        viewModelScope.launch {
            val result = submitIrregularityReportUseCase(
                nameOnCard = s.nameOnCard,
                licenseNumber = s.licenseNumber,
                cadre = s.cadre,
                gender = s.gender,
                remark = s.selectedRemark,
                snapshotContentUri = s.snapshotContentUri
            )
            _uiState.update { state ->
                when (result) {
                    SubmitIrregularityReportResult.Success ->
                        state.copy(submitState = ReportIrregularitySubmitState.Success)

                    is SubmitIrregularityReportResult.Error ->
                        state.copy(submitState = ReportIrregularitySubmitState.Error(result.message))
                }
            }
        }
    }

    fun consumeSubmitState() {
        _uiState.update { it.copy(submitState = ReportIrregularitySubmitState.Idle) }
    }
}
