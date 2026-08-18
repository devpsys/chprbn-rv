package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.components.AppTopBar
import ng.com.chprbn.mobile.core.designsystem.components.DownloadWarningDialog
import ng.com.chprbn.mobile.core.designsystem.components.DownloadingOverlay
import ng.com.chprbn.mobile.core.designsystem.components.ErrorDialog
import ng.com.chprbn.mobile.core.designsystem.components.LoadingState
import ng.com.chprbn.mobile.core.designsystem.components.SuccessDialog

@Composable
fun ExamDashboardScreen(
    viewModel: ExamDashboardViewModel = hiltViewModel(),
    onNotifications: () -> Unit = {},
    onLogAttendance: () -> Unit = {},
    onAttendanceMore: () -> Unit = {},
    onGradePractical: () -> Unit = {},
    onPracticalInfo: () -> Unit = {},
    onExamDashboardTab: () -> Unit = {},
    onStatisticsTab: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val loggedOut by viewModel.loggedOut.collectAsStateWithLifecycle()

    // The Statistics tab returns here via popBackStack, not a fresh
    // navigate(), so this ViewModel instance survives the round trip —
    // reload on every resume (covers first entry too) so a sync/clear
    // over on Statistics isn't left stale on the dashboard.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }

    LaunchedEffect(loggedOut) {
        if (loggedOut) onLogout()
    }

    ExamDashboardScreenContent(
        uiState = uiState,
        onNotifications = onNotifications,
        onLogAttendance = onLogAttendance,
        onAttendanceMore = onAttendanceMore,
        onGradePractical = onGradePractical,
        onPracticalInfo = onPracticalInfo,
        onDownloadDossier = viewModel::onDownloadDossierClicked,
        onExamDashboardTab = onExamDashboardTab,
        onStatisticsTab = onStatisticsTab,
        onLogout = viewModel::onLogoutClicked
    )
    ExamDownloadDossierOverlay(
        state = downloadState,
        onConfirm = viewModel::onDownloadConfirmed,
        onDismiss = viewModel::onDownloadDismissed,
    )
}

@Composable
private fun ExamDownloadDossierOverlay(
    state: DownloadDossierUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (state) {
        DownloadDossierUiState.Idle -> Unit
        DownloadDossierUiState.WarningShown -> DownloadWarningDialog(
            title = stringResource(R.string.exam_download_warning_title),
            message = stringResource(R.string.exam_download_warning_message),
            footnote = stringResource(R.string.exam_download_warning_footnote),
            primaryButtonText = stringResource(R.string.exam_download_warning_confirm),
            secondaryButtonText = stringResource(R.string.exam_download_warning_cancel),
            onConfirm = onConfirm,
            onCancel = onDismiss,
        )
        DownloadDossierUiState.Downloading -> DownloadingOverlay(
            title = stringResource(R.string.exam_download_loading_title),
            subtitle = stringResource(R.string.exam_download_loading_subtitle),
            encryptedLabel = stringResource(R.string.download_loading_encrypted_badge),
            // The use case doesn't surface real progress, so this renders
            // the same spinning-icon indeterminate look SyncingOverlay uses
            // (no progressFraction) instead of a bar frozen at a fake value.
        )
        is DownloadDossierUiState.Success -> SuccessDialog(
            title = stringResource(R.string.exam_download_success_title),
            message = stringResource(
                R.string.exam_download_success_message_format,
                state.papersCount,
                state.newCandidatesCount,
                state.skippedCandidatesCount,
            ),
            primaryButtonText = stringResource(R.string.action_ok),
            onPrimary = onDismiss,
            onDismiss = onDismiss,
        )
        is DownloadDossierUiState.Error -> ErrorDialog(
            title = stringResource(R.string.exam_download_error_title),
            message = state.message,
            primaryButtonText = stringResource(R.string.exam_download_error_action_retry),
            secondaryButtonText = stringResource(R.string.exam_download_error_action_close),
            onPrimary = onConfirm,
            onSecondary = onDismiss,
            onDismiss = onDismiss,
        )
    }
}

@Composable
internal fun ExamDashboardScreenContent(
    uiState: ExamDashboardUiState,
    onNotifications: () -> Unit = {},
    onLogAttendance: () -> Unit = {},
    onAttendanceMore: () -> Unit = {},
    onGradePractical: () -> Unit = {},
    onPracticalInfo: () -> Unit = {},
    onDownloadDossier: () -> Unit = {},
    onExamDashboardTab: () -> Unit = {},
    onStatisticsTab: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = scheme.background
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                ExamBottomNavBar(
                    modifier = Modifier.fillMaxWidth(),
                    selectedTab = ExamBottomNavSelection.Dashboard,
                    onDashboard = onExamDashboardTab,
                    onStatistics = onStatisticsTab
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onDownloadDossier,
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.exam_dashboard_action_download),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AppTopBar(
                    onNotifications = onNotifications,
                    onLogout = onLogout
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                if (uiState.isLoading) {
                    LoadingState()
                } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (!uiState.hasDownloadedData) {
                        ExamDashboardEmptyState(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp, bottom = 24.dp)
                        )
                    } else {
                        ExamOfficerSessionCard(
                            heroImageUrl = uiState.heroImageUrl,
                            sectionLabel = uiState.institutionSectionLabel,
                            institutionName = uiState.institutionName,
                            institutionCode = uiState.institutionCode,
                            institutionLocation = uiState.institutionLocation
                        )
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = stringResource(R.string.exam_dashboard_section_admin_tasks),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onBackground,
                                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                            )
                            // Three states for the tasks section:
                            //   1. hasSchedules = false                    → NoSchedulesMessage (dossier empty)
                            //   2. hasSchedules && !attendance && !practical → AdminOnlyMessage (papers exist but nothing to do here)
                            //   3. otherwise                                → whichever cards apply, independently
                            when {
                                !uiState.hasSchedules -> ExamDashboardNoSchedulesMessage()
                                !uiState.hasAttendanceCard && !uiState.hasPracticalAssessment ->
                                    ExamDashboardAdminOnlyMessage(centreName = uiState.institutionName)
                                else -> {
                                    if (uiState.hasAttendanceCard) {
                                        ExamTaskCard(
                                            imageUrl = uiState.attendanceTask.imageUrl,
                                            imageContentDescription = uiState.attendanceTask.imageContentDescription,
                                            chipPrimaryLabel = uiState.attendanceTask.chipPrimaryLabel,
                                            chipPrimaryContainer = scheme.secondaryContainer,
                                            chipPrimaryText = scheme.onSecondaryContainer,
                                            chipSecondaryLabel = uiState.attendanceTask.chipSecondaryLabel,
                                            chipSecondaryContainer = scheme.tertiaryContainer,
                                            chipSecondaryText = scheme.onTertiaryContainer,
                                            title = uiState.attendanceTask.title,
                                            description = uiState.attendanceTask.description,
                                            primaryActionLabel = uiState.attendanceTask.primaryActionLabel,
                                            onPrimaryAction = onLogAttendance,
                                            trailingIcon = Icons.Filled.MoreHoriz,
                                            onTrailingClick = onAttendanceMore,
                                            trailingContentDescription = stringResource(R.string.exam_dashboard_more_options_cd)
                                        )
                                    }
                                    if (uiState.hasPracticalAssessment) {
                                        // Only pad above the practical card when the
                                        // attendance card is also being drawn — otherwise
                                        // it doubles up with the section header spacing.
                                        if (uiState.hasAttendanceCard) {
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                        ExamTaskCard(
                                            imageUrl = uiState.practicalTask.imageUrl,
                                            imageContentDescription = uiState.practicalTask.imageContentDescription,
                                            chipPrimaryLabel = uiState.practicalTask.chipPrimaryLabel,
                                            chipPrimaryContainer = scheme.primaryContainer,
                                            chipPrimaryText = scheme.onPrimaryContainer,
                                            chipSecondaryLabel = uiState.practicalTask.chipSecondaryLabel,
                                            chipSecondaryContainer = scheme.surfaceVariant,
                                            chipSecondaryText = scheme.onSurfaceVariant,
                                            title = uiState.practicalTask.title,
                                            description = uiState.practicalTask.description,
                                            primaryActionLabel = uiState.practicalTask.primaryActionLabel,
                                            onPrimaryAction = onGradePractical,
                                            trailingIcon = Icons.Filled.Info,
                                            onTrailingClick = onPracticalInfo,
                                            trailingContentDescription = stringResource(R.string.exam_dashboard_information_cd)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(88.dp))
                        }
                    }
                }
                }
                }
            }
        }
    }
}

/**
 * Shown in place of the institution card + admin tasks when no dossier
 * has ever been downloaded to this device (E1/E9 hardening — a blank
 * "screen looks broken" state is worse than telling the officer exactly
 * what to do next: tap Download Records).
 */
@Composable
private fun ExamDashboardEmptyState(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(scheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(scheme.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudDownload,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(40.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.exam_dashboard_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.exam_dashboard_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/**
 * Shown instead of the attendance/practical task cards when the
 * downloaded center has zero papers scheduled for today.
 */
@Composable
private fun ExamDashboardNoSchedulesMessage(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.EventBusy,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.exam_dashboard_no_schedules_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.exam_dashboard_no_schedules_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

/**
 * Shown when the dossier has papers scheduled today but none of them
 * are actionable from this dashboard — i.e., the centre has been
 * downloaded, papersCount > 0, but there's neither a Theory paper
 * (attendance) nor practical sections (grading). Distinct from
 * [ExamDashboardNoSchedulesMessage] which handles the truly-empty case;
 * this one names the centre so the officer can confirm they're at the
 * right site.
 */
@Composable
private fun ExamDashboardAdminOnlyMessage(
    centreName: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.EventBusy,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.exam_dashboard_admin_only_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.exam_dashboard_admin_only_subtitle_format,
                    centreName,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ExamSessionTopBar(
    title: String,
    onMenu: () -> Unit,
    onNotifications: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        color = scheme.surface,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, scheme.outlineVariant, RoundedCornerShape(0.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(scheme.primaryContainer)
                    .clickable(onClick = onMenu),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu",
                    tint = scheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )
            IconButton(
                onClick = onNotifications,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(scheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = stringResource(R.string.exam_dashboard_notifications_cd),
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamOfficerSessionCard(
    heroImageUrl: String,
    sectionLabel: String,
    institutionName: String,
    institutionCode: String,
    institutionLocation: String
) {
    val scheme = MaterialTheme.colorScheme
    val mutedOnPrimary = scheme.onPrimary.copy(alpha = 0.9f)
    val context = LocalContext.current
    val corner = RoundedCornerShape(12.dp)

    BoxWithConstraints(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        val boxMaxWidth = maxWidth
        val wide = boxMaxWidth >= 600.dp
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = corner,
            color = scheme.primary,
            tonalElevation = 0.dp,
            shadowElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, scheme.primary)
        ) {
            if (wide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    val imageWidth = boxMaxWidth * 0.42f
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(heroImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.exam_dashboard_institution_banner_cd),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(imageWidth)
                            .height(imageWidth * (6f / 16f))
                    )
                    InstitutionCardTextBlock(
                        sectionLabel = sectionLabel,
                        institutionName = institutionName,
                        institutionCode = institutionCode,
                        institutionLocation = institutionLocation,
                        mutedOnPrimary = mutedOnPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(heroImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.exam_dashboard_institution_banner_cd),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 6f)
                    )
                    InstitutionCardTextBlock(
                        sectionLabel = sectionLabel,
                        institutionName = institutionName,
                        institutionCode = institutionCode,
                        institutionLocation = institutionLocation,
                        mutedOnPrimary = mutedOnPrimary,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InstitutionCardTextBlock(
    sectionLabel: String,
    institutionName: String,
    institutionCode: String,
    institutionLocation: String,
    mutedOnPrimary: Color,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = sectionLabel.uppercase(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = scheme.onPrimary.copy(alpha = 0.85f),
            letterSpacing = 1.sp
        )
        Text(
            text = institutionName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp,
            color = scheme.onPrimary
        )
        /*
        Column(
            modifier = Modifier.padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.exam_dashboard_label_code),
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedOnPrimary
                )
                Text(
                    text = institutionCode,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onPrimary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.exam_dashboard_label_location),
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedOnPrimary
                )
                Text(
                    text = institutionLocation,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onPrimary
                )
            }
        }
        */
    }
}

@Composable
private fun ExamTaskCard(
    imageUrl: String,
    imageContentDescription: String,
    chipPrimaryLabel: String,
    chipPrimaryContainer: Color,
    chipPrimaryText: Color,
    chipSecondaryLabel: String,
    chipSecondaryContainer: Color,
    chipSecondaryText: Color,
    title: String,
    description: String,
    primaryActionLabel: String,
    onPrimaryAction: () -> Unit,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onTrailingClick: () -> Unit,
    trailingContentDescription: String
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val boxMaxWidth = maxWidth
            val useRow = boxMaxWidth >= 600.dp
            if (useRow) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = imageContentDescription,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(boxMaxWidth * 0.33f)
                            .height(200.dp)
                    )
                    ExamTaskCardContent(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        chipPrimaryLabel = chipPrimaryLabel,
                        chipPrimaryContainer = chipPrimaryContainer,
                        chipPrimaryText = chipPrimaryText,
                        chipSecondaryLabel = chipSecondaryLabel,
                        chipSecondaryContainer = chipSecondaryContainer,
                        chipSecondaryText = chipSecondaryText,
                        title = title,
                        description = description,
                        primaryActionLabel = primaryActionLabel,
                        onPrimaryAction = onPrimaryAction,
                        trailingIcon = trailingIcon,
                        onTrailingClick = onTrailingClick,
                        trailingContentDescription = trailingContentDescription
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = imageContentDescription,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                    ExamTaskCardContent(
                        modifier = Modifier.fillMaxWidth(),
                        chipPrimaryLabel = chipPrimaryLabel,
                        chipPrimaryContainer = chipPrimaryContainer,
                        chipPrimaryText = chipPrimaryText,
                        chipSecondaryLabel = chipSecondaryLabel,
                        chipSecondaryContainer = chipSecondaryContainer,
                        chipSecondaryText = chipSecondaryText,
                        title = title,
                        description = description,
                        primaryActionLabel = primaryActionLabel,
                        onPrimaryAction = onPrimaryAction,
                        trailingIcon = trailingIcon,
                        onTrailingClick = onTrailingClick,
                        trailingContentDescription = trailingContentDescription
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamTaskCardContent(
    modifier: Modifier = Modifier,
    chipPrimaryLabel: String,
    chipPrimaryContainer: Color,
    chipPrimaryText: Color,
    chipSecondaryLabel: String,
    chipSecondaryContainer: Color,
    chipSecondaryText: Color,
    title: String,
    description: String,
    primaryActionLabel: String,
    onPrimaryAction: () -> Unit,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onTrailingClick: () -> Unit,
    trailingContentDescription: String
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                ExamChip(
                    text = chipPrimaryLabel,
                    container = chipPrimaryContainer,
                    textColor = chipPrimaryText
                )
                ExamChip(
                    text = chipSecondaryLabel,
                    container = chipSecondaryContainer,
                    textColor = chipSecondaryText
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
                lineHeight = 20.sp
            )
        }
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = primaryActionLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimary,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(scheme.primary)
                    .clickable(onClick = onPrimaryAction)
                    .padding(vertical = 10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(
                onClick = onTrailingClick,
                modifier = Modifier
                    .border(1.dp, scheme.outlineVariant, RoundedCornerShape(8.dp))
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = trailingContentDescription,
                    tint = scheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ExamChip(text: String, container: Color, textColor: Color) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = textColor,
        letterSpacing = 0.3.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(container)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Preview(showBackground = true, name = "Empty — nothing downloaded")
@Composable
private fun ExamDashboardScreenEmptyPreview() {
    ChprbnTheme {
        ExamDashboardScreenContent(uiState = ExamDashboardUiState.placeholder())
    }
}

@Preview(showBackground = true, name = "Loaded — with schedules")
@Composable
private fun ExamDashboardScreenPreview() {
    ChprbnTheme {
        ExamDashboardScreenContent(
            uiState = ExamDashboardUiState.placeholder().copy(hasDownloadedData = true),
        )
    }
}

@Preview(showBackground = true, name = "Loaded — no schedules today")
@Composable
private fun ExamDashboardScreenNoSchedulesPreview() {
    ChprbnTheme {
        ExamDashboardScreenContent(
            uiState = ExamDashboardUiState.placeholder().copy(
                hasDownloadedData = true,
                hasSchedules = false,
            ),
        )
    }
}
