package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Presentation-only SSQ section form VM. Reads institutionId + sectionKey
 * from nav args; local edits stay in memory until a draft repository lands.
 */
@HiltViewModel
class SsqSectionFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val institutionId: String =
        savedStateHandle.get<String>("institutionId").orEmpty()
    private val sectionKey: SsqSectionKey =
        SsqSectionKey.fromKey(savedStateHandle.get<String>("sectionKey").orEmpty())

    private val _uiState = MutableStateFlow<SsqSectionFormUiState>(
        if (institutionId.isBlank()) {
            SsqSectionFormUiState.Error("Missing institution")
        } else {
            SsqSectionFormUiState.placeholder(institutionId, sectionKey)
        },
    )
    val uiState: StateFlow<SsqSectionFormUiState> = _uiState.asStateFlow()

    fun onOwnershipSelected(option: OwnershipOption) = updateSectionA { it.copy(ownership = option) }

    fun onCampusLocationChange(value: String) = updateSectionA { it.copy(campusLocation = value) }

    fun onDateOfEstablishmentChange(value: String) =
        updateSectionA { it.copy(dateOfEstablishment = value) }

    fun onYearProgrammeCommencedChange(value: String) =
        updateSectionA { it.copy(yearProgrammeCommenced = value) }

    fun onFacultyChange(value: String) = updateSectionA { it.copy(faculty = value) }

    fun onDepartmentChange(value: String) = updateSectionA { it.copy(department = value) }

    fun onProgrammeToggled(programme: String) = updateSectionA { form ->
        val next = form.programmesSelected.toMutableSet()
        if (!next.add(programme)) next.remove(programme)
        form.copy(programmesSelected = next)
    }

    fun onAccreditationStatusChange(value: String) =
        updateSectionA { it.copy(accreditationStatus = value) }

    fun onDateOfLastVisitChange(value: String) =
        updateSectionA { it.copy(dateOfLastVisit = value) }

    fun onExpiryDateChange(value: String) = updateSectionA { it.copy(expiryDate = value) }

    fun onNbteActiveChange(value: Boolean) = updateSectionA { it.copy(nbteActive = value) }

    fun onAcademicStaffChange(value: String) =
        updateSectionA { it.copy(academicStaff = value.filter { ch -> ch.isDigit() }) }

    fun onTechnicalStaffChange(value: String) =
        updateSectionA { it.copy(technicalStaff = value.filter { ch -> ch.isDigit() }) }

    fun onAdminStaffChange(value: String) =
        updateSectionA { it.copy(adminStaff = value.filter { ch -> ch.isDigit() }) }

    fun onChecklistAnswer(itemId: String, answer: ChecklistAnswer) {
        updateSuccess { data ->
            data.copy(
                checklistItems = data.checklistItems.map { item ->
                    if (item.id == itemId) item.copy(answer = answer) else item
                },
                status = SectionDraftStatus.InProgress,
            )
        }
    }

    fun onChecklistRemarks(itemId: String, remarks: String) {
        updateSuccess { data ->
            data.copy(
                checklistItems = data.checklistItems.map { item ->
                    if (item.id == itemId) item.copy(remarks = remarks) else item
                },
            )
        }
    }

    fun onRemoveAttachment(attachmentId: String) {
        updateSuccess { data ->
            data.copy(attachments = data.attachments.filterNot { it.id == attachmentId })
        }
    }

    fun onSaveSectionDraft() {
        updateSuccess { data ->
            data.copy(
                status = SectionDraftStatus.SavedLocally,
                showSavedDialog = true,
            )
        }
    }

    fun onSaveAsDraft() = onSaveSectionDraft()

    fun dismissSavedDialog() {
        updateSuccess { it.copy(showSavedDialog = false) }
    }

    private fun updateSectionA(transform: (SectionAFormUiState) -> SectionAFormUiState) {
        updateSuccess { data ->
            val sectionA = data.sectionA ?: return@updateSuccess data
            data.copy(
                sectionA = transform(sectionA),
                status = SectionDraftStatus.InProgress,
            )
        }
    }

    private fun updateSuccess(transform: (SsqSectionFormData) -> SsqSectionFormData) {
        _uiState.update { state ->
            val success = state as? SsqSectionFormUiState.Success ?: return@update state
            SsqSectionFormUiState.Success(transform(success.data))
        }
    }
}
