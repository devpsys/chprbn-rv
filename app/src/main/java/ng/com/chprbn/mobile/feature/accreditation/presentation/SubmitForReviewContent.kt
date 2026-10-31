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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Grading
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.ErrorRed
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.SuccessGreen
import ng.com.chprbn.mobile.core.designsystem.WarningYellow
import ng.com.chprbn.mobile.core.designsystem.components.ErrorDialog
import ng.com.chprbn.mobile.core.designsystem.components.LoadingState
import ng.com.chprbn.mobile.core.designsystem.components.PrimaryButton
import ng.com.chprbn.mobile.core.designsystem.components.SyncingOverlay

@Composable
fun SubmitForReviewContent(
    modifier: Modifier = Modifier,
    uiState: SubmitForReviewUiState,
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {},
    onSubmitAssessment: () -> Unit = {},
    onRetrySubmit: () -> Unit = {},
    onDismissError: () -> Unit = {},
) {
    val success = uiState as? SubmitForReviewUiState.Success

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AssessmentFlowTopBar(
                    title = stringResource(R.string.accreditation_submit_title),
                    onBack = onBack,
                )
            },
            bottomBar = {
                if (success != null && !success.data.isSubmitting) {
                    SubmitForReviewBottomBar(
                        onSubmitAssessment = onSubmitAssessment,
                        onCancel = onCancel,
                    )
                }
            },
        ) { paddingValues ->
            when (uiState) {
                SubmitForReviewUiState.Loading -> LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )

                is SubmitForReviewUiState.Error -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.accreditation_submit_error_title),
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

                is SubmitForReviewUiState.Success -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                ) {
                    item { SubmitInstitutionCard(data = uiState.data) }
                    item { SubmitDossierReadinessCard(data = uiState.data) }
                    item { SubmitRawMarksCard(rows = uiState.data.rawMarks) }
                    item { SubmitConfirmationCard() }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }

        if (success?.data?.isSubmitting == true) {
            SyncingOverlay(
                title = stringResource(R.string.accreditation_submit_sync_title),
                subtitle = stringResource(R.string.accreditation_submit_sync_subtitle),
                encryptedLabel = stringResource(R.string.sync_loading_encrypted_badge),
            )
        }

        if (success?.data?.showErrorDialog == true) {
            ErrorDialog(
                title = stringResource(R.string.accreditation_submit_error_dialog_title),
                message = stringResource(R.string.accreditation_submit_error_dialog_message),
                primaryButtonText = stringResource(R.string.accreditation_submit_error_retry),
                secondaryButtonText = stringResource(R.string.accreditation_submit_error_dismiss),
                onPrimary = onRetrySubmit,
                onSecondary = onDismissError,
                onDismiss = onDismissError,
            )
        }
    }
}

@Composable
private fun SubmitInstitutionCard(data: SubmitForReviewData) {
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.accreditation_submit_audited_label),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                    )
                    Text(
                        text = data.institutionName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                ) {
                    Text(
                        text = data.locationLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.accreditation_submit_code_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = data.facilityCode,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun SubmitDossierReadinessCard(data: SubmitForReviewData) {
    val sectionsPct = if (data.sectionsTotal == 0) {
        0
    } else {
        (data.sectionsCompleted * 100) / data.sectionsTotal
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = SuccessGreen.copy(alpha = 0.18f),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(18.dp),
                        )
                    }
                    Text(
                        text = stringResource(R.string.accreditation_submit_dossier_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = SuccessGreen.copy(alpha = 0.18f),
                ) {
                    Text(
                        text = stringResource(
                            R.string.accreditation_submit_cycle_format,
                            data.cycleLabel,
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SubmitSummaryTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.AssignmentTurnedIn,
                    label = stringResource(R.string.accreditation_submit_sections_label),
                    value = stringResource(
                        R.string.accreditation_submit_sections_value_format,
                        data.sectionsCompleted,
                        data.sectionsTotal,
                    ),
                    hint = stringResource(
                        R.string.accreditation_submit_sections_pct_format,
                        sectionsPct,
                    ),
                )
                SubmitSummaryTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Gavel,
                    label = stringResource(R.string.accreditation_submit_criteria_label),
                    value = stringResource(
                        R.string.accreditation_submit_criteria_value_format,
                        data.criteriaEntered,
                        data.criteriaTotal,
                    ),
                    hint = stringResource(R.string.accreditation_submit_criteria_hint),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SubmitSummaryTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.AttachFile,
                    label = stringResource(R.string.accreditation_submit_attachments_label),
                    value = stringResource(
                        R.string.accreditation_submit_attachments_value_format,
                        data.attachmentCount,
                    ),
                    hint = null,
                )
                SubmitSummaryTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.FactCheck,
                    label = stringResource(R.string.accreditation_submit_marks_label),
                    value = stringResource(
                        R.string.accreditation_submit_marks_value_format,
                        data.scoreGroupsComplete,
                    ),
                    hint = null,
                    valueColor = SuccessGreen,
                )
            }
        }
    }
}

@Composable
private fun SubmitSummaryTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    hint: String?,
    valueColor: Color = PrimaryGreen,
) {
    Surface(
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = valueColor,
                    maxLines = 1,
                )
                if (hint != null) {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun SubmitRawMarksCard(rows: List<SubmitRawMarkRow>) {
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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Grading,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(18.dp),
                        )
                    }
                    Text(
                        text = stringResource(R.string.accreditation_submit_raw_marks_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = stringResource(R.string.accreditation_submit_raw_marks_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            rows.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    pair.forEach { row ->
                        SubmitRawMarkTile(
                            modifier = Modifier.weight(1f),
                            row = row,
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmitRawMarkTile(
    modifier: Modifier = Modifier,
    row: SubmitRawMarkRow,
) {
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
                text = row.groupLabel,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(
                    R.string.accreditation_submit_group_clause_format,
                    row.clauseLabel,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface,
                border = AccrdCardBorder,
            ) {
                Text(
                    text = stringResource(
                        R.string.accreditation_submit_score_pts_format,
                        row.rawScore,
                        row.maxPoints,
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreen,
                )
            }
        }
    }
}

@Composable
private fun SubmitConfirmationCard() {
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp),
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.accreditation_submit_confirm_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.accreditation_submit_confirm_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = WarningYellow,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.accreditation_submit_confirm_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = stringResource(R.string.accreditation_submit_no_signature),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun SubmitForReviewBottomBar(
    onSubmitAssessment: () -> Unit,
    onCancel: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
            ) {
                Text(
                    text = stringResource(R.string.accreditation_submit_cancel),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Button(
                onClick = onSubmitAssessment,
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
                Icon(
                    imageVector = Icons.Filled.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = stringResource(R.string.accreditation_submit_primary),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SubmitForReviewContentPreview() {
    ChprbnTheme {
        SubmitForReviewContent(uiState = SubmitForReviewUiState.placeholder())
    }
}
