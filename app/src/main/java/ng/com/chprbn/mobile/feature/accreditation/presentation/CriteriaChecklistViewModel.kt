package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class CriteriaChecklistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val institutionId: String =
        savedStateHandle.get<String>("institutionId").orEmpty()

    private val _uiState = MutableStateFlow<CriteriaChecklistUiState>(
        if (institutionId.isBlank()) {
            CriteriaChecklistUiState.Error("Missing institution")
        } else {
            CriteriaChecklistUiState.placeholder(institutionId)
        },
    )
    val uiState: StateFlow<CriteriaChecklistUiState> = _uiState.asStateFlow()

    fun onMetStatusChange(kind: CriteriaGroupKind, status: CriterionMetStatus) =
        updateGroup(kind) { it.copy(metStatus = status) }

    fun onCheckItemToggled(kind: CriteriaGroupKind, itemId: String) =
        updateGroup(kind) { group ->
            group.copy(
                checkItems = group.checkItems.map { item ->
                    if (item.id == itemId) item.copy(checked = !item.checked) else item
                },
            )
        }

    fun onEvidenceNotesChange(kind: CriteriaGroupKind, notes: String) =
        updateGroup(kind) { it.copy(evidenceNotes = notes) }

    fun onHrCountChange(kind: CriteriaGroupKind, field: HrCountField, value: String) =
        updateGroup(kind) { group ->
            val counts = group.hrCounts ?: CriteriaHrCountUiState("", "", "", "")
            val next = when (field) {
                HrCountField.FullTimePhc -> counts.copy(fullTimePhcTutors = digitsOnly(value))
                HrCountField.PartTimePhc -> counts.copy(partTimePhcTutors = digitsOnly(value))
                HrCountField.FullTimeLecturers -> counts.copy(fullTimeLecturers = digitsOnly(value))
                HrCountField.AdminSupport -> counts.copy(adminSupport = digitsOnly(value))
            }
            group.copy(hrCounts = next)
        }

    fun onCapitalAmountChange(value: String) =
        updateGroup(CriteriaGroupKind.CapitalBase) {
            it.copy(capitalAmountNaira = digitsOnly(value))
        }

    fun onStatementOfAccountConfirmed(checked: Boolean) =
        updateGroup(CriteriaGroupKind.CapitalBase) {
            it.copy(statementOfAccountConfirmed = checked)
        }

    fun onSalaryChange(kind: CriteriaGroupKind, field: SalaryField, value: String) =
        updateGroup(kind) { group ->
            val amount = digitsOnly(value)
            when (field) {
                SalaryField.Hod -> group.copy(hodSalaryNaira = amount)
                SalaryField.PhcTutorWithDegree -> group.copy(phcTutorWithDegreeSalaryNaira = amount)
                SalaryField.PhcTutorWithoutDegree -> group.copy(phcTutorWithoutDegreeSalaryNaira = amount)
            }
        }

    fun onRemoveAttachment(kind: CriteriaGroupKind, attachmentId: String) =
        updateGroup(kind) { group ->
            group.copy(attachments = group.attachments.filterNot { it.id == attachmentId })
        }

    fun onSaveChecklistDraft() {
        updateSuccess { it.copy(showSavedDialog = true) }
    }

    fun onSaveAsDraft() = onSaveChecklistDraft()

    fun dismissSavedDialog() {
        updateSuccess { it.copy(showSavedDialog = false) }
    }

    private fun digitsOnly(value: String): String = value.filter { it.isDigit() }

    private fun updateGroup(
        kind: CriteriaGroupKind,
        transform: (CriteriaGroupUiState) -> CriteriaGroupUiState,
    ) {
        updateSuccess { data ->
            data.copy(
                groups = data.groups.map { group ->
                    if (group.kind == kind) transform(group) else group
                },
            )
        }
    }

    private fun updateSuccess(transform: (CriteriaChecklistData) -> CriteriaChecklistData) {
        _uiState.update { state ->
            val success = state as? CriteriaChecklistUiState.Success ?: return@update state
            CriteriaChecklistUiState.Success(transform(success.data))
        }
    }
}

enum class HrCountField {
    FullTimePhc,
    PartTimePhc,
    FullTimeLecturers,
    AdminSupport,
}

enum class SalaryField {
    Hod,
    PhcTutorWithDegree,
    PhcTutorWithoutDegree,
}
