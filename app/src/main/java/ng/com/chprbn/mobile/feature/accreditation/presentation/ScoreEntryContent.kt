package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import ng.com.chprbn.mobile.core.designsystem.components.LoadingState
import ng.com.chprbn.mobile.core.designsystem.components.PrimaryButton
import ng.com.chprbn.mobile.core.designsystem.components.SuccessDialog

@Composable
fun ScoreEntryContent(
    modifier: Modifier = Modifier,
    uiState: ScoreEntryUiState,
    onBack: () -> Unit = {},
    onRawScoreChange: (ScoreCriterionGroup, String) -> Unit = { _, _ -> },
    onObservationsChange: (ScoreCriterionGroup, String) -> Unit = { _, _ -> },
    onRemoveAttachment: (ScoreCriterionGroup, String) -> Unit = { _, _ -> },
    onAddPhoto: (ScoreCriterionGroup) -> Unit = {},
    onAddDocument: (ScoreCriterionGroup) -> Unit = {},
    onSaveDraft: () -> Unit = {},
    onSubmitForServerReview: () -> Unit = {},
    onDismissSavedDialog: () -> Unit = {},
) {
    val success = uiState as? ScoreEntryUiState.Success

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AssessmentFlowTopBar(
                    title = stringResource(R.string.accreditation_score_title),
                    onBack = onBack,
                )
            },
            bottomBar = {
                if (success != null) {
                    ScoreEntryBottomBar(
                        onSaveDraft = onSaveDraft,
                        onSubmitForServerReview = onSubmitForServerReview,
                    )
                }
            },
        ) { paddingValues ->
            when (uiState) {
                ScoreEntryUiState.Loading -> LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )

                is ScoreEntryUiState.Error -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.accreditation_score_error_title),
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

                is ScoreEntryUiState.Success -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item { ScoreEntryHeader(data = uiState.data) }
                    item { ScoreEntryProgressCard(data = uiState.data) }
                    items(uiState.data.rows, key = { it.group.name }) { row ->
                        ScoreEntryGroupCard(
                            row = row,
                            onRawScoreChange = onRawScoreChange,
                            onObservationsChange = onObservationsChange,
                            onRemoveAttachment = onRemoveAttachment,
                            onAddPhoto = { onAddPhoto(row.group) },
                            onAddDocument = { onAddDocument(row.group) },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }

        if (success?.data?.showSavedDialog == true) {
            SuccessDialog(
                title = stringResource(R.string.accreditation_score_saved_title),
                message = stringResource(R.string.accreditation_score_saved_message),
                primaryButtonText = stringResource(R.string.accreditation_section_saved_ok),
                onPrimary = onDismissSavedDialog,
                onDismiss = onDismissSavedDialog,
            )
        }
    }
}

@Composable
private fun ScoreEntryHeader(data: ScoreEntryData) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text(
                text = stringResource(R.string.accreditation_score_form_label),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
            )
            Text(
                text = stringResource(R.string.accreditation_score_heading),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = AccrdCardBorder,
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Apartment,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp),
                    )
                }
                Column {
                    Text(
                        text = data.institutionName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(
                            R.string.accreditation_score_facility_code_format,
                            data.facilityCode,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = data.locationLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Policy,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(22.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.accreditation_score_protocol_label),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                    )
                    Text(
                        text = stringResource(R.string.accreditation_score_protocol_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreEntryProgressCard(data: ScoreEntryData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = AccrdCardBorder,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.accreditation_score_progress_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        R.string.accreditation_score_completed_format,
                        data.completedCount,
                        data.totalGroups,
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
}

@Composable
private fun ScoreEntryGroupCard(
    row: ScoreEntryRowUiState,
    onRawScoreChange: (ScoreCriterionGroup, String) -> Unit,
    onObservationsChange: (ScoreCriterionGroup, String) -> Unit,
    onRemoveAttachment: (ScoreCriterionGroup, String) -> Unit,
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
                        text = scoreCriterionLabel(row.group),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                    )
                    Text(
                        text = scoreGroupTitle(row.group),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = stringResource(
                            R.string.accreditation_score_max_marks_format,
                            row.maxPoints,
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            OutlinedTextField(
                value = row.rawScore,
                onValueChange = { onRawScoreChange(row.group, it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.accreditation_score_raw_label)) },
                suffix = {
                    Text(
                        stringResource(
                            R.string.accreditation_score_points_suffix_format,
                            row.maxPoints,
                        ),
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    cursorColor = PrimaryGreen,
                ),
            )
            Text(
                text = stringResource(
                    R.string.accreditation_score_entry_hint_format,
                    row.maxPoints,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = row.observations,
                onValueChange = { onObservationsChange(row.group, it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.accreditation_score_observations_label)) },
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
            AttachmentManagementCard(
                attachments = row.attachments,
                onAddPhoto = onAddPhoto,
                onAddDocument = onAddDocument,
                onRemoveAttachment = { onRemoveAttachment(row.group, it) },
                title = stringResource(R.string.accreditation_score_attachments_title),
                hint = stringResource(R.string.accreditation_score_attachments_hint),
                photoLabel = stringResource(R.string.accreditation_section_camera),
                documentLabel = stringResource(R.string.accreditation_section_gallery),
                mutedBackground = true,
            )
        }
    }
}

@Composable
private fun ScoreEntryBottomBar(
    onSaveDraft: () -> Unit,
    onSubmitForServerReview: () -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onSaveDraft,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                ) {
                    Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = stringResource(R.string.accreditation_score_save_draft),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Button(
                    onClick = onSubmitForServerReview,
                    modifier = Modifier
                        .weight(1.35f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = stringResource(R.string.accreditation_score_submit),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = stringResource(R.string.accreditation_score_submit_footnote),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun scoreCriterionLabel(group: ScoreCriterionGroup): String = when (group) {
    ScoreCriterionGroup.Location -> stringResource(R.string.accreditation_score_criterion_01)
    ScoreCriterionGroup.HeadOfDepartment -> stringResource(R.string.accreditation_score_criterion_02)
    ScoreCriterionGroup.HumanResources -> stringResource(R.string.accreditation_score_criterion_03)
    ScoreCriterionGroup.PracticeArea -> stringResource(R.string.accreditation_score_criterion_05)
    ScoreCriterionGroup.PhysicalFacilities -> stringResource(R.string.accreditation_score_criterion_06)
    ScoreCriterionGroup.FinancialAndWelfare -> stringResource(R.string.accreditation_score_criterion_07_08)
}

@Composable
private fun scoreGroupTitle(group: ScoreCriterionGroup): String = when (group) {
    ScoreCriterionGroup.Location -> stringResource(R.string.accreditation_score_group_location)
    ScoreCriterionGroup.HeadOfDepartment -> stringResource(R.string.accreditation_score_group_hod)
    ScoreCriterionGroup.HumanResources -> stringResource(R.string.accreditation_score_group_hr)
    ScoreCriterionGroup.PracticeArea -> stringResource(R.string.accreditation_score_group_practice)
    ScoreCriterionGroup.PhysicalFacilities -> stringResource(R.string.accreditation_score_group_facilities)
    ScoreCriterionGroup.FinancialAndWelfare -> stringResource(R.string.accreditation_score_group_financial)
}

@Preview(showBackground = true)
@Composable
private fun ScoreEntryContentPreview() {
    ChprbnTheme {
        ScoreEntryContent(uiState = ScoreEntryUiState.placeholder())
    }
}
