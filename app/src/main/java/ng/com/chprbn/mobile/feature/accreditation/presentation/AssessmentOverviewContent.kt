package ng.com.chprbn.mobile.feature.accreditation.presentation

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.ErrorRed
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.SuccessGreen
import ng.com.chprbn.mobile.core.designsystem.WarningYellow
import ng.com.chprbn.mobile.core.designsystem.components.LoadingState
import ng.com.chprbn.mobile.core.designsystem.components.PrimaryButton

@Composable
fun AssessmentOverviewContent(
    modifier: Modifier = Modifier,
    uiState: AssessmentOverviewUiState,
    onBack: () -> Unit = {},
    onSectionClick: (institutionId: String, sectionKey: String) -> Unit = { _, _ -> },
    onContinueToChecklist: (institutionId: String) -> Unit = {},
    onSaveAllDrafts: (institutionId: String) -> Unit = {},
) {
    val success = uiState as? AssessmentOverviewUiState.Success

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AssessmentFlowTopBar(title = stringResource(R.string.accreditation_overview_title), onBack = onBack) },
        bottomBar = {
            if (success != null) {
                OverviewBottomBar(
                    allSectionsSaved = success.data.allSectionsSaved,
                    onContinueToChecklist = {
                        onContinueToChecklist(success.data.institutionId)
                    },
                    onSaveAllDrafts = { onSaveAllDrafts(success.data.institutionId) },
                )
            }
        },
    ) { paddingValues ->
        when (uiState) {
            AssessmentOverviewUiState.Loading -> LoadingState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )

            is AssessmentOverviewUiState.Error -> OverviewErrorContent(
                modifier = Modifier.padding(paddingValues),
                message = uiState.message,
                onBack = onBack,
            )

            is AssessmentOverviewUiState.Success -> OverviewBody(
                modifier = Modifier.padding(paddingValues),
                data = uiState.data,
                onSectionClick = { sectionKey ->
                    onSectionClick(uiState.data.institutionId, sectionKey)
                },
            )
        }
    }
}

@Composable
internal fun AssessmentFlowTopBar(
    title: String,
    onBack: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.app_top_bar_back_action),
                        tint = PrimaryGreen,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = stringResource(R.string.accreditation_profile_officer_avatar_cd),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun OverviewBottomBar(
    allSectionsSaved: Boolean,
    onContinueToChecklist: () -> Unit,
    onSaveAllDrafts: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onContinueToChecklist,
                enabled = allSectionsSaved,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                    disabledContainerColor = PrimaryGreen.copy(alpha = 0.35f),
                    disabledContentColor = Color.White.copy(alpha = 0.8f),
                ),
            ) {
                Text(
                    text = stringResource(R.string.accreditation_overview_continue_checklist),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
            if (!allSectionsSaved) {
                Text(
                    text = stringResource(R.string.accreditation_overview_continue_locked_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            OutlinedButton(
                onClick = onSaveAllDrafts,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = AccrdCardBorder,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudUpload,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.accreditation_overview_save_all_drafts),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreen,
                )
            }
        }
    }
}

@Composable
private fun OverviewBody(
    data: AssessmentOverviewData,
    onSectionClick: (sectionKey: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        item { InstitutionProgressCard(data = data) }
        item {
            Text(
                text = stringResource(R.string.accreditation_overview_modules_heading),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        items(data.sections, key = { it.key.key }) { section ->
            SectionSummaryRow(
                section = section,
                onClick = { onSectionClick(section.key.key) },
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun InstitutionProgressCard(data: AssessmentOverviewData) {
    val scheme = MaterialTheme.colorScheme
    val percent = (data.progressFraction * 100f).toInt()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        border = AccrdCardBorder,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.accreditation_overview_audit_label),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurfaceVariant,
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PrimaryGreen.copy(alpha = 0.1f),
                ) {
                    Text(
                        text = data.cycleLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = data.institutionName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
            )
            Text(
                text = stringResource(
                    R.string.accreditation_overview_facility_meta_format,
                    data.facilityCode,
                    data.locationLabel,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = PrimaryGreen.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.accreditation_overview_percent_format, percent),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
                Text(
                    text = stringResource(
                        R.string.accreditation_overview_saved_count_format,
                        data.savedCount,
                        data.sections.size,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreen,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { data.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryGreen,
                trackColor = PrimaryGreen.copy(alpha = 0.12f),
            )
        }
    }
}

@Composable
private fun SectionSummaryRow(
    section: SsqSectionSummaryUiState,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val saved = section.status == SectionDraftStatus.SavedLocally ||
        section.status == SectionDraftStatus.PendingSync ||
        section.status == SectionDraftStatus.Synced
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        border = AccrdCardBorder,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (saved) PrimaryGreen.copy(alpha = 0.12f) else scheme.surfaceVariant,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = section.key.key,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (saved) PrimaryGreen else scheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${section.codeLabel}: ${section.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = section.rangeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = section.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            SectionStatusChip(status = section.status)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
internal fun SectionStatusChip(
    status: SectionDraftStatus,
    modifier: Modifier = Modifier,
) {
    val (labelRes, tint, bg, icon) = statusChipVisuals(status)
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
private fun statusChipVisuals(
    status: SectionDraftStatus,
): Quad<Int, Color, Color, ImageVector> = when (status) {
    SectionDraftStatus.NotStarted -> Quad(
        R.string.accreditation_section_status_not_started,
        MaterialTheme.colorScheme.onSurfaceVariant,
        MaterialTheme.colorScheme.surfaceVariant,
        Icons.Outlined.RadioButtonUnchecked,
    )
    SectionDraftStatus.InProgress -> Quad(
        R.string.accreditation_section_status_in_progress,
        WarningYellow,
        WarningYellow.copy(alpha = 0.12f),
        Icons.Outlined.Pending,
    )
    SectionDraftStatus.SavedLocally -> Quad(
        R.string.accreditation_section_status_saved_locally,
        SuccessGreen,
        SuccessGreen.copy(alpha = 0.12f),
        Icons.Filled.CheckCircle,
    )
    SectionDraftStatus.PendingSync -> Quad(
        R.string.accreditation_section_status_pending_sync,
        Color(0xFF0277BD),
        Color(0xFF0277BD).copy(alpha = 0.12f),
        Icons.Filled.CloudUpload,
    )
    SectionDraftStatus.Synced -> Quad(
        R.string.accreditation_section_status_synced,
        SuccessGreen,
        SuccessGreen.copy(alpha = 0.12f),
        Icons.Filled.CloudDone,
    )
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun OverviewErrorContent(
    message: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.accreditation_overview_error_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = ErrorRed,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        PrimaryButton(
            text = stringResource(R.string.accreditation_profile_action_back_to_hub),
            onClick = onBack,
        )
    }
}

@Preview(showBackground = true, widthDp = 411)
@Composable
private fun AssessmentOverviewContentPreview() {
    ChprbnTheme {
        AssessmentOverviewContent(uiState = AssessmentOverviewUiState.placeholder())
    }
}
