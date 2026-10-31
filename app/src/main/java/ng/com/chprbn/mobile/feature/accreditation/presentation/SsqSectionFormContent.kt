package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssuredWorkload
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.ErrorRed
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.SuccessGreen
import ng.com.chprbn.mobile.core.designsystem.components.LoadingState
import ng.com.chprbn.mobile.core.designsystem.components.PrimaryButton
import ng.com.chprbn.mobile.core.designsystem.components.SuccessDialog

@Composable
fun SsqSectionFormContent(
    modifier: Modifier = Modifier,
    uiState: SsqSectionFormUiState,
    onBack: () -> Unit = {},
    onOwnershipSelected: (OwnershipOption) -> Unit = {},
    onCampusLocationChange: (String) -> Unit = {},
    onDateOfEstablishmentChange: (String) -> Unit = {},
    onYearProgrammeCommencedChange: (String) -> Unit = {},
    onFacultyChange: (String) -> Unit = {},
    onDepartmentChange: (String) -> Unit = {},
    onProgrammeToggled: (String) -> Unit = {},
    onAccreditationStatusChange: (String) -> Unit = {},
    onDateOfLastVisitChange: (String) -> Unit = {},
    onExpiryDateChange: (String) -> Unit = {},
    onNbteNdChange: (Boolean) -> Unit = {},
    onNbteHndChange: (Boolean) -> Unit = {},
    onApprovedStudentChange: (level: String, value: String) -> Unit = { _, _ -> },
    onIndexedStudentChange: (key: String, value: String) -> Unit = { _, _ -> },
    onUnindexedStudentChange: (key: String, value: String) -> Unit = { _, _ -> },
    onEnrolledNdChange: (level: String, value: String) -> Unit = { _, _ -> },
    onEnrolledHndChange: (level: String, value: String) -> Unit = { _, _ -> },
    onAcademicStaffChange: (String) -> Unit = {},
    onTechnicalStaffChange: (String) -> Unit = {},
    onAdminStaffChange: (String) -> Unit = {},
    onAddTutor: (PhcTutorUiState) -> Unit = {},
    onAcademicStaffingRowChange: (StaffingRowUiState) -> Unit = {},
    onSupportStaffingRowChange: (StaffingRowUiState) -> Unit = {},
    onChecklistAnswer: (itemId: String, answer: ChecklistAnswer) -> Unit = { _, _ -> },
    onChecklistRemarks: (itemId: String, remarks: String) -> Unit = { _, _ -> },
    onChecklistMouStatus: (itemId: String, mouStatus: String) -> Unit = { _, _ -> },
    onRemoveAttachment: (attachmentId: String) -> Unit = {},
    onAddAttachment: (contentUri: String, isPhoto: Boolean, subsectionId: String?) -> Unit =
        { _, _, _ -> },
    onSaveSectionDraft: () -> Unit = {},
    onSaveAsDraft: () -> Unit = {},
    onDismissSavedDialog: () -> Unit = {},
) {
    val success = uiState as? SsqSectionFormUiState.Success

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AssessmentFlowTopBar(
                title = stringResource(R.string.accreditation_overview_title),
                onBack = onBack,
            )
        },
        bottomBar = {
            if (success != null) {
                SectionFormBottomBar(
                    onSaveSectionDraft = onSaveSectionDraft,
                    onSaveAsDraft = onSaveAsDraft,
                )
            }
        },
    ) { paddingValues ->
        when (uiState) {
            SsqSectionFormUiState.Loading -> LoadingState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )

            is SsqSectionFormUiState.Error -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.accreditation_section_error_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ErrorRed,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                PrimaryButton(
                    text = stringResource(R.string.accreditation_profile_action_back_to_hub),
                    onClick = onBack,
                )
            }

            is SsqSectionFormUiState.Success -> {
                SectionFormBody(
                    modifier = Modifier.padding(paddingValues),
                    data = uiState.data,
                    onOwnershipSelected = onOwnershipSelected,
                    onCampusLocationChange = onCampusLocationChange,
                    onDateOfEstablishmentChange = onDateOfEstablishmentChange,
                    onYearProgrammeCommencedChange = onYearProgrammeCommencedChange,
                    onFacultyChange = onFacultyChange,
                    onDepartmentChange = onDepartmentChange,
                    onProgrammeToggled = onProgrammeToggled,
                    onAccreditationStatusChange = onAccreditationStatusChange,
                    onDateOfLastVisitChange = onDateOfLastVisitChange,
                    onExpiryDateChange = onExpiryDateChange,
                    onNbteNdChange = onNbteNdChange,
                    onNbteHndChange = onNbteHndChange,
                    onApprovedStudentChange = onApprovedStudentChange,
                    onIndexedStudentChange = onIndexedStudentChange,
                    onUnindexedStudentChange = onUnindexedStudentChange,
                    onEnrolledNdChange = onEnrolledNdChange,
                    onEnrolledHndChange = onEnrolledHndChange,
                    onAcademicStaffChange = onAcademicStaffChange,
                    onTechnicalStaffChange = onTechnicalStaffChange,
                    onAdminStaffChange = onAdminStaffChange,
                    onAddTutor = onAddTutor,
                    onAcademicStaffingRowChange = onAcademicStaffingRowChange,
                    onSupportStaffingRowChange = onSupportStaffingRowChange,
                    onChecklistAnswer = onChecklistAnswer,
                    onChecklistRemarks = onChecklistRemarks,
                    onChecklistMouStatus = onChecklistMouStatus,
                    onRemoveAttachment = onRemoveAttachment,
                    onAddAttachment = onAddAttachment,
                )
                if (uiState.data.showSavedDialog) {
                    SuccessDialog(
                        title = stringResource(R.string.accreditation_section_saved_title),
                        message = stringResource(R.string.accreditation_section_saved_message),
                        primaryButtonText = stringResource(R.string.accreditation_section_saved_ok),
                        onPrimary = onDismissSavedDialog,
                        onDismiss = onDismissSavedDialog,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionFormBottomBar(
    onSaveSectionDraft: () -> Unit,
    onSaveAsDraft: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onSaveSectionDraft,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.accreditation_section_save_draft),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            OutlinedButton(
                onClick = onSaveAsDraft,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = AccrdCardBorder,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.accreditation_section_save_as_draft),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreen,
                )
            }
        }
    }
}

@Composable
private fun SectionFormBody(
    data: SsqSectionFormData,
    onOwnershipSelected: (OwnershipOption) -> Unit,
    onCampusLocationChange: (String) -> Unit,
    onDateOfEstablishmentChange: (String) -> Unit,
    onYearProgrammeCommencedChange: (String) -> Unit,
    onFacultyChange: (String) -> Unit,
    onDepartmentChange: (String) -> Unit,
    onProgrammeToggled: (String) -> Unit,
    onAccreditationStatusChange: (String) -> Unit,
    onDateOfLastVisitChange: (String) -> Unit,
    onExpiryDateChange: (String) -> Unit,
    onNbteNdChange: (Boolean) -> Unit,
    onNbteHndChange: (Boolean) -> Unit,
    onApprovedStudentChange: (String, String) -> Unit,
    onIndexedStudentChange: (String, String) -> Unit,
    onUnindexedStudentChange: (String, String) -> Unit,
    onEnrolledNdChange: (String, String) -> Unit,
    onEnrolledHndChange: (String, String) -> Unit,
    onAcademicStaffChange: (String) -> Unit,
    onTechnicalStaffChange: (String) -> Unit,
    onAdminStaffChange: (String) -> Unit,
    onAddTutor: (PhcTutorUiState) -> Unit,
    onAcademicStaffingRowChange: (StaffingRowUiState) -> Unit,
    onSupportStaffingRowChange: (StaffingRowUiState) -> Unit,
    onChecklistAnswer: (String, ChecklistAnswer) -> Unit,
    onChecklistRemarks: (String, String) -> Unit,
    onChecklistMouStatus: (String, String) -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onAddAttachment: (contentUri: String, isPhoto: Boolean, subsectionId: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (data.sectionKey) {
            SsqSectionKey.A -> {
                SectionHeaderCard(data = data)
                data.sectionA?.let { form ->
                    SectionAFormFields(
                        form = form,
                        onOwnershipSelected = onOwnershipSelected,
                        onCampusLocationChange = onCampusLocationChange,
                        onDateOfEstablishmentChange = onDateOfEstablishmentChange,
                        onYearProgrammeCommencedChange = onYearProgrammeCommencedChange,
                        onFacultyChange = onFacultyChange,
                        onDepartmentChange = onDepartmentChange,
                        onProgrammeToggled = onProgrammeToggled,
                        onAccreditationStatusChange = onAccreditationStatusChange,
                        onDateOfLastVisitChange = onDateOfLastVisitChange,
                        onExpiryDateChange = onExpiryDateChange,
                        onNbteNdChange = onNbteNdChange,
                        onNbteHndChange = onNbteHndChange,
                        onApprovedStudentChange = onApprovedStudentChange,
                        onIndexedStudentChange = onIndexedStudentChange,
                        onUnindexedStudentChange = onUnindexedStudentChange,
                        onEnrolledNdChange = onEnrolledNdChange,
                        onEnrolledHndChange = onEnrolledHndChange,
                        onAcademicStaffChange = onAcademicStaffChange,
                        onTechnicalStaffChange = onTechnicalStaffChange,
                        onAdminStaffChange = onAdminStaffChange,
                        onAddTutor = onAddTutor,
                    )
                }
                AttachmentManagementCard(
                    attachments = data.attachments,
                    onAddAttachment = { uri, isPhoto ->
                        onAddAttachment(uri, isPhoto, null)
                    },
                    onRemoveAttachment = onRemoveAttachment,
                )
            }
            SsqSectionKey.B -> SectionBFormFields(
                data = data,
                scrollState = scrollState,
                onChecklistAnswer = onChecklistAnswer,
                onChecklistRemarks = onChecklistRemarks,
                onRemoveAttachment = onRemoveAttachment,
                onAddAttachment = { subsectionId, uri, isPhoto ->
                    onAddAttachment(uri, isPhoto, subsectionId)
                },
            )
            SsqSectionKey.C -> SectionCFormFields(
                data = data,
                onChecklistAnswer = onChecklistAnswer,
                onChecklistRemarks = onChecklistRemarks,
                onRemoveAttachment = onRemoveAttachment,
                onAddAttachment = { uri, isPhoto ->
                    onAddAttachment(uri, isPhoto, null)
                },
            )
            SsqSectionKey.D -> SectionDFormFields(
                data = data,
                onAcademicStaffingRowChange = onAcademicStaffingRowChange,
                onSupportStaffingRowChange = onSupportStaffingRowChange,
                onChecklistAnswer = onChecklistAnswer,
                onChecklistRemarks = onChecklistRemarks,
                onRemoveAttachment = onRemoveAttachment,
                onAddAttachment = { uri, isPhoto ->
                    onAddAttachment(uri, isPhoto, null)
                },
            )
            SsqSectionKey.E -> SectionEFormFields(
                data = data,
                onChecklistAnswer = onChecklistAnswer,
                onChecklistRemarks = onChecklistRemarks,
                onRemoveAttachment = onRemoveAttachment,
                onAddAttachment = { uri, isPhoto ->
                    onAddAttachment(uri, isPhoto, null)
                },
            )
            SsqSectionKey.F -> SectionFFormFields(
                data = data,
                onChecklistAnswer = onChecklistAnswer,
                onChecklistRemarks = onChecklistRemarks,
                onChecklistMouStatus = onChecklistMouStatus,
                onRemoveAttachment = onRemoveAttachment,
                onAddAttachment = { uri, isPhoto ->
                    onAddAttachment(uri, isPhoto, null)
                },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
@Preview(showBackground = true, widthDp = 411)
@Composable
private fun SsqSectionAFormPreview() {
    ChprbnTheme {
        SsqSectionFormContent(uiState = SsqSectionFormUiState.placeholder(sectionKey = SsqSectionKey.A))
    }
}

@Preview(showBackground = true, widthDp = 411, name = "Section B")
@Composable
private fun SsqSectionBFormPreview() {
    ChprbnTheme {
        SsqSectionFormContent(uiState = SsqSectionFormUiState.placeholder(sectionKey = SsqSectionKey.B))
    }
}

@Preview(showBackground = true, widthDp = 411, name = "Section C")
@Composable
private fun SsqSectionCFormPreview() {
    ChprbnTheme {
        SsqSectionFormContent(uiState = SsqSectionFormUiState.placeholder(sectionKey = SsqSectionKey.C))
    }
}

@Preview(showBackground = true, widthDp = 411, name = "Section D")
@Composable
private fun SsqSectionDFormPreview() {
    ChprbnTheme {
        SsqSectionFormContent(uiState = SsqSectionFormUiState.placeholder(sectionKey = SsqSectionKey.D))
    }
}

@Preview(showBackground = true, widthDp = 411, name = "Section E")
@Composable
private fun SsqSectionEFormPreview() {
    ChprbnTheme {
        SsqSectionFormContent(uiState = SsqSectionFormUiState.placeholder(sectionKey = SsqSectionKey.E))
    }
}

@Preview(showBackground = true, widthDp = 411, name = "Section F")
@Composable
private fun SsqSectionFFormPreview() {
    ChprbnTheme {
        SsqSectionFormContent(uiState = SsqSectionFormUiState.placeholder(sectionKey = SsqSectionKey.F))
    }
}
