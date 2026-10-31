package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * SSQ section form (A–F). Presentation-only: camera/gallery are UI stubs
 * until Prompt H attachment wiring; save shows a local SuccessDialog.
 */
@Composable
fun SsqSectionFormScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: SsqSectionFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SsqSectionFormContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onOwnershipSelected = viewModel::onOwnershipSelected,
        onCampusLocationChange = viewModel::onCampusLocationChange,
        onDateOfEstablishmentChange = viewModel::onDateOfEstablishmentChange,
        onYearProgrammeCommencedChange = viewModel::onYearProgrammeCommencedChange,
        onFacultyChange = viewModel::onFacultyChange,
        onDepartmentChange = viewModel::onDepartmentChange,
        onProgrammeToggled = viewModel::onProgrammeToggled,
        onAccreditationStatusChange = viewModel::onAccreditationStatusChange,
        onDateOfLastVisitChange = viewModel::onDateOfLastVisitChange,
        onExpiryDateChange = viewModel::onExpiryDateChange,
        onNbteNdChange = viewModel::onNbteNdChange,
        onNbteHndChange = viewModel::onNbteHndChange,
        onApprovedStudentChange = viewModel::onApprovedStudentChange,
        onIndexedStudentChange = viewModel::onIndexedStudentChange,
        onUnindexedStudentChange = viewModel::onUnindexedStudentChange,
        onEnrolledNdChange = viewModel::onEnrolledNdChange,
        onEnrolledHndChange = viewModel::onEnrolledHndChange,
        onAcademicStaffChange = viewModel::onAcademicStaffChange,
        onTechnicalStaffChange = viewModel::onTechnicalStaffChange,
        onAdminStaffChange = viewModel::onAdminStaffChange,
        onAddTutor = viewModel::onAddTutor,
        onAcademicStaffingRowChange = viewModel::onAcademicStaffingRowChange,
        onSupportStaffingRowChange = viewModel::onSupportStaffingRowChange,
        onChecklistAnswer = viewModel::onChecklistAnswer,
        onChecklistRemarks = viewModel::onChecklistRemarks,
        onChecklistMouStatus = viewModel::onChecklistMouStatus,
        onRemoveAttachment = viewModel::onRemoveAttachment,
        onAddPhoto = { /* TODO: Prompt H camera capture */ },
        onAddDocument = { /* TODO: Prompt H gallery / PDF */ },
        onSaveSectionDraft = viewModel::onSaveSectionDraft,
        onSaveAsDraft = viewModel::onSaveAsDraft,
        onDismissSavedDialog = viewModel::dismissSavedDialog,
    )
}
