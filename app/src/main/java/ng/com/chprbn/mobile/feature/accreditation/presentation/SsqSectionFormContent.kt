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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.UploadFile
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

private val AccrdCardBorder = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.15f))
private val AccrdNestedBorder = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.1f))

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
    onAddTutor: () -> Unit = {},
    onRemoveTutor: (tutorId: String) -> Unit = {},
    onTutorUpdated: (PhcTutorUiState) -> Unit = {},
    onChecklistAnswer: (itemId: String, answer: ChecklistAnswer) -> Unit = { _, _ -> },
    onChecklistRemarks: (itemId: String, remarks: String) -> Unit = { _, _ -> },
    onRemoveAttachment: (attachmentId: String) -> Unit = {},
    onAddPhoto: () -> Unit = {},
    onAddDocument: () -> Unit = {},
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
                    onRemoveTutor = onRemoveTutor,
                    onTutorUpdated = onTutorUpdated,
                    onChecklistAnswer = onChecklistAnswer,
                    onChecklistRemarks = onChecklistRemarks,
                    onRemoveAttachment = onRemoveAttachment,
                    onAddPhoto = onAddPhoto,
                    onAddDocument = onAddDocument,
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
    onAddTutor: () -> Unit,
    onRemoveTutor: (String) -> Unit,
    onTutorUpdated: (PhcTutorUiState) -> Unit,
    onChecklistAnswer: (String, ChecklistAnswer) -> Unit,
    onChecklistRemarks: (String, String) -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onAddPhoto: () -> Unit,
    onAddDocument: () -> Unit,
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
                        onRemoveTutor = onRemoveTutor,
                        onTutorUpdated = onTutorUpdated,
                    )
                }
                AttachmentManagementCard(
                    attachments = data.attachments,
                    onAddPhoto = onAddPhoto,
                    onAddDocument = onAddDocument,
                    onRemoveAttachment = onRemoveAttachment,
                )
            }
            SsqSectionKey.B -> SectionBFormFields(
                data = data,
                scrollState = scrollState,
                onChecklistAnswer = onChecklistAnswer,
                onChecklistRemarks = onChecklistRemarks,
                onRemoveAttachment = onRemoveAttachment,
                onAddPhoto = onAddPhoto,
                onAddDocument = onAddDocument,
            )
            else -> {
                SectionHeaderCard(data = data)
                ChecklistItemsCard(
                    items = data.checklistItems,
                    onChecklistAnswer = onChecklistAnswer,
                    onChecklistRemarks = onChecklistRemarks,
                )
                AttachmentManagementCard(
                    attachments = data.attachments,
                    onAddPhoto = onAddPhoto,
                    onAddDocument = onAddDocument,
                    onRemoveAttachment = onRemoveAttachment,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SectionHeaderCard(data: SsqSectionFormData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = AccrdCardBorder,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = data.formCodeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                SectionStatusChip(status = data.status)
            }
            Text(
                text = "Section ${data.sectionKey.key}: ${data.title}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
            )
            Text(
                text = data.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SectionAFormFields(
    form: SectionAFormUiState,
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
    onAddTutor: () -> Unit,
    onRemoveTutor: (String) -> Unit,
    onTutorUpdated: (PhcTutorUiState) -> Unit,
) {
    FormCard(title = stringResource(R.string.accreditation_section_a_ownership_title)) {
        Text(
            text = stringResource(R.string.accreditation_section_a_ownership_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OwnershipOption.entries.forEach { option ->
                val selected = form.ownership == option
                val label = stringResource(
                    when (option) {
                        OwnershipOption.Federal -> R.string.accreditation_section_a_ownership_federal
                        OwnershipOption.State -> R.string.accreditation_section_a_ownership_state
                        OwnershipOption.Lga -> R.string.accreditation_section_a_ownership_lga
                        OwnershipOption.Private -> R.string.accreditation_section_a_ownership_private
                        OwnershipOption.FaithBased -> R.string.accreditation_section_a_ownership_faith
                    },
                )
                ChoiceChip(
                    label = label,
                    selected = selected,
                    onClick = { onOwnershipSelected(option) },
                )
            }
        }
    }

    FormCard(title = stringResource(R.string.accreditation_section_a_campus_title)) {
        FormOutlinedField(
            label = stringResource(R.string.accreditation_section_a_campus_location),
            value = form.campusLocation,
            onValueChange = onCampusLocationChange,
        )
        FormDatePickerField(
            label = stringResource(R.string.accreditation_section_a_date_established),
            value = form.dateOfEstablishment,
            onDateSelected = onDateOfEstablishmentChange,
        )
        FormYearPickerField(
            label = stringResource(R.string.accreditation_section_a_year_commenced),
            value = form.yearProgrammeCommenced,
            onYearSelected = onYearProgrammeCommencedChange,
        )
        FormOutlinedField(
            label = stringResource(R.string.accreditation_section_a_faculty),
            value = form.faculty,
            onValueChange = onFacultyChange,
        )
        FormOutlinedField(
            label = stringResource(R.string.accreditation_section_a_department),
            value = form.department,
            onValueChange = onDepartmentChange,
        )
        Text(
            text = stringResource(R.string.accreditation_section_a_programmes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
        )
        SsqSectionFormUiState.programmeOptions().forEach { programme ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProgrammeToggled(programme) },
            ) {
                Checkbox(
                    checked = programme in form.programmesSelected,
                    onCheckedChange = { onProgrammeToggled(programme) },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen),
                )
                Text(
                    text = programme,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }

    FormCard(title = stringResource(R.string.accreditation_section_a_status_title)) {
        AccreditationStatusDropdown(
            value = form.accreditationStatus,
            onValueChange = onAccreditationStatusChange,
        )
        FormDatePickerField(
            label = stringResource(R.string.accreditation_section_a_last_visit),
            value = form.dateOfLastVisit,
            onDateSelected = onDateOfLastVisitChange,
        )
        FormDatePickerField(
            label = stringResource(R.string.accreditation_section_a_expiry),
            value = form.expiryDate,
            onDateSelected = onExpiryDateChange,
        )
    }

    StudentCountMatrixCard(
        title = stringResource(R.string.accreditation_section_a_approved_title),
        subtitle = stringResource(R.string.accreditation_section_a_approved_subtitle),
    ) {
        LevelCountGrid(
            levels = SsqA1StudentGrids.approvedBchsLevels,
            valueFor = { level -> form.approvedStudents[level].orEmpty() },
            onValueChange = onApprovedStudentChange,
        )
    }

    StudentCountMatrixCard(
        title = stringResource(R.string.accreditation_section_a_indexed_title),
        subtitle = stringResource(R.string.accreditation_section_a_indexed_subtitle),
    ) {
        ProgrammeLevelCountFields(
            values = form.indexedStudents,
            onValueChange = onIndexedStudentChange,
        )
    }

    StudentCountMatrixCard(
        title = stringResource(R.string.accreditation_section_a_unindexed_title),
        subtitle = stringResource(R.string.accreditation_section_a_unindexed_subtitle),
    ) {
        ProgrammeLevelCountFields(
            values = form.unindexedStudents,
            onValueChange = onUnindexedStudentChange,
        )
    }

    FormCard(title = stringResource(R.string.accreditation_section_a_nbte_title)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNbteNdChange(!form.nbteNd) },
        ) {
            Checkbox(
                checked = form.nbteNd,
                onCheckedChange = onNbteNdChange,
                colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen),
            )
            Text(
                text = stringResource(R.string.accreditation_section_a_nbte_nd),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNbteHndChange(!form.nbteHnd) },
        ) {
            Checkbox(
                checked = form.nbteHnd,
                onCheckedChange = onNbteHndChange,
                colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen),
            )
            Text(
                text = stringResource(R.string.accreditation_section_a_nbte_hnd),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }

    StudentCountMatrixCard(
        title = stringResource(R.string.accreditation_section_a_enrolled_title),
        subtitle = stringResource(R.string.accreditation_section_a_enrolled_subtitle),
    ) {
        ProgrammeGroupLabel(stringResource(R.string.accreditation_section_a_enrolled_nd))
        LevelCountGrid(
            levels = SsqA1StudentGrids.enrolledLevels,
            valueFor = { level -> form.enrolledNd[level].orEmpty() },
            onValueChange = onEnrolledNdChange,
        )
        Spacer(modifier = Modifier.height(8.dp))
        ProgrammeGroupLabel(stringResource(R.string.accreditation_section_a_enrolled_hnd))
        LevelCountGrid(
            levels = SsqA1StudentGrids.enrolledLevels,
            valueFor = { level -> form.enrolledHnd[level].orEmpty() },
            onValueChange = onEnrolledHndChange,
        )
    }

    FormCard(title = stringResource(R.string.accreditation_section_a_staff_title)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StaffCountTile(
                label = stringResource(R.string.accreditation_section_a_staff_academic),
                value = form.academicStaff,
                onValueChange = onAcademicStaffChange,
                modifier = Modifier.weight(1f),
            )
            StaffCountTile(
                label = stringResource(R.string.accreditation_section_a_staff_technical),
                value = form.technicalStaff,
                onValueChange = onTechnicalStaffChange,
                modifier = Modifier.weight(1f),
            )
            StaffCountTile(
                label = stringResource(R.string.accreditation_section_a_staff_admin),
                value = form.adminStaff,
                onValueChange = onAdminStaffChange,
                modifier = Modifier.weight(1f),
            )
        }
    }

    FormCard(
        title = stringResource(R.string.accreditation_section_a_tutors_title),
        trailing = {
            Text(
                text = stringResource(
                    R.string.accreditation_section_a_tutors_count_format,
                    form.tutors.size,
                ),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
            )
        },
    ) {
        form.tutors.forEachIndexed { index, tutor ->
            if (index > 0) {
                HorizontalDivider(
                    color = PrimaryGreen.copy(alpha = 0.05f),
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            TutorEditorCard(
                tutor = tutor,
                onTutorUpdated = onTutorUpdated,
                onRemoveTutor = onRemoveTutor,
            )
        }
        OutlinedButton(
            onClick = onAddTutor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = RoundedCornerShape(8.dp),
            border = AccrdCardBorder,
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = PrimaryGreen)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.accreditation_section_a_add_tutor),
                color = PrimaryGreen,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun StudentCountMatrixCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = AccrdCardBorder,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp),
                )
            }
            content()
        }
    }
}

@Composable
private fun ProgrammeLevelCountFields(
    values: Map<String, String>,
    onValueChange: (String, String) -> Unit,
) {
    SsqA1StudentGrids.indexedUnindexedGroups.forEachIndexed { index, (group, levels) ->
        if (index > 0) Spacer(modifier = Modifier.height(10.dp))
        ProgrammeGroupLabel(group)
        LevelCountGrid(
            levels = levels,
            valueFor = { level -> values[SsqA1StudentGrids.key(group, level)].orEmpty() },
            onValueChange = { level, value ->
                onValueChange(SsqA1StudentGrids.key(group, level), value)
            },
        )
    }
}

@Composable
private fun ProgrammeGroupLabel(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = PrimaryGreen.copy(alpha = 0.1f),
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen,
        )
    }
}

@Composable
private fun LevelCountGrid(
    levels: List<String>,
    valueFor: (String) -> String,
    onValueChange: (String, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        levels.chunked(2).forEach { rowLevels ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowLevels.forEach { level ->
                    LevelCountTile(
                        label = level,
                        value = valueFor(level),
                        onValueChange = { onValueChange(level, it) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowLevels.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LevelCountTile(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val countHint = stringResource(R.string.accreditation_section_a_count_hint)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = countHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp),
                )
                CompactNumberField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StaffCountTile(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            CompactNumberField(
                value = value,
                onValueChange = onValueChange,
                textAlign = TextAlign.Center,
                emphasize = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CompactNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.End,
    emphasize: Boolean = false,
) {
    BasicTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { ch -> ch.isDigit() }) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(PrimaryGreen),
        textStyle = TextStyle(
            color = if (emphasize) PrimaryGreen else MaterialTheme.colorScheme.onSurface,
            fontSize = if (emphasize) 22.sp else 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = textAlign,
            lineHeight = if (emphasize) 26.sp else 22.sp,
        ),
        modifier = modifier
            .widthIn(min = 64.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        decorationBox = { inner ->
            Box(
                contentAlignment = when (textAlign) {
                    TextAlign.Center -> Alignment.Center
                    TextAlign.Start -> Alignment.CenterStart
                    else -> Alignment.CenterEnd
                },
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "0",
                        fontSize = if (emphasize) 22.sp else 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = textAlign,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                inner()
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TutorEditorCard(
    tutor: PhcTutorUiState,
    onTutorUpdated: (PhcTutorUiState) -> Unit,
    onRemoveTutor: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = tutor.name.ifBlank { stringResource(R.string.accreditation_section_a_tutor_name) },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            IconButton(onClick = { onRemoveTutor(tutor.id) }) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.accreditation_section_a_remove_tutor_cd),
                    tint = ErrorRed,
                )
            }
        }
        FormOutlinedField(
            label = stringResource(R.string.accreditation_section_a_tutor_name),
            value = tutor.name,
            onValueChange = { onTutorUpdated(tutor.copy(name = it)) },
        )
        FormSimpleDropdown(
            label = stringResource(R.string.accreditation_section_a_tutor_sex),
            value = tutor.sex,
            options = SsqSectionFormUiState.sexOptions(),
            onValueChange = { onTutorUpdated(tutor.copy(sex = it)) },
        )
        FormOutlinedField(
            label = stringResource(R.string.accreditation_section_a_tutor_license),
            value = tutor.licenseNo,
            onValueChange = { onTutorUpdated(tutor.copy(licenseNo = it)) },
        )
        FormOutlinedField(
            label = stringResource(R.string.accreditation_section_a_tutor_phone),
            value = tutor.phone,
            onValueChange = { onTutorUpdated(tutor.copy(phone = it)) },
            keyboardType = KeyboardType.Phone,
        )
        FormOutlinedField(
            label = stringResource(R.string.accreditation_section_a_tutor_qualification),
            value = tutor.qualification,
            onValueChange = { onTutorUpdated(tutor.copy(qualification = it)) },
        )
        FormSimpleDropdown(
            label = stringResource(R.string.accreditation_section_a_tutor_employment),
            value = tutor.employmentStatus,
            options = SsqSectionFormUiState.employmentStatusOptions(),
            onValueChange = { onTutorUpdated(tutor.copy(employmentStatus = it)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormSimpleDropdown(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    cursorColor = PrimaryGreen,
                    focusedLabelColor = PrimaryGreen,
                ),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionBFormFields(
    data: SsqSectionFormData,
    scrollState: ScrollState,
    onChecklistAnswer: (String, ChecklistAnswer) -> Unit,
    onChecklistRemarks: (String, String) -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onAddPhoto: () -> Unit,
    onAddDocument: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val subsectionOffsets = remember { mutableMapOf<String, Int>() }
    var selectedTabId by remember {
        mutableStateOf(data.checklistSubsections.firstOrNull()?.id.orEmpty())
    }

    SectionBHeaderCard(data = data)

    if (data.checklistSubsections.isNotEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                data.checklistSubsections.forEach { subsection ->
                    val selected = selectedTabId == subsection.id
                    Surface(
                        onClick = {
                            selectedTabId = subsection.id
                            val offset = subsectionOffsets[subsection.id] ?: return@Surface
                            scope.launch { scrollState.animateScrollTo(offset) }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        color = if (selected) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            Color.Transparent
                        },
                        shadowElevation = if (selected) 1.dp else 0.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = if (subsection.id == "B1") {
                                    Icons.AutoMirrored.Filled.MenuBook
                                } else {
                                    Icons.Filled.AccountBalance
                                },
                                contentDescription = null,
                                tint = if (selected) {
                                    PrimaryGreen
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = subsection.tabLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) {
                                    PrimaryGreen
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }

    data.checklistSubsections.forEach { subsection ->
        val items = data.checklistItems.filter { it.subsectionId == subsection.id }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    subsectionOffsets[subsection.id] = coordinates.positionInParent().y.toInt()
                },
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SubsectionGroupHeader(
                badge = subsection.badge,
                title = subsection.title,
                subtitle = subsection.subtitle,
                itemCount = items.size,
            )
            items.forEach { item ->
                SectionBChecklistItemCard(
                    item = item,
                    onChecklistAnswer = onChecklistAnswer,
                    onChecklistRemarks = onChecklistRemarks,
                )
            }
            AttachmentManagementCard(
                attachments = subsection.attachments,
                title = subsection.attachmentsTitle,
                hint = subsection.attachmentsHint,
                countLabel = stringResource(
                    R.string.accreditation_section_attachments_files_format,
                    subsection.attachments.size,
                ),
                photoLabel = stringResource(R.string.accreditation_section_take_photo),
                documentLabel = stringResource(R.string.accreditation_section_add_document),
                onAddPhoto = onAddPhoto,
                onAddDocument = onAddDocument,
                onRemoveAttachment = onRemoveAttachment,
                mutedBackground = true,
            )
        }
    }
}

@Composable
private fun SectionBHeaderCard(data: SsqSectionFormData) {
    val evaluated = data.evaluatedItemCount
    val total = data.totalChecklistCount.coerceAtLeast(1)
    val progress = evaluated.toFloat() / total.toFloat()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = AccrdCardBorder,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Filled.AssuredWorkload,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = data.formCodeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                SectionStatusChip(status = data.status)
            }
            Text(
                text = stringResource(R.string.accreditation_section_b_heading),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = data.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.accreditation_section_completion_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(
                            R.string.accreditation_section_evaluated_format,
                            evaluated,
                            data.totalChecklistCount,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryGreen,
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = PrimaryGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
private fun SubsectionGroupHeader(
    badge: String,
    title: String,
    subtitle: String,
    itemCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = stringResource(
                    R.string.accreditation_section_items_count_format,
                    itemCount,
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionBChecklistItemCard(
    item: SsqChecklistItemUiState,
    onChecklistAnswer: (String, ChecklistAnswer) -> Unit,
    onChecklistRemarks: (String, String) -> Unit,
) {
    val (statusIcon, statusTint) = when (item.answer) {
        ChecklistAnswer.Yes, ChecklistAnswer.Available ->
            Icons.Filled.CheckCircle to SuccessGreen
        ChecklistAnswer.No, ChecklistAnswer.NotAvailable ->
            Icons.Filled.Cancel to ErrorRed
        ChecklistAnswer.Unanswered ->
            Icons.Outlined.RadioButtonUnchecked to MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = AccrdCardBorder,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Surface(
                        modifier = Modifier.size(20.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = item.numberInSubsection.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusTint,
                    modifier = Modifier.size(20.dp),
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (item.usesAvailability) {
                        ComplianceToggle(
                            label = stringResource(R.string.accreditation_section_answer_available),
                            selected = item.answer == ChecklistAnswer.Available,
                            selectedColor = PrimaryGreen,
                            icon = Icons.Filled.Check,
                            onClick = {
                                onChecklistAnswer(item.id, ChecklistAnswer.Available)
                            },
                            modifier = Modifier.weight(1f),
                        )
                        ComplianceToggle(
                            label = stringResource(R.string.accreditation_section_answer_not_available),
                            selected = item.answer == ChecklistAnswer.NotAvailable,
                            selectedColor = ErrorRed,
                            icon = Icons.Filled.Close,
                            onClick = {
                                onChecklistAnswer(item.id, ChecklistAnswer.NotAvailable)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        ComplianceToggle(
                            label = stringResource(R.string.accreditation_section_answer_compliant),
                            selected = item.answer == ChecklistAnswer.Yes,
                            selectedColor = PrimaryGreen,
                            icon = Icons.Filled.Check,
                            onClick = { onChecklistAnswer(item.id, ChecklistAnswer.Yes) },
                            modifier = Modifier.weight(1f),
                        )
                        ComplianceToggle(
                            label = stringResource(R.string.accreditation_section_answer_non_compliant),
                            selected = item.answer == ChecklistAnswer.No,
                            selectedColor = ErrorRed,
                            icon = Icons.Filled.Close,
                            onClick = { onChecklistAnswer(item.id, ChecklistAnswer.No) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.accreditation_section_remarks_field),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = item.remarks,
                    onValueChange = { onChecklistRemarks(item.id, it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.accreditation_section_remarks_placeholder),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    minLines = 2,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        cursorColor = PrimaryGreen,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        unfocusedBorderColor = Color.Transparent,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ComplianceToggle(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(6.dp),
        color = if (selected) selectedColor else Color.Transparent,
        shadowElevation = if (selected) 1.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ChecklistItemsCard(
    items: List<SsqChecklistItemUiState>,
    onChecklistAnswer: (String, ChecklistAnswer) -> Unit,
    onChecklistRemarks: (String, String) -> Unit,
) {
    FormCard(title = stringResource(R.string.accreditation_section_checklist_title)) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                HorizontalDivider(
                    color = PrimaryGreen.copy(alpha = 0.05f),
                    modifier = Modifier.padding(vertical = 10.dp),
                )
            }
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (item.usesAvailability) {
                    ChoiceChip(
                        label = stringResource(R.string.accreditation_section_answer_available),
                        selected = item.answer == ChecklistAnswer.Available,
                        onClick = { onChecklistAnswer(item.id, ChecklistAnswer.Available) },
                    )
                    ChoiceChip(
                        label = stringResource(R.string.accreditation_section_answer_not_available),
                        selected = item.answer == ChecklistAnswer.NotAvailable,
                        onClick = { onChecklistAnswer(item.id, ChecklistAnswer.NotAvailable) },
                    )
                } else {
                    ChoiceChip(
                        label = stringResource(R.string.accreditation_section_answer_yes),
                        selected = item.answer == ChecklistAnswer.Yes,
                        onClick = { onChecklistAnswer(item.id, ChecklistAnswer.Yes) },
                    )
                    ChoiceChip(
                        label = stringResource(R.string.accreditation_section_answer_no),
                        selected = item.answer == ChecklistAnswer.No,
                        onClick = { onChecklistAnswer(item.id, ChecklistAnswer.No) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            FormOutlinedField(
                label = stringResource(R.string.accreditation_section_remarks),
                value = item.remarks,
                onValueChange = { onChecklistRemarks(item.id, it) },
                singleLine = false,
            )
        }
    }
}

@Composable
private fun AttachmentManagementCard(
    attachments: List<SsqAttachmentUiState>,
    onAddPhoto: () -> Unit,
    onAddDocument: () -> Unit,
    onRemoveAttachment: (String) -> Unit,
    title: String = stringResource(R.string.accreditation_section_attachments_title),
    hint: String = stringResource(R.string.accreditation_section_attachments_hint),
    countLabel: String = stringResource(
        R.string.accreditation_section_attachments_count_format,
        attachments.size,
    ),
    photoLabel: String = stringResource(R.string.accreditation_section_camera),
    documentLabel: String = stringResource(R.string.accreditation_section_gallery),
    mutedBackground: Boolean = false,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (mutedBackground) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = if (mutedBackground) null else AccrdCardBorder,
        shadowElevation = if (mutedBackground) 0.dp else 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.UploadFile,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = countLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            attachments.forEach { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = AccrdNestedBorder,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (item.isPhoto) Icons.Filled.Image else Icons.Filled.Description,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = item.sizeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(
                            onClick = { onRemoveAttachment(item.id) },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(
                                    R.string.accreditation_section_remove_attachment_cd,
                                ),
                                tint = ErrorRed,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onAddPhoto,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = AccrdCardBorder,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Icon(
                        Icons.Outlined.CameraAlt,
                        null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = photoLabel,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                OutlinedButton(
                    onClick = onAddDocument,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = AccrdCardBorder,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Icon(
                        Icons.Outlined.AttachFile,
                        null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = documentLabel,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun FormCard(
    title: String,
    trailing: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = AccrdCardBorder,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    icon?.invoke()
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                    )
                }
                trailing?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) null else AccrdNestedBorder,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        )
    }
}

private val AccrdDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

@Composable
private fun FormOutlinedField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    digitsOnly: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(
                    if (digitsOnly) newValue.filter { it.isDigit() } else newValue,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (singleLine) Modifier.height(56.dp) else Modifier),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 2,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                cursorColor = PrimaryGreen,
                focusedLabelColor = PrimaryGreen,
            ),
        )
    }
}

/** Read-only date field that opens the stitch-styled calendar picker. */
@Composable
private fun FormDatePickerField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    FormPickerField(
        label = label,
        value = value,
        trailingContentDescription = stringResource(R.string.accreditation_section_pick_date_cd),
        onOpen = { showPicker = true },
    )

    if (showPicker) {
        AccrdCalendarDatePickerDialog(
            title = label.removeSuffix("*").trim()
                .ifBlank { stringResource(R.string.accreditation_section_date_picker_title) },
            initialDate = parseAccrdDate(value),
            yearOnly = false,
            onDismiss = { showPicker = false },
            onConfirm = { date ->
                onDateSelected(date.format(AccrdDateFormatter))
                showPicker = false
            },
        )
    }
}

/** Year-only field; opens the same picker on the year grid and stores `yyyy`. */
@Composable
private fun FormYearPickerField(
    label: String,
    value: String,
    onYearSelected: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    FormPickerField(
        label = label,
        value = value,
        trailingContentDescription = stringResource(R.string.accreditation_section_pick_year_cd),
        onOpen = { showPicker = true },
    )

    if (showPicker) {
        AccrdCalendarDatePickerDialog(
            title = label.removeSuffix("*").trim()
                .ifBlank { stringResource(R.string.accreditation_section_year_picker_title) },
            initialDate = parseAccrdYear(value),
            yearOnly = true,
            onDismiss = { showPicker = false },
            onConfirm = { date ->
                onYearSelected(date.year.toString())
                showPicker = false
            },
        )
    }
}

@Composable
private fun FormPickerField(
    label: String,
    value: String,
    trailingContentDescription: String,
    onOpen: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                onOpen()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            placeholder = {
                Text(
                    text = stringResource(R.string.accreditation_section_date_picker_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            interactionSource = interactionSource,
            trailingIcon = {
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryGreen.copy(alpha = 0.1f))
                        .clickable(onClick = onOpen),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = trailingContentDescription,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(18.dp),
                    )
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = PrimaryGreen.copy(alpha = 0.25f),
                cursorColor = PrimaryGreen,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
        )
    }
}

private fun parseAccrdDate(value: String): LocalDate? {
    if (value.isBlank()) return null
    return try {
        LocalDate.parse(value.trim(), AccrdDateFormatter)
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun parseAccrdYear(value: String): LocalDate? {
    if (value.isBlank()) return null
    val year = value.trim().toIntOrNull() ?: return null
    return try {
        LocalDate.of(year, 1, 1)
    } catch (_: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccreditationStatusDropdown(
    value: String,
    onValueChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
    ) {
        Text(
            text = stringResource(R.string.accreditation_section_a_current_status),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    cursorColor = PrimaryGreen,
                ),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                SsqSectionFormUiState.accreditationStatusOptions().forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        },
                    )
                }
            }
        }
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
