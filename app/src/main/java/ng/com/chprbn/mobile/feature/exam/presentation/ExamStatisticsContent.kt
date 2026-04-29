package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ExamStatisticsContent(
    uiState: ExamStatisticsUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSyncNow: () -> Unit,
    onClearCached: () -> Unit,
    onExamDashboardTab: () -> Unit,
    onStatisticsTab: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = scheme.background,
        topBar = {
            ExamStatisticsTopBar(
                onBack = onBack,
                onRefresh = onRefresh
            )
        },
        bottomBar = {
            ExamBottomNavBar(
                modifier = Modifier.fillMaxWidth(),
                selectedTab = ExamBottomNavSelection.Statistics,
                onDashboard = onExamDashboardTab,
                onStatistics = onStatisticsTab
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SummaryCardsSection(
                recordsDownloaded = uiState.recordsDownloaded,
                attendanceCaptured = uiState.attendanceCaptured,
                syncedRecords = uiState.syncedRecords,
                recordsUpdatedLabel = uiState.recordsUpdatedLabel,
                attendanceSubtitle = uiState.attendanceSubtitle,
                syncProgressFraction = uiState.syncProgressFraction
            )
            SyncComparisonSection(
                cachedBarFraction = uiState.cachedBarFraction,
                syncedBarFraction = uiState.syncedBarFraction,
                cachedCountLabel = uiState.cachedCountLabel,
                syncedCountLabel = uiState.syncedCountLabel,
                totalCountLabel = uiState.totalCountLabel,
                pendingSyncLegendCount = uiState.pendingSyncLegendCount,
                successfullySyncedLegendCount = uiState.successfullySyncedLegendCount,
                footnote = uiState.footnote
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable(onClick = onSyncNow),
                    shape = RoundedCornerShape(12.dp),
                    color = scheme.primary,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = null,
                            tint = scheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sync Now",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimary
                        )
                    }
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable(onClick = onClearCached),
                    shape = RoundedCornerShape(12.dp),
                    color = scheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        scheme.error.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = null,
                            tint = scheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Clear Cached Records",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.error
                        )
                    }
                }
            }
            StatisticsIllustration(imageUrl = uiState.illustrationImageUrl)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ExamStatisticsTopBar(
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        color = scheme.surface,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            scheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = scheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Exam Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onBackground,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Refresh",
                    tint = scheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryCardsSection(
    recordsDownloaded: String,
    attendanceCaptured: String,
    syncedRecords: String,
    recordsUpdatedLabel: String,
    attendanceSubtitle: String,
    syncProgressFraction: Float
) {
    val scheme = MaterialTheme.colorScheme
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val compact = maxWidth < 400.dp
        val gap = 16.dp
        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                SummaryMetricCard(
                    icon = {
                        Icon(
                            Icons.Outlined.Download,
                            null,
                            tint = scheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = "Records Downloaded",
                    value = recordsDownloaded,
                    footer = {
                        Text(
                            recordsUpdatedLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                )
                SummaryMetricCard(
                    icon = {
                        Icon(
                            Icons.Outlined.Assignment,
                            null,
                            tint = scheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = "Attendance Captured",
                    value = attendanceCaptured,
                    footer = {
                        Text(
                            attendanceSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = scheme.primary
                        )
                    }
                )
                SummaryMetricCard(
                    icon = {
                        Icon(
                            Icons.Outlined.Sync,
                            null,
                            tint = scheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = "Synced Records",
                    value = syncedRecords,
                    footer = {
                        LinearSyncProgress(fraction = syncProgressFraction)
                    }
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                SummaryMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            Icons.Outlined.Download,
                            null,
                            tint = scheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = "Records Downloaded",
                    value = recordsDownloaded,
                    footer = {
                        Text(
                            recordsUpdatedLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                )
                SummaryMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            Icons.Outlined.Assignment,
                            null,
                            tint = scheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = "Attendance Captured",
                    value = attendanceCaptured,
                    footer = {
                        Text(
                            attendanceSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = scheme.primary
                        )
                    }
                )
                SummaryMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            Icons.Outlined.Sync,
                            null,
                            tint = scheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = "Synced Records",
                    value = syncedRecords,
                    footer = {
                        LinearSyncProgress(fraction = syncProgressFraction)
                    }
                )
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    footer: @Composable () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            scheme.outlineVariant.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                letterSpacing = (-0.25).sp
            )
            footer()
        }
    }
}

@Composable
private fun LinearSyncProgress(fraction: Float) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(scheme.surfaceVariant.copy(alpha = 0.8f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(6.dp)
                    .background(scheme.primary, RoundedCornerShape(999.dp))
            )
        }
    }
}

@Composable
private fun SyncComparisonSection(
    cachedBarFraction: Float,
    syncedBarFraction: Float,
    cachedCountLabel: String,
    syncedCountLabel: String,
    totalCountLabel: String,
    pendingSyncLegendCount: String,
    successfullySyncedLegendCount: String,
    footnote: String
) {
    val scheme = MaterialTheme.colorScheme
    val primaryLight = scheme.primary.copy(alpha = 0.14f)
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = scheme.surface,
            shadowElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                scheme.outlineVariant.copy(alpha = 0.8f)
            )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Sync Status Comparison",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val wide = maxWidth >= 600.dp
                    if (wide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            ComparisonBars(
                                modifier = Modifier.weight(1f),
                                primaryLight = primaryLight,
                                cachedBarFraction = cachedBarFraction,
                                syncedBarFraction = syncedBarFraction,
                                cachedCountLabel = cachedCountLabel,
                                syncedCountLabel = syncedCountLabel,
                                totalCountLabel = totalCountLabel
                            )
                            LegendColumn(
                                modifier = Modifier.weight(1f),
                                pendingSyncLegendCount = pendingSyncLegendCount,
                                successfullySyncedLegendCount = successfullySyncedLegendCount,
                                footnote = footnote
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                            ComparisonBars(
                                modifier = Modifier.fillMaxWidth(),
                                primaryLight = primaryLight,
                                cachedBarFraction = cachedBarFraction,
                                syncedBarFraction = syncedBarFraction,
                                cachedCountLabel = cachedCountLabel,
                                syncedCountLabel = syncedCountLabel,
                                totalCountLabel = totalCountLabel
                            )
                            LegendColumn(
                                modifier = Modifier.fillMaxWidth(),
                                pendingSyncLegendCount = pendingSyncLegendCount,
                                successfullySyncedLegendCount = successfullySyncedLegendCount,
                                footnote = footnote
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonBars(
    modifier: Modifier,
    primaryLight: Color,
    cachedBarFraction: Float,
    syncedBarFraction: Float,
    cachedCountLabel: String,
    syncedCountLabel: String,
    totalCountLabel: String
) {
    val scheme = MaterialTheme.colorScheme
    val barTotalHeight = 180.dp
    val cachedFill = scheme.tertiary
    val totalFill = scheme.outlineVariant
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        ChartBarColumn(
            trackColor = primaryLight,
            fillFraction = cachedBarFraction.coerceIn(0f, 1f),
            fillColor = cachedFill,
            caption = "Cached",
            valueLabel = cachedCountLabel,
            valueColor = cachedFill,
            barHeight = barTotalHeight
        )
        ChartBarColumn(
            trackColor = primaryLight,
            fillFraction = syncedBarFraction.coerceIn(0f, 1f),
            fillColor = scheme.primary,
            caption = "Synced",
            valueLabel = syncedCountLabel,
            valueColor = scheme.primary,
            barHeight = barTotalHeight
        )
        ChartBarColumn(
            trackColor = primaryLight,
            fillFraction = 1f,
            fillColor = totalFill,
            caption = "Total",
            valueLabel = totalCountLabel,
            valueColor = scheme.onSurface.copy(alpha = 0.75f),
            barHeight = barTotalHeight
        )
    }
}

@Composable
private fun ChartBarColumn(
    trackColor: Color,
    fillFraction: Float,
    fillColor: Color,
    caption: String,
    valueLabel: String,
    valueColor: Color,
    barHeight: Dp
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.width(64.dp)
    ) {
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(barHeight)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(trackColor),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fillFraction)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(fillColor)
            )
        }
        Text(
            text = caption.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Text(
            text = valueLabel,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun LegendColumn(
    modifier: Modifier,
    pendingSyncLegendCount: String,
    successfullySyncedLegendCount: String,
    footnote: String
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(scheme.surfaceVariant.copy(alpha = 0.45f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(scheme.tertiary)
                )
                Text(
                    text = "Records Pending Sync",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface.copy(alpha = 0.85f)
                )
            }
            Text(
                text = pendingSyncLegendCount,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(scheme.surfaceVariant.copy(alpha = 0.45f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(scheme.primary)
                )
                Text(
                    text = "Successfully Synced",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface.copy(alpha = 0.85f)
                )
            }
            Text(
                text = successfullySyncedLegendCount,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
        }
        Text(
            text = footnote,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
        )
    }
}

@Composable
private fun StatisticsIllustration(imageUrl: String) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.surfaceVariant)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.primary.copy(alpha = 0.1f))
        )
    }
}
