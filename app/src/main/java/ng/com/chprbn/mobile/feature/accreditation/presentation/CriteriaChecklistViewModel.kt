package ng.com.chprbn.mobile.feature.accreditation.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.AddAccreditationAttachmentUseCase
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.GetCriteriaChecklistDraftUseCase
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.RemoveAccreditationAttachmentUseCase
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.SaveCriteriaChecklistDraftUseCase

@HiltViewModel
class CriteriaChecklistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCriteriaDraft: GetCriteriaChecklistDraftUseCase,
    private val saveCriteriaDraft: SaveCriteriaChecklistDraftUseCase,
    private val addAttachment: AddAccreditationAttachmentUseCase,
    private val removeAttachment: RemoveAccreditationAttachmentUseCase,
) : ViewModel() {

    private val institutionId: String =
        savedStateHandle.get<String>("institutionId").orEmpty()

    private val _uiState = MutableStateFlow<CriteriaChecklistUiState>(
        if (institutionId.isBlank()) {
            CriteriaChecklistUiState.Error("Missing institution")
        } else {
            CriteriaChecklistUiState.Loading
        },
    )
    val uiState: StateFlow<CriteriaChecklistUiState> = _uiState.asStateFlow()

    init {
        if (institutionId.isNotBlank()) {
            viewModelScope.launch { load() }
        }
    }

    private suspend fun load() {
        val record = getCriteriaDraft(institutionId)
        val fromJson = parseCriteriaChecklistData(record?.payloadJson)
        val base = fromJson
            ?: CriteriaChecklistUiState.placeholder(institutionId).data
        _uiState.value = CriteriaChecklistUiState.Success(
            base.copy(institutionId = institutionId, showSavedDialog = false),
        )
    }

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

    fun onAttachmentAdded(kind: CriteriaGroupKind, contentUri: String, isPhoto: Boolean) {
        viewModelScope.launch {
            val stored = addAttachment(
                institutionId = institutionId,
                parentKey = "criteria:${kind.name}",
                fromUri = Uri.parse(contentUri),
                isPhoto = isPhoto,
            )
            updateGroup(kind) { group ->
                group.copy(
                    attachments = group.attachments + SsqAttachmentUiState(
                        id = stored?.id ?: "att-${UUID.randomUUID()}",
                        title = stored?.title ?: filenameFromContentUri(contentUri),
                        sizeLabel = stored?.sizeLabel ?: "Local",
                        isPhoto = isPhoto,
                        contentUri = stored?.contentUri ?: contentUri,
                    ),
                )
            }
        }
    }

    fun onRemoveAttachment(kind: CriteriaGroupKind, attachmentId: String) {
        viewModelScope.launch {
            removeAttachment(attachmentId)
            updateGroup(kind) { group ->
                group.copy(attachments = group.attachments.filterNot { it.id == attachmentId })
            }
        }
    }

    private fun filenameFromContentUri(contentUri: String): String {
        val segment = Uri.parse(contentUri).lastPathSegment.orEmpty()
        return segment.substringAfterLast('/').takeIf { it.isNotBlank() } ?: "attachment.jpg"
    }

    fun onSaveChecklistDraft() {
        val success = _uiState.value as? CriteriaChecklistUiState.Success ?: return
        val data = success.data.copy(showSavedDialog = true)
        _uiState.value = CriteriaChecklistUiState.Success(data)
        viewModelScope.launch {
            saveCriteriaDraft(
                institutionId = institutionId,
                payloadJson = data.copy(showSavedDialog = false).toPayloadJson(),
                evaluatedCount = data.evaluatedCount,
                totalCriteria = data.totalCriteria,
            )
        }
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
