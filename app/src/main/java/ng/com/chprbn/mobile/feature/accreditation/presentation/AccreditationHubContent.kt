package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.DomainDisabled
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.ErrorRed
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.SuccessGreen
import ng.com.chprbn.mobile.core.designsystem.WarningYellow
import ng.com.chprbn.mobile.core.designsystem.components.AppTopBar
import ng.com.chprbn.mobile.core.designsystem.components.GridMenuCard
import ng.com.chprbn.mobile.core.designsystem.components.LoadingState
import ng.com.chprbn.mobile.core.designsystem.components.PrimaryButton

@Composable
fun AccreditationHubContent(
    modifier: Modifier = Modifier,
    uiState: AccreditationHubUiState,
    onBack: () -> Unit = {},
    onQuickSync: () -> Unit = {},
    onStartNewAssessment: () -> Unit = {},
    onContinueAssessment: () -> Unit = {},
    onLocalDrafts: () -> Unit = {},
    onSyncStatus: () -> Unit = {},
    onInstitutionClick: (String) -> Unit = {},
    onStartAssessment: (String) -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = scheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Same shared header as Exam Dashboard / OSYVALAC Verification.
            AppTopBar(
                onNotifications = onQuickSync,
                onBack = onBack,
            )
            when {
                uiState.isLoading -> LoadingState(modifier = Modifier.fillMaxSize())

                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    uiState.officer?.let { OfficerBanner(officer = it) }
                    FeatureGridSection(
                        grid = uiState.featureGrid,
                        onStartNewAssessment = onStartNewAssessment,
                        onContinueAssessment = onContinueAssessment,
                        onLocalDrafts = onLocalDrafts,
                        onSyncStatus = onSyncStatus,
                    )
                    AssignedInstitutionsSection(
                        institutions = uiState.assignedInstitutions,
                        upcoming = uiState.upcomingInstitutions,
                        onInstitutionClick = onInstitutionClick,
                        onStartAssessment = onStartAssessment,
                    )
                    OfflineNoticeBar()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun OfficerBanner(officer: OfficerBannerUiState) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = officer.initials,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Column {
                    Text(
                        text = officer.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = officer.registrationLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (officer.isVerified) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = SuccessGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen),
                        )
                        Text(
                            text = stringResource(R.string.accreditation_hub_officer_verified),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureGridSection(
    grid: FeatureGridUiState,
    onStartNewAssessment: () -> Unit,
    onContinueAssessment: () -> Unit,
    onLocalDrafts: () -> Unit,
    onSyncStatus: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.feature_grid_section_label),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
            )
            OnlineStatusChip(isOnline = grid.isOnline)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GridMenuCard(
                title = stringResource(R.string.accreditation_hub_tile_start_title),
                subtitle = grid.startSubtitle,
                icon = Icons.Outlined.WorkspacePremium,
                isPrimary = true,
                onClick = onStartNewAssessment,
                modifier = Modifier
                    .weight(1f)
                    .height(148.dp),
            )
            GridMenuCard(
                title = stringResource(R.string.accreditation_hub_tile_continue_title),
                subtitle = grid.continueSubtitle,
                icon = Icons.Outlined.PlayCircle,
                isPrimary = false,
                onClick = onContinueAssessment,
                modifier = Modifier
                    .weight(1f)
                    .height(148.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GridMenuCard(
                title = stringResource(R.string.accreditation_hub_tile_drafts_title),
                subtitle = grid.draftsSubtitle,
                icon = Icons.Outlined.EditNote,
                isPrimary = false,
                onClick = onLocalDrafts,
                modifier = Modifier
                    .weight(1f)
                    .height(148.dp),
            )
            GridMenuCard(
                title = stringResource(R.string.accreditation_hub_tile_sync_title),
                subtitle = grid.syncSubtitle,
                icon = Icons.Outlined.CloudDone,
                isPrimary = false,
                onClick = onSyncStatus,
                modifier = Modifier
                    .weight(1f)
                    .height(148.dp),
            )
        }
    }
}

@Composable
private fun OnlineStatusChip(isOnline: Boolean) {
    val label = if (isOnline) {
        stringResource(R.string.feature_grid_status_online)
    } else {
        stringResource(R.string.accreditation_hub_status_offline)
    }
    val tint = if (isOnline) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tint.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(tint),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = tint,
            )
        }
    }
}

@Composable
private fun AssignedInstitutionsSection(
    institutions: List<AssignedInstitutionUiState>,
    upcoming: List<UpcomingInstitutionUiState>,
    onInstitutionClick: (String) -> Unit,
    onStartAssessment: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.accreditation_hub_assigned_section),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                )
                if (institutions.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = PrimaryGreen.copy(alpha = 0.1f),
                    ) {
                        Text(
                            text = stringResource(
                                R.string.accreditation_hub_assigned_count_format,
                                institutions.size,
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryGreen,
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.accreditation_hub_strict_assignment),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (institutions.isEmpty()) {
            EmptyAssignedState()
        } else {
            institutions.forEach { institution ->
                AssignedInstitutionCard(
                    institution = institution,
                    onClick = { onInstitutionClick(institution.id) },
                    onStartAssessment = { onStartAssessment(institution.id) },
                )
            }
        }

        upcoming.forEach { item ->
            UpcomingInstitutionRow(item = item)
        }
    }
}

@Composable
private fun EmptyAssignedState() {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, scheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.DomainDisabled,
                contentDescription = null,
                tint = PrimaryGreen.copy(alpha = 0.55f),
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = stringResource(R.string.accreditation_hub_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryGreen,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.accreditation_hub_empty_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AssignedInstitutionCard(
    institution: AssignedInstitutionUiState,
    onClick: () -> Unit,
    onStartAssessment: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.15f)),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Domain,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = stringResource(R.string.accreditation_hub_zonal_assignment),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            letterSpacing = 0.8.sp,
                        )
                    }
                    Text(
                        text = institution.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                    )
                }
                InstitutionSyncBadge(status = institution.syncStatus)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(scheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InstitutionMetaRow(
                    icon = Icons.Filled.LocationOn,
                    text = institution.locationLabel,
                )
                InstitutionMetaRow(
                    icon = Icons.AutoMirrored.Outlined.Assignment,
                    text = stringResource(
                        R.string.accreditation_hub_protocol_format,
                        institution.protocolLabel,
                    ),
                )
                InstitutionMetaRow(
                    icon = Icons.Filled.History,
                    text = stringResource(
                        R.string.accreditation_hub_last_synced_format,
                        institution.lastSyncedLabel,
                    ),
                )
            }

            PrimaryButton(
                text = stringResource(R.string.accreditation_hub_action_start_assessment),
                modifier = Modifier.fillMaxWidth(),
                onClick = onStartAssessment,
            )

            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Filled.VerifiedUser,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(R.string.accreditation_hub_assignment_verified_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun InstitutionMetaRow(
    icon: ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun InstitutionSyncBadge(status: InstitutionSyncStatus) {
    val (labelRes, tint) = when (status) {
        InstitutionSyncStatus.Synced ->
            R.string.accreditation_hub_sync_synced to SuccessGreen
        InstitutionSyncStatus.DraftPendingSync ->
            R.string.accreditation_hub_sync_draft_pending to WarningYellow
        InstitutionSyncStatus.SyncFailed ->
            R.string.accreditation_hub_sync_failed to ErrorRed
        InstitutionSyncStatus.NeverSynced ->
            R.string.accreditation_hub_sync_never to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tint.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                fontWeight = FontWeight.SemiBold,
                color = tint,
            )
        }
    }
}

@Composable
private fun UpcomingInstitutionRow(item: UpcomingInstitutionUiState) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.7f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.accreditation_hub_upcoming_label),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp,
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.scheduleLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Outlined.LockClock,
                contentDescription = stringResource(R.string.accreditation_hub_upcoming_locked_cd),
                tint = scheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun OfflineNoticeBar() {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = scheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.WifiOff,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.accreditation_hub_offline_notice),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 411)
@Composable
private fun AccreditationHubContentPreview() {
    ChprbnTheme {
        AccreditationHubContent(uiState = AccreditationHubUiState.placeholder())
    }
}

@Preview(showBackground = true, widthDp = 411, name = "Empty assigned")
@Composable
private fun AccreditationHubEmptyPreview() {
    ChprbnTheme {
        AccreditationHubContent(
            uiState = AccreditationHubUiState.placeholder().copy(
                assignedInstitutions = emptyList(),
                upcomingInstitutions = emptyList(),
            ),
        )
    }
}
