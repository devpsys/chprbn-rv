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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun SectionAFormFields(
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
    onAddTutor: (PhcTutorUiState) -> Unit,
) {
    var showAddTutorDialog by remember { mutableStateOf(false) }
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.accreditation_section_a_tutors_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = stringResource(
                    R.string.accreditation_section_a_tutors_count_format,
                    form.tutors.size,
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    form.tutors.forEach { tutor ->
        TutorRosterCard(tutor = tutor)
    }

    OutlinedButton(
        onClick = { showAddTutorDialog = true },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        border = AccrdCardBorder,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        ),
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, tint = PrimaryGreen)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.accreditation_section_a_add_tutor),
            color = PrimaryGreen,
            fontWeight = FontWeight.SemiBold,
        )
    }

    if (showAddTutorDialog) {
        AddPhcTutorBottomSheet(
            onDismiss = { showAddTutorDialog = false },
            onConfirm = { tutor ->
                onAddTutor(tutor)
                showAddTutorDialog = false
            },
        )
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
private fun TutorRosterCard(tutor: PhcTutorUiState) {
    val isFullTime = tutor.employmentStatus.equals("Full-Time", ignoreCase = true)
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tutor.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (tutor.roleTitle.isNotBlank()) {
                        Text(
                            text = tutor.roleTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isFullTime) {
                        PrimaryGreen.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ) {
                    Text(
                        text = tutor.employmentStatus.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isFullTime) {
                            PrimaryGreen
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TutorMetaCell(
                            label = stringResource(R.string.accreditation_section_a_tutor_license_label),
                            value = tutor.licenseNo,
                            modifier = Modifier.weight(1f),
                        )
                        TutorMetaCell(
                            label = stringResource(R.string.accreditation_section_a_tutor_sex_label),
                            value = tutor.sex,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TutorMetaCell(
                            label = stringResource(R.string.accreditation_section_a_tutor_qualification_label),
                            value = tutor.qualification,
                            modifier = Modifier.weight(1f),
                        )
                        TutorMetaCell(
                            label = stringResource(R.string.accreditation_section_a_tutor_phone_label),
                            value = tutor.phone,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorMetaCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Soft fills work on dark surfaces; light mode needs opaque fills + outline. */
@Composable
private fun isLightThemeSurface(): Boolean =
    MaterialTheme.colorScheme.surface.luminance() > 0.5f

@Composable
private fun sheetFieldFillColor(): Color {
    val scheme = MaterialTheme.colorScheme
    return if (isLightThemeSurface()) {
        scheme.surfaceVariant
    } else {
        scheme.surfaceVariant.copy(alpha = 0.55f)
    }
}

@Composable
private fun sheetFieldUnfocusedBorder(): Color {
    val scheme = MaterialTheme.colorScheme
    return if (isLightThemeSurface()) {
        scheme.outline
    } else {
        Color.Transparent
    }
}

@Composable
private fun SheetFilledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val fill = sheetFieldFillColor()
    val unfocusedBorder = sheetFieldUnfocusedBorder()
    val placeholderColor = if (isLightThemeSurface()) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.outline
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(Locale.ENGLISH),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            singleLine = true,
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = placeholderColor,
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = fill,
                unfocusedContainerColor = fill,
                disabledContainerColor = fill.copy(alpha = 0.7f),
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = unfocusedBorder,
                cursorColor = PrimaryGreen,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun SheetSegmentedField(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    val light = isLightThemeSurface()
    val trackColor = if (light) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    }
    val trackBorder = if (light) AccrdNestedBorder else null

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(Locale.ENGLISH),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = trackColor,
            border = trackBorder,
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                options.forEach { option ->
                    val isSelected = selected == option
                    Surface(
                        onClick = { onSelected(option) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isSelected -> PrimaryGreen
                            light -> MaterialTheme.colorScheme.surface
                            else -> Color.Transparent
                        },
                        shadowElevation = if (isSelected || light) 1.dp else 0.dp,
                        border = if (!isSelected && light) AccrdNestedBorder else null,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) {
                                    Color.White
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPhcTutorBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (PhcTutorUiState) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var roleTitle by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf(SsqSectionFormUiState.sexOptions().first()) }
    var licenseNo by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var qualification by remember { mutableStateOf("") }
    var employmentStatus by remember {
        mutableStateOf(SsqSectionFormUiState.employmentStatusOptions().first())
    }
    val canSave = name.isNotBlank() && licenseNo.isNotBlank()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.45f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PrimaryGreen.copy(alpha = 0.12f),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(22.dp),
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.accreditation_section_a_add_tutor_dialog_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.accreditation_section_a_add_tutor_dialog_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(
                            R.string.accreditation_section_a_add_tutor_close_cd,
                        ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp),
                color = if (isLightThemeSurface()) {
                    MaterialTheme.colorScheme.outline
                } else {
                    PrimaryGreen.copy(alpha = 0.12f)
                },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SheetFilledField(
                    label = stringResource(R.string.accreditation_section_a_tutor_name),
                    value = name,
                    onValueChange = { name = it },
                    placeholder = stringResource(R.string.accreditation_section_a_tutor_name_hint),
                )
                SheetFilledField(
                    label = stringResource(R.string.accreditation_section_a_tutor_role),
                    value = roleTitle,
                    onValueChange = { roleTitle = it },
                    placeholder = stringResource(R.string.accreditation_section_a_tutor_role_hint),
                )
                SheetSegmentedField(
                    label = stringResource(R.string.accreditation_section_a_tutor_sex),
                    options = SsqSectionFormUiState.sexOptions(),
                    selected = sex,
                    onSelected = { sex = it },
                )
                SheetFilledField(
                    label = stringResource(R.string.accreditation_section_a_tutor_license),
                    value = licenseNo,
                    onValueChange = { licenseNo = it },
                    placeholder = stringResource(R.string.accreditation_section_a_tutor_license_hint),
                )
                SheetFilledField(
                    label = stringResource(R.string.accreditation_section_a_tutor_phone),
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = stringResource(R.string.accreditation_section_a_tutor_phone_hint),
                    keyboardType = KeyboardType.Phone,
                )
                SheetFilledField(
                    label = stringResource(R.string.accreditation_section_a_tutor_qualification),
                    value = qualification,
                    onValueChange = { qualification = it },
                    placeholder = stringResource(R.string.accreditation_section_a_tutor_qualification_hint),
                )
                SheetSegmentedField(
                    label = stringResource(R.string.accreditation_section_a_tutor_employment),
                    options = SsqSectionFormUiState.employmentStatusOptions(),
                    selected = employmentStatus,
                    onSelected = { employmentStatus = it },
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            onConfirm(
                                PhcTutorUiState(
                                    id = "",
                                    name = name.trim(),
                                    sex = sex,
                                    licenseNo = licenseNo.trim(),
                                    phone = phone.trim(),
                                    qualification = qualification.trim(),
                                    employmentStatus = employmentStatus,
                                    roleTitle = roleTitle.trim(),
                                ),
                            )
                        },
                        enabled = canSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            contentColor = Color.White,
                            disabledContainerColor = PrimaryGreen.copy(alpha = 0.35f),
                            disabledContentColor = Color.White.copy(alpha = 0.8f),
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.accreditation_section_a_add_tutor_dialog_save),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = AccrdCardBorder,
                    ) {
                        Text(
                            text = stringResource(R.string.accreditation_section_a_add_tutor_dialog_cancel),
                            color = PrimaryGreen,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}
