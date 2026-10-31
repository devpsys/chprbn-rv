package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.ErrorRed
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.SuccessGreen
import ng.com.chprbn.mobile.core.designsystem.WarningYellow
import ng.com.chprbn.mobile.core.network.stripHostsAndUrls

private val InfoBlue = Color(0xFF0277BD)

/**
 * Compact draft-lifecycle badge for hub / sync surfaces:
 * Saved Locally (yellow), Synced (green), Sync Failed (red).
 */
enum class DraftBadgeKind {
    SavedLocally,
    Synced,
    SyncFailed,
}

fun SectionDraftStatus.toDraftBadgeKind(): DraftBadgeKind? = when (this) {
    SectionDraftStatus.SavedLocally,
    SectionDraftStatus.PendingSync,
    -> DraftBadgeKind.SavedLocally
    SectionDraftStatus.Synced -> DraftBadgeKind.Synced
    SectionDraftStatus.SyncFailed -> DraftBadgeKind.SyncFailed
    SectionDraftStatus.NotStarted,
    SectionDraftStatus.InProgress,
    -> null
}

/**
 * Pill for SSQ section draft lifecycle (overview + section headers).
 * AttachmentThumbnail lives in [AttachmentManager] (Prompt H) — do not duplicate.
 */
@Composable
fun SectionCompletionChip(
    status: SectionDraftStatus,
    modifier: Modifier = Modifier,
) {
    val (labelRes, tint, bg, icon) = sectionCompletionVisuals(status)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = tint,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun sectionCompletionVisuals(
    status: SectionDraftStatus,
): QuadVisuals = when (status) {
    SectionDraftStatus.NotStarted -> QuadVisuals(
        R.string.accreditation_component_status_not_started,
        MaterialTheme.colorScheme.onSurfaceVariant,
        MaterialTheme.colorScheme.surfaceVariant,
        Icons.Outlined.RadioButtonUnchecked,
    )
    SectionDraftStatus.InProgress -> QuadVisuals(
        R.string.accreditation_component_status_in_progress,
        WarningYellow,
        WarningYellow.copy(alpha = 0.12f),
        Icons.Outlined.Pending,
    )
    SectionDraftStatus.SavedLocally -> QuadVisuals(
        R.string.accreditation_component_status_saved_locally,
        PrimaryGreen,
        PrimaryGreen.copy(alpha = 0.12f),
        Icons.Filled.Save,
    )
    SectionDraftStatus.PendingSync -> QuadVisuals(
        R.string.accreditation_component_status_pending_sync,
        InfoBlue,
        InfoBlue.copy(alpha = 0.12f),
        Icons.Filled.CloudUpload,
    )
    SectionDraftStatus.Synced -> QuadVisuals(
        R.string.accreditation_component_status_synced,
        SuccessGreen,
        SuccessGreen.copy(alpha = 0.12f),
        Icons.Filled.CloudDone,
    )
    SectionDraftStatus.SyncFailed -> QuadVisuals(
        R.string.accreditation_component_status_sync_failed,
        ErrorRed,
        ErrorRed.copy(alpha = 0.12f),
        Icons.Filled.CloudOff,
    )
}

private data class QuadVisuals(
    val labelRes: Int,
    val tint: Color,
    val bg: Color,
    val icon: ImageVector,
)

@Composable
fun DraftStatusBadge(
    kind: DraftBadgeKind,
    modifier: Modifier = Modifier,
) {
    val (labelRes, tint, bg) = when (kind) {
        DraftBadgeKind.SavedLocally -> Triple(
            R.string.accreditation_component_draft_saved_locally,
            WarningYellow,
            WarningYellow.copy(alpha = 0.14f),
        )
        DraftBadgeKind.Synced -> Triple(
            R.string.accreditation_component_draft_synced,
            SuccessGreen,
            SuccessGreen.copy(alpha = 0.14f),
        )
        DraftBadgeKind.SyncFailed -> Triple(
            R.string.accreditation_component_draft_sync_failed,
            ErrorRed,
            ErrorRed.copy(alpha = 0.14f),
        )
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(tint),
            )
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = tint,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun DraftStatusBadge(
    status: SectionDraftStatus,
    modifier: Modifier = Modifier,
) {
    val kind = status.toDraftBadgeKind() ?: return
    DraftStatusBadge(kind = kind, modifier = modifier)
}

/**
 * Institution sync failure row. Message is sanitised via [stripHostsAndUrls]
 * so hostnames / URLs never reach the officer UI.
 */
@Composable
fun SyncErrorRow(
    institutionName: String,
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sanitised = remember(errorMessage) { errorMessage.stripHostsAndUrls() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.25f)),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ErrorRed.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SyncProblem,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = institutionName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = sanitised,
                        style = MaterialTheme.typography.labelSmall,
                        color = ErrorRed,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            OutlinedButton(
                onClick = onRetry,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, PrimaryGreen),
                modifier = Modifier.height(34.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.accreditation_component_sync_error_retry),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionCompletionChipPreview() {
    ChprbnTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionDraftStatus.entries.forEach { status ->
                SectionCompletionChip(status = status)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DraftStatusBadgePreview() {
    ChprbnTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DraftBadgeKind.entries.forEach { kind ->
                DraftStatusBadge(kind = kind)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncErrorRowPreview() {
    ChprbnTheme {
        SyncErrorRow(
            institutionName = "ABUTH Zaria",
            errorMessage = "failed to connect to api.chprbn.gov.ng/216.219.94.166 (port 443)",
            onRetry = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
