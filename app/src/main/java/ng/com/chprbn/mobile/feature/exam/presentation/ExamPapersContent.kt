package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.rememberScrollState
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults

@Composable
fun ExamPapersContent(
    uiState: ExamPapersUiState,
    onBack: () -> Unit,
    onOpenPaper: () -> Unit,
    onSyncNow: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = scheme.background,
        topBar = {
            ExamAttendanceTopBar(
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingSyncFab(
                onClick = onSyncNow
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SummaryDailyOverview(
                dailyOverviewTitle = uiState.dailyOverviewTitle,
                dailyDateLabel = uiState.dailyDateLabel,
                totalPapersLabel = uiState.totalPapersLabel,
                studentsLabel = uiState.studentsLabel,
                statusPillLabel = uiState.statusPillLabel
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.exam_papers_section_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onBackground
                )
                Text(
                    text = stringResource(R.string.exam_papers_section_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.papers.forEach { paper ->
                    PaperCard(
                        paper = paper,
                        onOpenPaper = onOpenPaper
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingSyncFab(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = scheme.primary,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = Icons.Filled.Sync,
                contentDescription = stringResource(R.string.exam_papers_action_sync),
                tint = scheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ExamAttendanceTopBar(
    onBack: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(color = scheme.surface, tonalElevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.exam_papers_action_back),
                    tint = scheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = stringResource(R.string.exam_papers_header_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onBackground,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
private fun SummaryDailyOverview(
    dailyOverviewTitle: String,
    dailyDateLabel: String,
    totalPapersLabel: String,
    studentsLabel: String,
    statusPillLabel: String
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = scheme.primary
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = dailyOverviewTitle,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = scheme.onPrimary.copy(alpha = 0.9f)
            )
            Text(
                text = dailyDateLabel,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = scheme.onPrimary.copy(alpha = 0.25f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatColumn(label = stringResource(R.string.exam_papers_summary_total_papers), value = totalPapersLabel)
                StatColumn(label = stringResource(R.string.exam_papers_summary_students), value = studentsLabel)
                Spacer(modifier = Modifier.weight(1f))
                StatusPill(text = statusPillLabel)
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = scheme.onPrimary.copy(alpha = 0.85f),
            fontSize = 12.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.onPrimary
        )
    }
}

@Composable
private fun StatusPill(text: String) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = scheme.onPrimary.copy(alpha = 0.18f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PaperCard(
    paper: ExamPaperCardUiState,
    onOpenPaper: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val corner = RoundedCornerShape(16.dp)
    val isUpcoming = paper.status == ExamPaperAttendanceStatus.Upcoming

    val borderColor = when (paper.status) {
        ExamPaperAttendanceStatus.Completed -> scheme.outlineVariant.copy(alpha = 0.85f)
        ExamPaperAttendanceStatus.Active -> scheme.primary.copy(alpha = 0.35f)
        ExamPaperAttendanceStatus.Upcoming -> scheme.outlineVariant.copy(alpha = 0.75f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(corner)
            .background(Color.Transparent)
            .clickable(onClick = onOpenPaper),
        shape = corner,
        color = scheme.surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconCircle(iconKind = paper.iconKind, status = paper.status)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = paper.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        )
                        Text(
                            text = paper.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
                StatusPillForCard(status = paper.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
                RowStat(icon = Icons.Outlined.Schedule, text = paper.timeLabel, tint = scheme.onSurfaceVariant)
                RowStat(
                    icon = Icons.Outlined.Groups,
                    text = paper.groupOrLocationLabel,
                    tint = scheme.onSurfaceVariant
                )
            }

            if (paper.status == ExamPaperAttendanceStatus.Active && paper.primaryActionLabel != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = scheme.primary,
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = paper.primaryActionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenPaper)
                            .padding(vertical = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPillForCard(status: ExamPaperAttendanceStatus) {
    val scheme = MaterialTheme.colorScheme
    return when (status) {
        ExamPaperAttendanceStatus.Completed -> {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = scheme.primary.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.2f))
            ) {
                Text(
                    text = stringResource(R.string.exam_papers_status_completed),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        ExamPaperAttendanceStatus.Active -> {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = scheme.primary
            ) {
                Text(
                    text = stringResource(R.string.exam_papers_status_active),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        ExamPaperAttendanceStatus.Upcoming -> {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = scheme.surfaceVariant
            ) {
                Text(
                    text = stringResource(R.string.exam_papers_status_upcoming),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun IconCircle(iconKind: ExamPaperIconKind, status: ExamPaperAttendanceStatus) {
    val scheme = MaterialTheme.colorScheme
    val iconTint = when (status) {
        ExamPaperAttendanceStatus.Completed,
        ExamPaperAttendanceStatus.Active -> scheme.primary
        ExamPaperAttendanceStatus.Upcoming -> scheme.onSurfaceVariant
    }
    val bg = when (status) {
        ExamPaperAttendanceStatus.Completed,
        ExamPaperAttendanceStatus.Active -> scheme.primary.copy(alpha = 0.10f)
        ExamPaperAttendanceStatus.Upcoming -> scheme.surfaceVariant
    }

    val icon = when (iconKind) {
        ExamPaperIconKind.Description -> Icons.Outlined.Description
        ExamPaperIconKind.EditNote -> Icons.Outlined.EditNote
        ExamPaperIconKind.Science -> Icons.Outlined.Science
    }

    Surface(
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun RowStat(icon: ImageVector, text: String, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = tint,
            fontWeight = FontWeight.Medium
        )
    }
}

