package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
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

    fun onNbteNdChange(value: Boolean) = updateSectionA { it.copy(nbteNd = value) }

    fun onNbteHndChange(value: Boolean) = updateSectionA { it.copy(nbteHnd = value) }

    fun onApprovedStudentChange(level: String, value: String) = updateSectionA { form ->
        form.copy(approvedStudents = form.approvedStudents + (level to digitsOnly(value)))
    }

    fun onIndexedStudentChange(key: String, value: String) = updateSectionA { form ->
        form.copy(indexedStudents = form.indexedStudents + (key to digitsOnly(value)))
    }

    fun onUnindexedStudentChange(key: String, value: String) = updateSectionA { form ->
        form.copy(unindexedStudents = form.unindexedStudents + (key to digitsOnly(value)))
    }

    fun onEnrolledNdChange(level: String, value: String) = updateSectionA { form ->
        form.copy(enrolledNd = form.enrolledNd + (level to digitsOnly(value)))
    }

    fun onEnrolledHndChange(level: String, value: String) = updateSectionA { form ->
        form.copy(enrolledHnd = form.enrolledHnd + (level to digitsOnly(value)))
    }

    fun onAcademicStaffChange(value: String) =
        updateSectionA { it.copy(academicStaff = digitsOnly(value)) }

    fun onTechnicalStaffChange(value: String) =
        updateSectionA { it.copy(technicalStaff = digitsOnly(value)) }

    fun onAdminStaffChange(value: String) =
        updateSectionA { it.copy(adminStaff = digitsOnly(value)) }

    fun onAddTutor(tutor: PhcTutorUiState) = updateSectionA { form ->
        form.copy(
            tutors = form.tutors + tutor.copy(
                id = tutor.id.ifBlank { "tutor-${UUID.randomUUID()}" },
            ),
        )
    }

    fun onRemoveTutor(tutorId: String) = updateSectionA { form ->
        form.copy(tutors = form.tutors.filterNot { it.id == tutorId })
    }

    fun onAcademicStaffingRowChange(row: StaffingRowUiState) {
        updateSectionD { section ->
            section.copy(
                academicRows = section.academicRows.map {
                    if (it.id == row.id) row.withDigitCounts() else it
                },
            )
        }
    }

    fun onSupportStaffingRowChange(row: StaffingRowUiState) {
        updateSectionD { section ->
            section.copy(
                supportRows = section.supportRows.map {
                    if (it.id == row.id) row.withDigitCounts() else it
                },
            )
        }
    }

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
            val inSubsection = data.checklistSubsections.any { subsection ->
                subsection.attachments.any { it.id == attachmentId }
            }
            if (inSubsection) {
                data.copy(
                    checklistSubsections = data.checklistSubsections.map { subsection ->
                        subsection.copy(
                            attachments = subsection.attachments.filterNot { it.id == attachmentId },
                        )
                    },
                )
            } else {
                data.copy(attachments = data.attachments.filterNot { it.id == attachmentId })
            }
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

    private fun digitsOnly(value: String): String = value.filter { it.isDigit() }

    private fun StaffingRowUiState.withDigitCounts(): StaffingRowUiState = copy(
        required = digitsOnly(required),
        available = digitsOnly(available),
        fullTime = digitsOnly(fullTime),
        partTime = digitsOnly(partTime),
    )

    private fun updateSectionA(transform: (SectionAFormUiState) -> SectionAFormUiState) {
        updateSuccess { data ->
            val sectionA = data.sectionA ?: return@updateSuccess data
            data.copy(
                sectionA = transform(sectionA),
                status = SectionDraftStatus.InProgress,
            )
        }
    }

    private fun updateSectionD(transform: (SectionDFormUiState) -> SectionDFormUiState) {
        updateSuccess { data ->
            val sectionD = data.sectionD ?: return@updateSuccess data
            data.copy(
                sectionD = transform(sectionD),
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
