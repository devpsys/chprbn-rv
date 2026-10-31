package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.ErrorRed
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.SuccessGreen
import ng.com.chprbn.mobile.core.designsystem.WarningYellow
import ng.com.chprbn.mobile.core.designsystem.components.LoadingState
import ng.com.chprbn.mobile.core.designsystem.components.PrimaryButton
import ng.com.chprbn.mobile.core.designsystem.components.SuccessDialog
import ng.com.chprbn.mobile.core.designsystem.components.SyncingOverlay

private val InfoBlue = Color(0xFF0277BD)

@Composable
fun SyncStatusContent(
    modifier: Modifier = Modifier,
    uiState: SyncStatusUiState,
    onBack: () -> Unit = {},
    onRetryDraft: (draftId: String) -> Unit = {},
    onRetryFailed: () -> Unit = {},
    onDismissRetrySuccess: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AssessmentFlowTopBar(
                    title = stringResource(R.string.accreditation_sync_status_title),
                    onBack = onBack,
                )
            },
            bottomBar = {
                if (!uiState.isLoading) {
                    SyncStatusBottomBar(
                        retryEnabled = uiState.failedDrafts.isNotEmpty() && !uiState.isRetrying,
                        onRetryFailed = onRetryFailed,
                        onClose = onBack,
                    )
                }
            },
        ) { paddingValues ->
            when {
                uiState.isLoading -> LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                ) {
                    item {
                        OverallSyncMonitorCard(
                            overallStatus = uiState.overallStatus,
                            progressFraction = uiState.progressFraction,
                            lastSuccessfulSyncLabel = uiState.lastSuccessfulSyncLabel,
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.accreditation_sync_status_failed_jobs_format,
                                    uiState.failedDrafts.size,
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(R.string.accreditation_sync_status_outbox_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (uiState.failedDrafts.isEmpty()) {
                        item { SyncStatusEmptyState() }
                    } else {
                        items(uiState.failedDrafts, key = { it.id }) { draft ->
                            FailedSyncDraftCard(
                                draft = draft,
                                onRetry = { onRetryDraft(draft.id) },
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }

        if (uiState.isRetrying) {
            SyncingOverlay(
                title = stringResource(R.string.accreditation_sync_status_overlay_title),
                subtitle = stringResource(R.string.accreditation_sync_status_overlay_subtitle),
                encryptedLabel = stringResource(R.string.sync_loading_encrypted_badge),
            )
        }

        if (uiState.showRetrySuccess) {
            SuccessDialog(
                title = stringResource(R.string.accreditation_sync_status_success_title),
                message = stringResource(R.string.accreditation_sync_status_success_message),
                primaryButtonText = stringResource(R.string.accreditation_sync_status_success_done),
                onPrimary = onDismissRetrySuccess,
                onDismiss = onDismissRetrySuccess,
            )
        }
    }
}

@Composable
private fun OverallSyncMonitorCard(
    overallStatus: OverallSyncStatusUi,
    progressFraction: Float,
    lastSuccessfulSyncLabel: String,
) {
    val (labelRes, color, container, icon) = overallStatusVisuals(overallStatus)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.accreditation_sync_status_monitor_label),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = PrimaryGreen,
                )
                Surface(
                    color = container,
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(labelRes),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = color,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.accreditation_sync_status_queue_progress),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(
                        R.string.accreditation_sync_status_progress_format,
                        (progressFraction * 100).toInt(),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryGreen,
                trackColor = PrimaryGreen.copy(alpha = 0.12f),
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.accreditation_sync_status_last_success_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = lastSuccessfulSyncLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun overallStatusVisuals(
    status: OverallSyncStatusUi,
): QuadOverall = when (status) {
    OverallSyncStatusUi.Synced -> QuadOverall(
        R.string.accreditation_sync_status_overall_synced,
        SuccessGreen,
        SuccessGreen.copy(alpha = 0.12f),
        Icons.Filled.CheckCircle,
    )
    OverallSyncStatusUi.Syncing -> QuadOverall(
        R.string.accreditation_sync_status_overall_syncing,
        InfoBlue,
        InfoBlue.copy(alpha = 0.12f),
        Icons.Outlined.Sync,
    )
    OverallSyncStatusUi.SyncFailed -> QuadOverall(
        R.string.accreditation_sync_status_overall_failed,
        ErrorRed,
        ErrorRed.copy(alpha = 0.12f),
        Icons.Outlined.ErrorOutline,
    )
    OverallSyncStatusUi.Offline -> QuadOverall(
        R.string.accreditation_sync_status_overall_offline,
        WarningYellow,
        WarningYellow.copy(alpha = 0.12f),
        Icons.Outlined.CloudOff,
    )
}

private data class QuadOverall(
    val labelRes: Int,
    val color: Color,
    val container: Color,
    val icon: ImageVector,
)

@Composable
private fun FailedSyncDraftCard(
    draft: LocalDraftItemUi,
    onRetry: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = draft.assignmentCode,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = draft.institutionName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
                LocalDraftSyncBadge(state = draft.syncState)
            }
            SyncErrorRow(
                institutionName = draft.institutionName,
                errorMessage = draft.lastRawError
                    ?: stringResource(R.string.accreditation_dialog_sync_failed_title),
                onRetry = onRetry,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        R.string.accreditation_sync_status_meta_format,
                        draft.sectionsCompleted,
                        draft.totalSections,
                        draft.attachmentsCount,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        R.string.accreditation_sync_status_attempts_format,
                        draft.attemptCount,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SyncStatusEmptyState() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.accreditation_sync_status_empty_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SuccessGreen,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.accreditation_sync_status_empty_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SyncStatusBottomBar(
    retryEnabled: Boolean,
    onRetryFailed: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PrimaryButton(
                text = stringResource(R.string.accreditation_sync_status_retry_failed),
                onClick = onRetryFailed,
                enabled = retryEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, PrimaryGreen),
            ) {
                Text(
                    text = stringResource(R.string.accreditation_sync_status_close),
                    color = PrimaryGreen,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncStatusContentPreview() {
    ChprbnTheme {
        SyncStatusContent(uiState = SyncStatusUiState.placeholder())
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncStatusContentSyncedPreview() {
    ChprbnTheme {
        SyncStatusContent(
            uiState = SyncStatusUiState.placeholder().copy(
                overallStatus = OverallSyncStatusUi.Synced,
                progressFraction = 1f,
                failedDrafts = emptyList(),
                lastSuccessfulSyncLabel = "Just now",
            ),
        )
    }
}
