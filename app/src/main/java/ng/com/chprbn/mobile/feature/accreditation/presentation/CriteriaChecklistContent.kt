package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.ErrorRed
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.WarningYellow
import ng.com.chprbn.mobile.core.designsystem.components.LoadingState
import ng.com.chprbn.mobile.core.designsystem.components.PrimaryButton
import ng.com.chprbn.mobile.core.designsystem.components.SuccessDialog

@Composable
fun CriteriaChecklistContent(
    modifier: Modifier = Modifier,
    uiState: CriteriaChecklistUiState,
    onBack: () -> Unit = {},
    onMetStatusChange: (CriteriaGroupKind, CriterionMetStatus) -> Unit = { _, _ -> },
    onCheckItemToggled: (CriteriaGroupKind, String) -> Unit = { _, _ -> },
    onEvidenceNotesChange: (CriteriaGroupKind, String) -> Unit = { _, _ -> },
    onHrCountChange: (CriteriaGroupKind, HrCountField, String) -> Unit = { _, _, _ -> },
    onCapitalAmountChange: (String) -> Unit = {},
    onStatementOfAccountConfirmed: (Boolean) -> Unit = {},
    onSalaryChange: (CriteriaGroupKind, SalaryField, String) -> Unit = { _, _, _ -> },
    onRemoveAttachment: (CriteriaGroupKind, String) -> Unit = { _, _ -> },
    onAddPhoto: (CriteriaGroupKind) -> Unit = {},
    onAddDocument: (CriteriaGroupKind) -> Unit = {},
    onSaveChecklistDraft: () -> Unit = {},
    onSaveAsDraft: () -> Unit = {},
    onContinueToScoreEntry: (String) -> Unit = {},
    onDismissSavedDialog: () -> Unit = {},
) {
    val success = uiState as? CriteriaChecklistUiState.Success

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AssessmentFlowTopBar(
                title = stringResource(R.string.accreditation_criteria_title),
                onBack = onBack,
            )
        },
        bottomBar = {
            if (success != null) {
                CriteriaChecklistBottomBar(
                    onSaveChecklistDraft = onSaveChecklistDraft,
                    onSaveAsDraft = onSaveAsDraft,
                    onContinueToScoreEntry = {
                        onContinueToScoreEntry(success.data.institutionId)
                    },
                )
            }
        },
    ) { paddingValues ->
        when (uiState) {
            CriteriaChecklistUiState.Loading -> LoadingState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )

            is CriteriaChecklistUiState.Error -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.accreditation_criteria_error_title),
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

            is CriteriaChecklistUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        CriteriaChecklistHeaderCard(data = uiState.data)
                    }
                    items(uiState.data.groups, key = { it.kind.name }) { group ->
                        CriteriaGroupCard(
                            group = group,
                            onMetStatusChange = onMetStatusChange,
                            onCheckItemToggled = onCheckItemToggled,
                            onEvidenceNotesChange = onEvidenceNotesChange,
                            onHrCountChange = onHrCountChange,
                            onCapitalAmountChange = onCapitalAmountChange,
                            onStatementOfAccountConfirmed = onStatementOfAccountConfirmed,
                            onSalaryChange = onSalaryChange,
                            onRemoveAttachment = onRemoveAttachment,
                            onAddPhoto = { onAddPhoto(group.kind) },
                            onAddDocument = { onAddDocument(group.kind) },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
                if (uiState.data.showSavedDialog) {
                    SuccessDialog(
                        title = stringResource(R.string.accreditation_criteria_saved_title),
                        message = stringResource(R.string.accreditation_criteria_saved_message),
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
private fun CriteriaChecklistHeaderCard(data: CriteriaChecklistData) {
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
                        text = stringResource(R.string.accreditation_criteria_section_label),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = data.institutionName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(
                            R.string.accreditation_criteria_facility_meta_format,
                            data.locationLabel,
                            data.cycleLabel,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Policy,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp),
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.accreditation_criteria_evaluated_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(
                                R.string.accreditation_criteria_evaluated_format,
                                data.evaluatedCount,
                                data.totalCriteria,
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { data.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = PrimaryGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = WarningYellow,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.accreditation_criteria_server_advisory),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CriteriaGroupCard(
    group: CriteriaGroupUiState,
    onMetStatusChange: (CriteriaGroupKind, CriterionMetStatus) -> Unit,
    onCheckItemToggled: (CriteriaGroupKind, String) -> Unit,
    onEvidenceNotesChange: (CriteriaGroupKind, String) -> Unit,
    onHrCountChange: (CriteriaGroupKind, HrCountField, String) -> Unit,
    onCapitalAmountChange: (String) -> Unit,
    onStatementOfAccountConfirmed: (Boolean) -> Unit,
    onSalaryChange: (CriteriaGroupKind, SalaryField, String) -> Unit,
    onRemoveAttachment: (CriteriaGroupKind, String) -> Unit,
    onAddPhoto: () -> Unit,
    onAddDocument: () -> Unit,
) {
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
                        text = criteriaSectionLabel(group.kind),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                    )
                    Text(
                        text = criteriaGroupTitle(group.kind),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                CriterionMetNotMetToggle(
                    metStatus = group.metStatus,
                    onMet = { onMetStatusChange(group.kind, CriterionMetStatus.Met) },
                    onNotMet = { onMetStatusChange(group.kind, CriterionMetStatus.NotMet) },
                )
            }
            when (group.kind) {
                CriteriaGroupKind.HumanResources -> {
                    group.hrCounts?.let { counts ->
                        HrCountsGrid(
                            counts = counts,
                            onCountChange = { field, value ->
                                onHrCountChange(group.kind, field, value)
                            },
                        )
                    }
                }
                CriteriaGroupKind.PhysicalFacilities -> {
                    PhysicalFacilitiesChips(chips = group.facilitySummaryChips)
                    SectionECrossReferenceRow()
                }
                CriteriaGroupKind.CapitalBase -> {
                    CapitalBaseFields(
                        amount = group.capitalAmountNaira,
                        statementConfirmed = group.statementOfAccountConfirmed,
                        onAmountChange = onCapitalAmountChange,
                        onStatementConfirmed = onStatementOfAccountConfirmed,
                    )
                }
                CriteriaGroupKind.StaffWelfare -> {
                    StaffWelfareSalaryFields(
                        hodSalary = group.hodSalaryNaira,
                        tutorWithDegree = group.phcTutorWithDegreeSalaryNaira,
                        tutorWithoutDegree = group.phcTutorWithoutDegreeSalaryNaira,
                        onSalaryChange = { field, value ->
                            onSalaryChange(group.kind, field, value)
                        },
                    )
                }
                else -> Unit
            }
            group.checkItems.forEach { item ->
                val label = criteriaCheckItemLabel(group.kind, item.id)
                if (label.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Checkbox(
                            checked = item.checked,
                            onCheckedChange = { onCheckItemToggled(group.kind, item.id) },
                            colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen),
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                }
            }
            if (group.kind != CriteriaGroupKind.PhysicalFacilities ||
                group.evidenceNotes.isNotEmpty()
            ) {
                OutlinedTextField(
                    value = group.evidenceNotes,
                    onValueChange = { onEvidenceNotesChange(group.kind, it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(stringResource(R.string.accreditation_criteria_evidence_label))
                    },
                    minLines = 2,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        cursorColor = PrimaryGreen,
                    ),
                )
            }
            AttachmentManagementCard(
                attachments = group.attachments,
                onAddPhoto = onAddPhoto,
                onAddDocument = onAddDocument,
                onRemoveAttachment = { onRemoveAttachment(group.kind, it) },
                title = stringResource(R.string.accreditation_criteria_attachments_title),
                hint = stringResource(R.string.accreditation_criteria_attachments_hint),
                photoLabel = stringResource(R.string.accreditation_section_take_photo),
                documentLabel = stringResource(R.string.accreditation_section_add_document),
                mutedBackground = true,
            )
        }
    }
}

@Composable
private fun CriterionMetNotMetToggle(
    metStatus: CriterionMetStatus?,
    onMet: () -> Unit,
    onNotMet: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(modifier = Modifier.padding(2.dp)) {
            val metSelected = metStatus == CriterionMetStatus.Met
            val notMetSelected = metStatus == CriterionMetStatus.NotMet
            Surface(
                onClick = onMet,
                shape = RoundedCornerShape(6.dp),
                color = if (metSelected) PrimaryGreen else Color.Transparent,
            ) {
                Text(
                    text = stringResource(R.string.accreditation_criteria_met),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (metSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                onClick = onNotMet,
                shape = RoundedCornerShape(6.dp),
                color = if (notMetSelected) ErrorRed else Color.Transparent,
            ) {
                Text(
                    text = stringResource(R.string.accreditation_criteria_not_met),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (notMetSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HrCountsGrid(
    counts: CriteriaHrCountUiState,
    onCountChange: (HrCountField, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HrCountCell(
                label = stringResource(R.string.accreditation_criteria_hr_ft_phc),
                value = counts.fullTimePhcTutors,
                requirement = stringResource(R.string.accreditation_criteria_hr_ft_phc_req),
                onValueChange = { onCountChange(HrCountField.FullTimePhc, it) },
                modifier = Modifier.weight(1f),
            )
            HrCountCell(
                label = stringResource(R.string.accreditation_criteria_hr_pt_phc),
                value = counts.partTimePhcTutors,
                requirement = stringResource(R.string.accreditation_criteria_hr_pt_phc_req),
                onValueChange = { onCountChange(HrCountField.PartTimePhc, it) },
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HrCountCell(
                label = stringResource(R.string.accreditation_criteria_hr_ft_lecturers),
                value = counts.fullTimeLecturers,
                requirement = stringResource(R.string.accreditation_criteria_hr_ft_lecturers_req),
                onValueChange = { onCountChange(HrCountField.FullTimeLecturers, it) },
                modifier = Modifier.weight(1f),
            )
            HrCountCell(
                label = stringResource(R.string.accreditation_criteria_hr_admin),
                value = counts.adminSupport,
                requirement = stringResource(R.string.accreditation_criteria_hr_admin_req),
                onValueChange = { onCountChange(HrCountField.AdminSupport, it) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HrCountCell(
    label: String,
    value: String,
    requirement: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
            )
            Text(
                text = requirement,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            CompactNumberField(
                value = value,
                onValueChange = onValueChange,
                emphasize = true,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhysicalFacilitiesChips(chips: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        chips.forEach { chip ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = chip,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun SectionECrossReferenceRow() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Filled.Layers,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.accreditation_criteria_section_e_link),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreen,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun CapitalBaseFields(
    amount: String,
    statementConfirmed: Boolean,
    onAmountChange: (String) -> Unit,
    onStatementConfirmed: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.accreditation_criteria_capital_minimum_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { onAmountChange(it.filter { ch -> ch.isDigit() }) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.accreditation_criteria_capital_amount_label)) },
            prefix = { Text("₦") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = CurrencyAmountVisualTransformation,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                cursorColor = PrimaryGreen,
            ),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = statementConfirmed,
                onCheckedChange = onStatementConfirmed,
                colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen),
            )
            Text(
                text = stringResource(R.string.accreditation_criteria_capital_statement),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun StaffWelfareSalaryFields(
    hodSalary: String,
    tutorWithDegree: String,
    tutorWithoutDegree: String,
    onSalaryChange: (SalaryField, String) -> Unit,
) {
    SalaryRow(
        title = stringResource(R.string.accreditation_criteria_welfare_hod),
        minimum = stringResource(R.string.accreditation_criteria_welfare_hod_min),
        value = hodSalary,
        onValueChange = { onSalaryChange(SalaryField.Hod, it) },
    )
    SalaryRow(
        title = stringResource(R.string.accreditation_criteria_welfare_tutor_degree),
        minimum = stringResource(R.string.accreditation_criteria_welfare_tutor_degree_min),
        value = tutorWithDegree,
        onValueChange = { onSalaryChange(SalaryField.PhcTutorWithDegree, it) },
    )
    SalaryRow(
        title = stringResource(R.string.accreditation_criteria_welfare_tutor_no_degree),
        minimum = stringResource(R.string.accreditation_criteria_welfare_tutor_no_degree_min),
        value = tutorWithoutDegree,
        onValueChange = { onSalaryChange(SalaryField.PhcTutorWithoutDegree, it) },
    )
}

@Composable
private fun SalaryRow(
    title: String,
    minimum: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = minimum,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = value,
                onValueChange = { onValueChange(it.filter { ch -> ch.isDigit() }) },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("₦") },
                suffix = { Text(stringResource(R.string.accreditation_criteria_salary_per_month)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = CurrencyAmountVisualTransformation,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    cursorColor = PrimaryGreen,
                ),
            )
        }
    }
}

@Composable
private fun CriteriaChecklistBottomBar(
    onSaveChecklistDraft: () -> Unit,
    onSaveAsDraft: () -> Unit,
    onContinueToScoreEntry: () -> Unit,
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
            OutlinedButton(
                onClick = onContinueToScoreEntry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.accreditation_criteria_continue_score_entry))
            }
            Button(
                onClick = onSaveChecklistDraft,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.accreditation_criteria_save_checklist),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OutlinedButton(
                onClick = onSaveAsDraft,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.accreditation_section_save_as_draft),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun criteriaSectionLabel(kind: CriteriaGroupKind): String = when (kind) {
    CriteriaGroupKind.Location -> stringResource(R.string.accreditation_criteria_section_1)
    CriteriaGroupKind.HeadOfDepartment -> stringResource(R.string.accreditation_criteria_section_2)
    CriteriaGroupKind.HumanResources -> stringResource(R.string.accreditation_criteria_section_3)
    CriteriaGroupKind.ExistingDepartments -> stringResource(R.string.accreditation_criteria_section_4)
    CriteriaGroupKind.PracticeArea -> stringResource(R.string.accreditation_criteria_section_5)
    CriteriaGroupKind.PhysicalFacilities -> stringResource(R.string.accreditation_criteria_section_6)
    CriteriaGroupKind.CapitalBase -> stringResource(R.string.accreditation_criteria_section_7)
    CriteriaGroupKind.StaffWelfare -> stringResource(R.string.accreditation_criteria_section_8)
}

@Composable
private fun criteriaGroupTitle(kind: CriteriaGroupKind): String = when (kind) {
    CriteriaGroupKind.Location -> stringResource(R.string.accreditation_criteria_group_location)
    CriteriaGroupKind.HeadOfDepartment -> stringResource(R.string.accreditation_criteria_group_hod)
    CriteriaGroupKind.HumanResources -> stringResource(R.string.accreditation_criteria_group_hr)
    CriteriaGroupKind.ExistingDepartments -> stringResource(R.string.accreditation_criteria_group_departments)
    CriteriaGroupKind.PracticeArea -> stringResource(R.string.accreditation_criteria_group_practice)
    CriteriaGroupKind.PhysicalFacilities -> stringResource(R.string.accreditation_criteria_group_facilities)
    CriteriaGroupKind.CapitalBase -> stringResource(R.string.accreditation_criteria_group_capital)
    CriteriaGroupKind.StaffWelfare -> stringResource(R.string.accreditation_criteria_group_welfare)
}

@Composable
private fun criteriaCheckItemLabel(kind: CriteriaGroupKind, itemId: String): String = when (kind) {
    CriteriaGroupKind.Location -> when (itemId) {
        "loc_rural" -> stringResource(R.string.accreditation_criteria_loc_rural)
        "loc_phc" -> stringResource(R.string.accreditation_criteria_loc_phc)
        "loc_hectares" -> stringResource(R.string.accreditation_criteria_loc_hectares)
        "loc_ownership" -> stringResource(R.string.accreditation_criteria_loc_ownership)
        else -> ""
    }
    CriteriaGroupKind.HeadOfDepartment -> when (itemId) {
        "hod_trained" -> stringResource(R.string.accreditation_criteria_hod_trained)
        "hod_postgrad" -> stringResource(R.string.accreditation_criteria_hod_postgrad)
        else -> ""
    }
    CriteriaGroupKind.HumanResources -> when (itemId) {
        "hr_visiting" -> stringResource(R.string.accreditation_criteria_hr_visiting)
        "hr_admin" -> stringResource(R.string.accreditation_criteria_hr_admin_cadre)
        else -> ""
    }
    CriteriaGroupKind.ExistingDepartments -> when (itemId) {
        "dept_two_programmes" -> stringResource(R.string.accreditation_criteria_dept_programmes)
        else -> ""
    }
    CriteriaGroupKind.PracticeArea -> when (itemId) {
        "prac_facilities" -> stringResource(R.string.accreditation_criteria_prac_facilities)
        "prac_mou" -> stringResource(R.string.accreditation_criteria_prac_mou)
        "prac_coordinator" -> stringResource(R.string.accreditation_criteria_prac_coordinator)
        else -> ""
    }
    CriteriaGroupKind.CapitalBase -> when (itemId) {
        "cap_statement" -> stringResource(R.string.accreditation_criteria_capital_statement)
        else -> ""
    }
    CriteriaGroupKind.StaffWelfare -> when (itemId) {
        "welfare_schedules" -> stringResource(R.string.accreditation_criteria_welfare_schedules)
        else -> ""
    }
    CriteriaGroupKind.PhysicalFacilities -> ""
}

@Preview(showBackground = true)
@Composable
private fun CriteriaChecklistContentPreview() {
    ChprbnTheme {
        CriteriaChecklistContent(uiState = CriteriaChecklistUiState.placeholder())
    }
}
