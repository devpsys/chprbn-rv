package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SyncProblem
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.SuccessGreen

@Composable
fun ExaminationSchedulesContent(
    modifier: Modifier = Modifier,
    uiState: ExaminationSchedulesUiState,
    onBack: () -> Unit = {},
    onScheduleClick: (ScheduleCardUiState) -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = scheme.background,
        topBar = {
            ExaminationSchedulesTopBar(onBack = onBack)
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
//            item { HelpBanner() }
            items(uiState.schedules, key = { it.id }) { schedule ->
                ScheduleCard(
                    schedule = schedule,
                    onClick = { onScheduleClick(schedule) },
                )
            }
            item { DecorativeFooter(imageUrl = uiState.decorativeImageUrl) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ExaminationSchedulesTopBar(
    onBack: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        color = scheme.primary,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.assessment_schedules_action_back),
                        tint = scheme.onPrimary,
                    )
                }
                // Brand-mark tile (white background, primary-tinted icon).
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(scheme.onPrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.MedicalServices,
                        contentDescription = stringResource(R.string.assessment_schedules_brand_logo_cd),
                        tint = scheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.assessment_schedules_header_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = scheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
//            IconButton(onClick = onMore, modifier = Modifier.size(40.dp)) {
//                Icon(
//                    imageVector = Icons.Filled.MoreVert,
//                    contentDescription = stringResource(R.string.assessment_schedules_action_more),
//                    tint = scheme.onPrimary,
//                )
//            }
        }
    }
}

@Composable
private fun HelpBanner() {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, scheme.outlineVariant),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(scheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = stringResource(R.string.assessment_schedules_help_banner),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = scheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.NorthEast,
                contentDescription = stringResource(R.string.assessment_schedules_help_banner_arrow_cd),
                tint = scheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ScheduleCard(
    schedule: ScheduleCardUiState,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    // Status palette mirrors the rest of the app (see SyncScreen / VerifiedListScreen):
    // green shades for Synced, orange shades for Pending.
    val stripColor = when (schedule.syncStatus) {
        ScheduleSyncStatus.Synced -> SuccessGreen
        ScheduleSyncStatus.Pending -> Color(0xFFEF6C00)
    }
    Surface(
        // Use Surface's onClick overload so the ripple is clipped to the
        // same RoundedCornerShape as the card itself — `.clickable` on the
        // outer modifier chain would draw a sharp rectangular splash that
        // pokes outside the rounded corners.
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            // Merge title + date + status into one accessibility node.
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Vertical status strip on the left.
            Box(
                modifier = Modifier
                    .padding(start = 4.dp, top = 16.dp, bottom = 16.dp)
                    .width(6.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(stripColor),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = schedule.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = stringResource(R.string.assessment_schedule_calendar_icon_cd),
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(
                            R.string.assessment_schedule_date_format,
                            schedule.dateLabel,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            ScheduleStatusPill(status = schedule.syncStatus)
            IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.assessment_schedule_open_action),
                    tint = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ScheduleStatusPill(status: ScheduleSyncStatus) {
    // Same green/orange palette the rest of the app uses for sync status.
    // Hex values match SyncScreen + VerifiedListScreen so the chip looks the
    // same across screens regardless of MaterialTheme.colorScheme tuning.
    val (containerColor, contentColor, icon, labelRes) = when (status) {
        ScheduleSyncStatus.Synced -> Quadruple(
            Color(0xFFE8F5E9),
            SuccessGreen,
            Icons.Filled.CheckCircle,
            R.string.assessment_status_synced,
        )

        ScheduleSyncStatus.Pending -> Quadruple(
            Color(0xFFFFF3E0),
            Color(0xFFEF6C00),
            Icons.Filled.SyncProblem,
            R.string.assessment_status_pending,
        )
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun DecorativeFooter(imageUrl: String?) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp),
        // Design uses rounded-2xl (1.5rem ≈ 24dp), more pronounced than the
        // 12dp on the schedule cards above.
        shape = RoundedCornerShape(24.dp),
        color = scheme.primaryContainer,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Layer 1 — background image at 40% alpha, mirroring the design's
            // `mix-blend-overlay opacity-40`. AsyncImage doesn't expose CSS
            // blend modes; alpha gives a close-enough visual on the
            // primary-container base.
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.assessment_decorative_image_cd),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.4f,
                )
            }
            // Layer 2 — soft light-green → light-grey gradient. Provides a
            // readable backdrop for the dark teal text on the left without
            // overwhelming the image, which fades through the lighter grey
            // on the right.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFC8E6C9).copy(alpha = 0.85f),
                                Color(0xFFE0E0E0).copy(alpha = 0.35f),
                            ),
                        ),
                    ),
            )
            // Layer 3 — copy. Hex colors match the design's
            // `text-on-primary-fixed` (#00201c) heading and
            // `text-on-primary-fixed-variant` (#005048) subtitle so contrast
            // against the teal gradient is preserved regardless of theme.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.assessment_decorative_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00201C),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.assessment_decorative_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF005048),
                    // Design uses `max-w-xs` (≈ 20rem / 320dp) so the line
                    // wraps short of the image area on the right.
                    modifier = Modifier.width(320.dp),
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun ExaminationSchedulesContentPreview() {
    ChprbnTheme {
        ExaminationSchedulesContent(
            uiState = ExaminationSchedulesUiState(
                schedules = listOf(
                    ScheduleCardUiState(
                        id = "PE-2024",
                        title = "PE-2024 / Practical Exam",
                        dateLabel = "Oct 24, 2024",
                        syncStatus = ScheduleSyncStatus.Synced,
                    ),
                    ScheduleCardUiState(
                        id = "MD-801",
                        title = "MD-801 / Theory",
                        dateLabel = "Oct 26, 2024",
                        syncStatus = ScheduleSyncStatus.Pending,
                    ),
                ),
                // Preview without a network image — gradient + flat primary
                // container colour stand in.
                decorativeImageUrl = null,
            ),
        )
    }
}
