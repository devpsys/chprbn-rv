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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.components.AppTopBar

@Composable
fun ExamDashboardScreen(
    viewModel: ExamDashboardViewModel = hiltViewModel(),
    onNotifications: () -> Unit = {},
    onLogAttendance: () -> Unit = {},
    onAttendanceMore: () -> Unit = {},
    onGradePractical: () -> Unit = {},
    onPracticalInfo: () -> Unit = {},
    onDownloadDossier: () -> Unit = {},
    onExamDashboardTab: () -> Unit = {},
    onStatisticsTab: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExamDashboardScreenContent(
        uiState = uiState,
        onNotifications = onNotifications,
        onLogAttendance = onLogAttendance,
        onAttendanceMore = onAttendanceMore,
        onGradePractical = onGradePractical,
        onPracticalInfo = onPracticalInfo,
        onDownloadDossier = onDownloadDossier,
        onExamDashboardTab = onExamDashboardTab,
        onStatisticsTab = onStatisticsTab
    )
}

@Composable
private fun ExamDashboardScreenContent(
    uiState: ExamDashboardUiState,
    onNotifications: () -> Unit = {},
    onLogAttendance: () -> Unit = {},
    onAttendanceMore: () -> Unit = {},
    onGradePractical: () -> Unit = {},
    onPracticalInfo: () -> Unit = {},
    onDownloadDossier: () -> Unit = {},
    onExamDashboardTab: () -> Unit = {},
    onStatisticsTab: () -> Unit = {}
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
                    modifier = Modifier.padding(end = 24.dp),
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
                        text = "Download Dossier",
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
                    onNotifications = onNotifications
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    ExamOfficerSessionCard(
                        heroImageUrl = uiState.heroImageUrl,
                        sectionLabel = uiState.institutionSectionLabel,
                        institutionName = uiState.institutionName,
                        institutionCode = uiState.institutionCode,
                        institutionLocation = uiState.institutionLocation
                    )
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Administrative Tasks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onBackground,
                            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                        )
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
                            trailingContentDescription = "More options"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
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
                            trailingContentDescription = "Information"
                        )
                        Spacer(modifier = Modifier.height(88.dp))
//                        ExamQuickStatsRow(
//                            attendanceRate = uiState.attendanceRatePercent,
//                            gpa = uiState.currentGpa,
//                            modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
//                        )
                    }
                }
            }
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
                        contentDescription = "Notifications",
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
                        contentDescription = "Institution banner",
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
                        contentDescription = "Institution banner",
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
        Column(
            modifier = Modifier.padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Code: ",
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
                    text = "Location: ",
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

@Composable
private fun ExamQuickStatsRow(
    attendanceRate: String,
    gpa: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ExamStatTile(
            label = "Attendance Rate",
            value = attendanceRate,
            modifier = Modifier.weight(1f)
        )
        ExamStatTile(
            label = "Current GPA",
            value = gpa,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ExamStatTile(label: String, value: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = scheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExamDashboardScreenPreview() {
    ChprbnTheme {
        ExamDashboardScreenContent(uiState = ExamDashboardUiState.placeholder())
    }
}
