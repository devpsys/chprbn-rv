package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun AssessmentPaperDetailContent(
    modifier: Modifier = Modifier,
    uiState: AssessmentPaperDetailUiState,
    onBack: () -> Unit = {},
    onShare: () -> Unit = {},
    onMore: () -> Unit = {},
    onCandidateClick: (CandidateRowUiState) -> Unit = {},
    onViewFullDirectory: () -> Unit = {},
    onScanQr: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = scheme.background,
        topBar = {
            PaperDetailTopBar(onBack = onBack, onShare = onShare, onMore = onMore)
        },
        floatingActionButton = {
            ScanQrFab(onClick = onScanQr)
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
            item {
                PaperHeroCard(
                    paperTitle = uiState.paperTitle,
                    heroImageUrl = uiState.heroImageUrl,
                )
            }
            item {
                CheckedInProgressCard(
                    progressFraction = uiState.progressFraction,
                    checkedIn = uiState.checkedInCount,
                    total = uiState.totalCount,
                )
            }
//            item {
//                FacilityAndHallRow(
//                    facilityName = uiState.facilityName,
//                    facilityAddress = uiState.facilityAddress,
//                    hallName = uiState.hallName,
//                    hallAddress = uiState.hallAddress,
//                )
//            }
            item {
                CandidateDirectoryCard(
                    candidates = uiState.candidates,
                    totalCount = uiState.totalCount,
                    onCandidateClick = onCandidateClick,
                    onViewFullDirectory = onViewFullDirectory,
                )
            }
            // Trailing space sized to clear the Scan QR Code FAB (~56dp tall
            // + 16dp Scaffold margin + breathing room) so the last list item
            // can be fully scrolled into view above the FAB.
            item { Spacer(modifier = Modifier.height(96.dp)) }
        }
    }
}

@Composable
private fun PaperDetailTopBar(
    onBack: () -> Unit,
    onShare: () -> Unit,
    onMore: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        color = scheme.surface,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, scheme.outlineVariant),
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
                        contentDescription = stringResource(R.string.assessment_paper_detail_action_back),
                        tint = scheme.onSurface,
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.assessment_paper_detail_overline),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = scheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.assessment_paper_detail_header_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun PaperHeroCard(
    paperTitle: String,
    heroImageUrl: String?,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(224.dp),
        shape = RoundedCornerShape(16.dp),
        color = scheme.primaryContainer,
        shadowElevation = 1.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!heroImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(heroImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.assessment_paper_detail_hero_image_cd),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            // Bottom-up gradient tinted with the brand primary so the title
            // stays readable. Solid primary at the bottom; transparent at
            // the top.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                scheme.primary.copy(alpha = 0.6f),
                            ),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = scheme.primary,
                ) {
                    Text(
                        text = stringResource(R.string.assessment_paper_detail_status_active),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onPrimary,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = paperTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    // Hard white reads well against the primary-tinted
                    // gradient regardless of the underlying image.
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun CheckedInProgressCard(
    progressFraction: Float,
    checkedIn: Int,
    total: Int,
) {
    val scheme = MaterialTheme.colorScheme
    val pct = (progressFraction.coerceIn(0f, 1f) * 100).toInt()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
        shadowElevation = 1.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(scheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Text(
                        text = stringResource(R.string.assessment_paper_detail_progress_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                    )
                }
                Text(
                    text = stringResource(
                        R.string.assessment_paper_detail_progress_percent_format,
                        pct,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(scheme.outlineVariant),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(scheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(999.dp))
                            .background(scheme.primary),
                    )
                }
                Text(
                    text = stringResource(
                        R.string.assessment_paper_detail_progress_count_format,
                        checkedIn,
                        total,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FacilityAndHallRow(
    facilityName: String,
    facilityAddress: String,
    hallName: String,
    hallAddress: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InfoCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.MedicalServices,
            iconCd = stringResource(R.string.assessment_paper_detail_facility_icon_cd),
            label = stringResource(R.string.assessment_paper_detail_facility_label),
            title = facilityName,
            subtitle = facilityAddress,
        )
        InfoCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.MeetingRoom,
            iconCd = stringResource(R.string.assessment_paper_detail_hall_icon_cd),
            label = stringResource(R.string.assessment_paper_detail_hall_label),
            title = hallName,
            subtitle = hallAddress,
        )
    }
}

@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconCd: String,
    label: String,
    title: String,
    subtitle: String,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconCd,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CandidateDirectoryCard(
    candidates: List<CandidateRowUiState>,
    totalCount: Int,
    onCandidateClick: (CandidateRowUiState) -> Unit,
    onViewFullDirectory: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
        shadowElevation = 1.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = null,
                        tint = scheme.onSurface,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = stringResource(R.string.assessment_paper_detail_directory_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                    )
                }
                Text(
                    text = stringResource(
                        R.string.assessment_paper_detail_directory_total_format,
                        totalCount,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(scheme.outlineVariant),
            )
            candidates.forEachIndexed { index, candidate ->
                CandidateRow(
                    candidate = candidate,
                    onClick = { onCandidateClick(candidate) },
                )
                if (index < candidates.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(scheme.outlineVariant),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                TextButton(
                    onClick = onViewFullDirectory,
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.assessment_paper_detail_view_full_directory),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun CandidateRow(
    candidate: CandidateRowUiState,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        // Surface(onClick) so the ripple is clipped to the row's bounds and
        // the row is announced as a single TalkBack node.
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        color = scheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = candidate.initials,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = candidate.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
                Text(
                    text = stringResource(
                        R.string.assessment_paper_detail_directory_id_format,
                        candidate.id,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            CandidateStatusPill(status = candidate.syncStatus)
        }
    }
}

@Composable
private fun CandidateStatusPill(status: CandidateSyncStatus) {
    // Same green/orange convention used by the schedule cards + the rest of
    // the app (SyncScreen, VerifiedListScreen). "Unsynced" here is the
    // not-yet-synchronised state and shares the orange palette with "Pending".
    val (containerColor, contentColor, labelRes) = when (status) {
        CandidateSyncStatus.Synced -> Triple(
            Color(0xFFE8F5E9),
            SuccessGreen,
            R.string.assessment_paper_detail_directory_status_synced,
        )

        CandidateSyncStatus.Unsynced -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFEF6C00),
            R.string.assessment_paper_detail_directory_status_unsynced,
        )
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
    ) {
        Text(
            text = stringResource(labelRes),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
    }
}

@Composable
private fun ScanQrFab(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    // Same Extended FAB pattern as ExamPaperContent — primary container,
    // text + small inset icon tile — so the action reads identically across
    // the exam and assessment surfaces.
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = scheme.primary,
        contentColor = scheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.assessment_paper_detail_action_scan_qr),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = scheme.onPrimary.copy(alpha = 0.2f),
        ) {
            Icon(
                imageVector = Icons.Filled.QrCodeScanner,
                contentDescription = null,
                tint = scheme.onPrimary,
                modifier = Modifier
                    .padding(8.dp)
                    .size(20.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun AssessmentPaperDetailContentPreview() {
    ChprbnTheme {
        AssessmentPaperDetailContent(
            uiState = AssessmentPaperDetailUiState(
                paperTitle = "Regulatory Medical Paper A-14",
                statusLabel = "Active",
                progressFraction = 0.75f,
                checkedInCount = 90,
                totalCount = 120,
                facilityName = "St. Jude Metropolitan Hospital",
                facilityAddress = "Regulatory District 04, North Campus",
                hallName = "Auditorium C-12",
                hallAddress = "3rd Floor, West Wing Elevator",
                candidates = listOf(
                    CandidateRowUiState(
                        id = "RE-40112",
                        initials = "JS",
                        fullName = "Jonathan Smith",
                        syncStatus = CandidateSyncStatus.Synced,
                    ),
                    CandidateRowUiState(
                        id = "RE-40115",
                        initials = "AM",
                        fullName = "Anita Meyer",
                        syncStatus = CandidateSyncStatus.Unsynced,
                    ),
                ),
                heroImageUrl = null,
            ),
        )
    }
}
