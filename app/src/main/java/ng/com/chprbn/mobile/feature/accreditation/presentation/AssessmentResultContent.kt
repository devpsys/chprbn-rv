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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import ng.com.chprbn.mobile.core.designsystem.components.SuccessDialog

@Composable
fun AssessmentResultContent(
    modifier: Modifier = Modifier,
    uiState: AssessmentResultUiState,
    onBack: () -> Unit = {},
    onDone: () -> Unit = {},
    onViewCertificate: () -> Unit = {},
    onDismissCertificateDialog: () -> Unit = {},
    onRetry: () -> Unit = {},
    onDismissError: () -> Unit = {},
) {
    val success = uiState as? AssessmentResultUiState.Success

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AssessmentFlowTopBar(
                    title = stringResource(R.string.accreditation_result_title),
                    onBack = onBack,
                )
            },
            bottomBar = {
                if (success != null) {
                    AssessmentResultBottomBar(
                        onViewCertificate = onViewCertificate,
                        onDone = onDone,
                    )
                }
            },
        ) { paddingValues ->
            when (uiState) {
                AssessmentResultUiState.Loading -> LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )

                is AssessmentResultUiState.Error -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.accreditation_result_error_title),
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
                        onClick = onDone,
                    )
                }

                is AssessmentResultUiState.Success -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                ) {
                    item { ResultInstitutionCard(data = uiState.data) }
                    item { ResultServerDecisionCard(data = uiState.data) }
                    item { ResultRawScoresCard(rows = uiState.data.rawScoreRows) }
                    if (uiState.data.conditions.isNotEmpty()) {
                        item { ResultConditionsCard(conditions = uiState.data.conditions) }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }

        if (success?.data?.showCertificateDialog == true) {
            SuccessDialog(
                title = stringResource(R.string.accreditation_result_cert_dialog_title),
                message = stringResource(R.string.accreditation_result_cert_dialog_message),
                primaryButtonText = stringResource(R.string.accreditation_result_cert_dialog_ok),
                onPrimary = onDismissCertificateDialog,
                onDismiss = onDismissCertificateDialog,
            )
        }

        if (success?.data?.showErrorDialog == true) {
            ErrorDialog(
                title = stringResource(R.string.accreditation_result_error_dialog_title),
                message = stringResource(R.string.accreditation_result_error_dialog_message),
                primaryButtonText = stringResource(R.string.accreditation_result_error_retry),
                secondaryButtonText = stringResource(R.string.accreditation_result_error_dismiss),
                onPrimary = onRetry,
                onSecondary = onDismissError,
                onDismiss = onDismissError,
            )
        }
    }
}

@Composable
private fun ResultInstitutionCard(data: AssessmentResultData) {
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
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(26.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = SuccessGreen.copy(alpha = 0.18f),
                    ) {
                        Text(
                            text = stringResource(R.string.accreditation_result_verified),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.institutionName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = data.locationLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            ) {
                Row(modifier = Modifier.padding(vertical = 10.dp)) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.accreditation_result_code_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = data.facilityCode,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.accreditation_result_date_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = data.assessmentDateLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultServerDecisionCard(data: AssessmentResultData) {
    val decisionTint = gradeAccent(data.grade)
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(R.string.accreditation_result_server_section),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = stringResource(R.string.accreditation_result_computed_badge),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = decisionTint.copy(alpha = 0.12f),
                border = AccrdCardBorder,
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
                        Text(
                            text = stringResource(R.string.accreditation_result_cert_granted),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = decisionTint,
                        )
                        AccreditationGradeBadge(grade = data.grade)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = decisionTint,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WorkspacePremium,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(28.dp),
                            )
                        }
                        Column {
                            Text(
                                text = decisionLabel(data.decision),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = decisionTint,
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = decisionTint,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = stringResource(
                                        R.string.accreditation_result_tenure_format,
                                        data.accreditationPeriod,
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = decisionTint,
                                )
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ResultMetricTile(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.accreditation_result_overall_score_label),
                    hint = stringResource(R.string.accreditation_result_overall_score_hint),
                    value = data.overallScorePercentLabel,
                    emphasizeValue = true,
                )
                ResultMetricTile(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.accreditation_result_quota_label),
                    hint = stringResource(R.string.accreditation_result_quota_hint),
                    value = stringResource(
                        R.string.accreditation_result_quota_value_format,
                        data.studentQuotaLabel,
                    ),
                    subtitle = stringResource(R.string.accreditation_result_quota_per_session),
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.accreditation_result_receipt_label),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.accreditation_result_receipt_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = AccrdCardBorder,
                    ) {
                        Text(
                            text = data.receiptRef,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = PrimaryGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultMetricTile(
    modifier: Modifier = Modifier,
    title: String,
    hint: String,
    value: String,
    subtitle: String? = null,
    emphasizeValue: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = if (emphasizeValue) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.titleMedium
                },
                fontWeight = FontWeight.Bold,
                color = if (emphasizeValue) PrimaryGreen else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun AccreditationGradeBadge(grade: AccreditationServerGrade) {
    val (bg, fg, label) = when (grade) {
        AccreditationServerGrade.Excellent -> Triple(
            SuccessGreen,
            Color.White,
            stringResource(R.string.accreditation_result_grade_excellent),
        )
        AccreditationServerGrade.Satisfactory -> Triple(
            PrimaryGreen,
            Color.White,
            stringResource(R.string.accreditation_result_grade_satisfactory),
        )
        AccreditationServerGrade.ReVisit -> Triple(
            WarningYellow,
            Color.Black,
            stringResource(R.string.accreditation_result_grade_revisit),
        )
        AccreditationServerGrade.Denial -> Triple(
            ErrorRed,
            Color.White,
            stringResource(R.string.accreditation_result_grade_denial),
        )
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bg,
        shadowElevation = 1.dp,
    ) {
        Text(
            text = stringResource(R.string.accreditation_result_grade_badge_format, label),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            maxLines = 1,
        )
    }
}

@Composable
private fun gradeAccent(grade: AccreditationServerGrade): Color = when (grade) {
    AccreditationServerGrade.Excellent -> Color(0xFF1B5E20)
    AccreditationServerGrade.Satisfactory -> PrimaryGreen
    AccreditationServerGrade.ReVisit -> WarningYellow
    AccreditationServerGrade.Denial -> ErrorRed
}

@Composable
private fun decisionLabel(decision: AccreditationServerDecision): String = when (decision) {
    AccreditationServerDecision.FullAccreditation ->
        stringResource(R.string.accreditation_result_decision_full)
    AccreditationServerDecision.ProvisionalConditional ->
        stringResource(R.string.accreditation_result_decision_provisional)
    AccreditationServerDecision.Denial ->
        stringResource(R.string.accreditation_result_decision_denial)
    AccreditationServerDecision.Revocation ->
        stringResource(R.string.accreditation_result_decision_revocation)
    AccreditationServerDecision.Deferral ->
        stringResource(R.string.accreditation_result_decision_deferral)
}

@Composable
private fun ResultRawScoresCard(rows: List<AssessmentResultScoreRow>) {
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.FactCheck,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(R.string.accreditation_result_raw_scores_title),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = stringResource(R.string.accreditation_result_raw_scores_badge),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            rows.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    pair.forEach { row ->
                        ResultScoreTile(
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
private fun ResultScoreTile(
    modifier: Modifier = Modifier,
    row: AssessmentResultScoreRow,
) {
    val earned = row.rawScore.toIntOrNull() ?: 0
    val fraction = if (row.maxPoints == 0) 0f else earned.toFloat() / row.maxPoints.toFloat()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = AccrdCardBorder,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(
                    R.string.accreditation_result_section_format,
                    row.sectionIndex,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = row.groupLabel,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.accreditation_result_earned),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        R.string.accreditation_result_earned_format,
                        row.rawScore,
                        row.maxPoints,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            LinearProgressIndicator(
                progress = { fraction.coerceIn(0f, 1f) },
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
private fun ResultConditionsCard(conditions: List<String>) {
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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Gavel,
                    contentDescription = null,
                    tint = WarningYellow,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.accreditation_result_conditions_title),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = AccrdCardBorder,
            ) {
                Column {
                    conditions.forEachIndexed { index, condition ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryGreen,
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                            Text(
                                text = condition,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssessmentResultBottomBar(
    onViewCertificate: () -> Unit,
    onDone: () -> Unit,
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
                onClick = onDone,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
            ) {
                Text(
                    text = stringResource(R.string.accreditation_result_done),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Button(
                onClick = onViewCertificate,
                modifier = Modifier
                    .weight(1.6f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = stringResource(R.string.accreditation_result_view_certificate),
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
private fun AssessmentResultContentPreview() {
    ChprbnTheme {
        AssessmentResultContent(uiState = AssessmentResultUiState.placeholder())
    }
}
