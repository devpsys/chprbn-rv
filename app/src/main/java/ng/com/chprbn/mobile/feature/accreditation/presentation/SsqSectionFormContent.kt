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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CameraAlt
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.ErrorRed
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
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
    onNbteActiveChange: (Boolean) -> Unit = {},
    onAcademicStaffChange: (String) -> Unit = {},
    onTechnicalStaffChange: (String) -> Unit = {},
    onAdminStaffChange: (String) -> Unit = {},
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
                    onNbteActiveChange = onNbteActiveChange,
                    onAcademicStaffChange = onAcademicStaffChange,
                    onTechnicalStaffChange = onTechnicalStaffChange,
                    onAdminStaffChange = onAdminStaffChange,
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
    onNbteActiveChange: (Boolean) -> Unit,
    onAcademicStaffChange: (String) -> Unit,
    onTechnicalStaffChange: (String) -> Unit,
    onAdminStaffChange: (String) -> Unit,
    onChecklistAnswer: (String, ChecklistAnswer) -> Unit,
    onChecklistRemarks: (String, String) -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onAddPhoto: () -> Unit,
    onAddDocument: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeaderCard(data = data)
        when (data.sectionKey) {
            SsqSectionKey.A -> data.sectionA?.let { form ->
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
                    onNbteActiveChange = onNbteActiveChange,
                    onAcademicStaffChange = onAcademicStaffChange,
                    onTechnicalStaffChange = onTechnicalStaffChange,
                    onAdminStaffChange = onAdminStaffChange,
                )
            }
            else -> ChecklistItemsCard(
                items = data.checklistItems,
                onChecklistAnswer = onChecklistAnswer,
                onChecklistRemarks = onChecklistRemarks,
            )
        }
        AttachmentManagementCard(
            attachments = data.attachments,
            onAddPhoto = onAddPhoto,
            onAddDocument = onAddDocument,
            onRemoveAttachment = onRemoveAttachment,
        )
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
    onNbteActiveChange: (Boolean) -> Unit,
    onAcademicStaffChange: (String) -> Unit,
    onTechnicalStaffChange: (String) -> Unit,
    onAdminStaffChange: (String) -> Unit,
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNbteActiveChange(!form.nbteActive) },
        ) {
            Checkbox(
                checked = form.nbteActive,
                onCheckedChange = onNbteActiveChange,
                colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen),
            )
            Text(
                text = stringResource(R.string.accreditation_section_a_nbte),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }

    FormCard(title = stringResource(R.string.accreditation_section_a_staff_title)) {
        FormOutlinedField(
            label = stringResource(R.string.accreditation_profile_staff_academic),
            value = form.academicStaff,
            onValueChange = onAcademicStaffChange,
            keyboardType = KeyboardType.Number,
            digitsOnly = true,
        )
        FormOutlinedField(
            label = stringResource(R.string.accreditation_profile_staff_technical),
            value = form.technicalStaff,
            onValueChange = onTechnicalStaffChange,
            keyboardType = KeyboardType.Number,
            digitsOnly = true,
        )
        FormOutlinedField(
            label = stringResource(R.string.accreditation_profile_staff_admin),
            value = form.adminStaff,
            onValueChange = onAdminStaffChange,
            keyboardType = KeyboardType.Number,
            digitsOnly = true,
        )
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
            TutorRow(tutor = tutor)
        }
        OutlinedButton(
            onClick = { /* TODO: add tutor row */ },
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
private fun TutorRow(tutor: PhcTutorUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = tutor.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = tutor.roleTitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(
                R.string.accreditation_section_a_tutor_meta_format,
                tutor.employmentStatus,
                tutor.licenseNo,
                tutor.sex,
                tutor.qualification,
                tutor.phone,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
) {
    FormCard(
        title = stringResource(R.string.accreditation_section_attachments_title),
        trailing = {
            Text(
                text = stringResource(
                    R.string.accreditation_section_attachments_count_format,
                    attachments.size,
                ),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
            )
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.AttachFile,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(20.dp),
            )
        },
    ) {
        Text(
            text = stringResource(R.string.accreditation_section_attachments_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onAddPhoto,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp),
                shape = RoundedCornerShape(8.dp),
                border = AccrdCardBorder,
            ) {
                Icon(Icons.Outlined.CameraAlt, null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.accreditation_section_camera),
                    color = PrimaryGreen,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            OutlinedButton(
                onClick = onAddDocument,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp),
                shape = RoundedCornerShape(8.dp),
                border = AccrdCardBorder,
            ) {
                Icon(Icons.Outlined.UploadFile, null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.accreditation_section_gallery),
                    color = PrimaryGreen,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        attachments.forEach { item ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
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
                            contentDescription = stringResource(R.string.accreditation_section_remove_attachment_cd),
                            tint = ErrorRed,
                            modifier = Modifier.size(16.dp),
                        )
                    }
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

@Preview(showBackground = true, widthDp = 411, name = "Section C")
@Composable
private fun SsqSectionCFormPreview() {
    ChprbnTheme {
        SsqSectionFormContent(uiState = SsqSectionFormUiState.placeholder(sectionKey = SsqSectionKey.C))
    }
}
