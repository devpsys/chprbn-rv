package ng.com.chprbn.mobile.feature.verification.presentation

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
import ng.com.chprbn.mobile.feature.verification.domain.model.IrregularityRemark
import ng.com.chprbn.mobile.feature.verification.domain.model.IrregularityReportPrefill
import ng.com.chprbn.mobile.feature.verification.domain.model.SubmitIrregularityReportResult
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
    private val gson: Gson,
    private val submitIrregularityReportUseCase: SubmitIrregularityReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(run {
        val encoded = savedStateHandle.get<String>("prefillJson").orEmpty()
        val json = Uri.decode(encoded)
        val prefill = runCatching {
            if (json.isBlank()) IrregularityReportPrefill()
            else gson.fromJson(json, IrregularityReportPrefill::class.java)
        }.getOrNull() ?: IrregularityReportPrefill()
        ReportIrregularityUiState(
            nameOnCard = prefill.nameOnCard,
            licenseNumber = prefill.licenseNumber,
            cadre = prefill.cadre,
            gender = prefill.gender
        )
    })
    val uiState: StateFlow<ReportIrregularityUiState> = _uiState.asStateFlow()

    fun onNameOnCardChange(value: String) {
        _uiState.update { it.copy(nameOnCard = value, fieldErrors = it.fieldErrors.copy(nameOnCard = null)) }
    }

    fun onLicenseNumberChange(value: String) {
        _uiState.update { it.copy(licenseNumber = value, fieldErrors = it.fieldErrors.copy(licenseNumber = null)) }
    }

    fun onCadreChange(value: String) {
        _uiState.update { it.copy(cadre = value, fieldErrors = it.fieldErrors.copy(cadre = null)) }
    }

    fun onGenderChange(value: String) {
        _uiState.update { it.copy(gender = value, fieldErrors = it.fieldErrors.copy(gender = null)) }
    }

    fun onRemarkSelected(remark: IrregularityRemark) {
        _uiState.update { it.copy(selectedRemark = remark, fieldErrors = it.fieldErrors.copy(remark = null)) }
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
        _uiState.update { it.copy(snapshotContentUri = null, fieldErrors = it.fieldErrors.copy(snapshot = null)) }
    }

    fun submit() {
        if (_uiState.value.submitState is ReportIrregularitySubmitState.Submitting) return
        val s = _uiState.value
        val errors = ReportIrregularityFieldErrors(
            nameOnCard = if (s.nameOnCard.isBlank()) "Required" else null,
            licenseNumber = if (s.licenseNumber.isBlank()) "Required" else null,
            cadre = if (s.cadre.isBlank()) "Required" else null,
            gender = if (s.gender.isBlank()) "Required" else null,
            remark = if (s.selectedRemark == null) "Select an option" else null,
            snapshot = if (s.snapshotContentUri.isNullOrBlank()) "Attach an image" else null
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

        _uiState.update { it.copy(submitState = ReportIrregularitySubmitState.Submitting, fieldErrors = ReportIrregularityFieldErrors()) }
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
