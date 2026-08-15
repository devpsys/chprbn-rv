package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.components.LoadingState

/**
 * Filter-label constant that means "no attendance filter applied". Kept in
 * sync with `ExamCandidatesUiState.placeholder()` and `ExamCandidatesViewModel`.
 */
private const val FILTER_ALL_LABEL = "All"

// Screen-local design tokens. Kept inline (rather than promoted to
// `core/designsystem/Color.kt`) because they're accent shades used only
// by the exam-candidates card and its status pill / warning button;
// no other surface has asked for them yet.
private val StatusEmeraldBg = Color(0xFFD1FAE5)
private val StatusEmeraldText = Color(0xFF065F46)
private val StatusEmeraldDot = Color(0xFF10B981)
private val StatusSlateBg = Color(0xFFF1F5F9)
private val StatusSlateText = Color(0xFF475569)
private val StatusSlateDot = Color(0xFF94A3B8)
private val StatusFlaggedBg = Color(0xFFFEE2E2)
private val StatusFlaggedText = Color(0xFF991B1B)
private val StatusFlaggedDot = Color(0xFFEF4444)
private val WarningOrangeBg = Color(0xFFFFF7ED)
private val WarningOrangeText = Color(0xFFC2410C)
private val WarningOrangeBorder = Color(0xFFFED7AA)
private val NeutralButtonBg = Color(0xFFF8FAFC)
private val NeutralButtonBorder = Color(0xFFE2E8F0)
private val NeutralButtonText = Color(0xFF334155)
private val SubtleGrey = Color(0xFF94A3B8)

@Composable
fun ExamCandidatesContent(
    uiState: ExamCandidatesUiState,
    onBack: () -> Unit,
    onAddRemark: (candidateId: String) -> Unit,
    onViewProfile: (candidateId: String) -> Unit,
    onQueryChange: (String) -> Unit = {},
    onFilterSelected: (String) -> Unit = {},
    onFiltersClick: () -> Unit = {},
    onCandidatesTab: () -> Unit = {},
    onReportsTab: () -> Unit = {},
    onSettingsTab: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = scheme.background,
        topBar = {
            ExamCandidatesTopBar(
                onBack = onBack,
                onFiltersClick = onFiltersClick,
            )
        },
    ) { paddingValues ->
        if (!uiState.hasLoaded) {
            LoadingState(modifier = Modifier.padding(paddingValues))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(scheme.surface),
        ) {
            ExamCandidatesSearch(
                query = uiState.searchQuery,
                onQueryChange = onQueryChange,
            )
            ExamFilterChips(
                filterLabels = uiState.filterLabels,
                activeFilterLabel = uiState.activeFilterLabel,
                onFilterSelected = onFilterSelected,
            )
            if (uiState.candidates.isEmpty()) {
                ExamCandidatesEmptyState(
                    query = uiState.searchQuery,
                    activeFilterLabel = uiState.activeFilterLabel,
                    onClearSearch = { onQueryChange("") },
                    onShowAll = { onFilterSelected(FILTER_ALL_LABEL) },
                    modifier = Modifier
                        .weight(1f)
                        .background(scheme.background),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(scheme.background),
                    contentPadding = PaddingValues(vertical = 0.dp),
                ) {
                    items(uiState.candidates) { candidate ->
                        ExamCandidateCard(
                            candidate = candidate,
                            onAddRemark = { onAddRemark(candidate.candidateId) },
                            onViewProfile = { onViewProfile(candidate.candidateId) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamCandidatesTopBar(
    onBack: () -> Unit,
    onFiltersClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.6f)),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.exam_candidates_action_back),
                    tint = scheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = stringResource(R.string.exam_candidates_header_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center,
            )
            IconButton(onClick = onFiltersClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = stringResource(R.string.exam_candidates_action_filters),
                    tint = scheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun ExamCandidatesSearch(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    var value by remember(query) { mutableStateOf(query) }

    TextField(
        value = value,
        onValueChange = {
            value = it
            onQueryChange(it)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp)),
        singleLine = true,
        placeholder = { Text(stringResource(R.string.exam_candidates_search_placeholder)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = SubtleGrey,
                modifier = Modifier.size(22.dp),
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = NeutralButtonBg,
            unfocusedContainerColor = NeutralButtonBg,
            focusedIndicatorColor = scheme.primary,
            unfocusedIndicatorColor = Color.Transparent,
            focusedPlaceholderColor = SubtleGrey,
            unfocusedPlaceholderColor = SubtleGrey,
        ),
    )
}

@Composable
private fun ExamFilterChips(
    filterLabels: List<String>,
    activeFilterLabel: String,
    onFilterSelected: (String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        filterLabels.forEach { label ->
            val selected = label == activeFilterLabel
            Surface(
                modifier = Modifier.clickable { onFilterSelected(label) },
                shape = RoundedCornerShape(999.dp),
                color = if (selected) scheme.primary else StatusSlateBg,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) scheme.onPrimary else StatusSlateText,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun ExamCandidateCard(
    candidate: ExamCandidateUiState,
    onAddRemark: () -> Unit,
    onViewProfile: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val avatarShape = RoundedCornerShape(14.dp)
    val statusVisual = statusVisualFor(candidate.statusPillLabel)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    CandidateAvatar(
                        avatarUrl = candidate.avatarUrl,
                        shape = avatarShape,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = candidate.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface,
                        )
                        Text(
                            text = candidate.idLabel,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = SubtleGrey,
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    StatusPill(label = candidate.statusPillLabel, visual = statusVisual)
                    Text(
                        text = candidate.statusSubLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = SubtleGrey,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RemarkButton(
                    remarkCount = candidate.remarkCount,
                    onClick = onAddRemark,
                    modifier = Modifier.weight(1f),
                )
                CardActionButton(
                    label = stringResource(R.string.exam_candidates_action_view_profile),
                    icon = Icons.Outlined.Visibility,
                    onClick = onViewProfile,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CandidateAvatar(avatarUrl: String?, shape: RoundedCornerShape) {
    if (avatarUrl.isNullOrBlank()) {
        // Bundled fallback — never hotlink a stock photo (E11 audit fix).
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(shape)
                .background(PrimaryGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(36.dp),
            )
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(shape),
        )
    }
}

internal data class StatusVisual(val bg: Color, val text: Color, val dot: Color)

internal fun statusVisualFor(label: String): StatusVisual = when {
    label.equals("Signed In", ignoreCase = true) ->
        StatusVisual(StatusEmeraldBg, StatusEmeraldText, StatusEmeraldDot)

    label.equals("Flagged", ignoreCase = true) ->
        StatusVisual(StatusFlaggedBg, StatusFlaggedText, StatusFlaggedDot)

    else ->
        // Signed Out / Pending / anything unrecognised — neutral slate.
        StatusVisual(StatusSlateBg, StatusSlateText, StatusSlateDot)
}

@Composable
internal fun StatusPill(label: String, visual: StatusVisual) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = visual.bg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(visual.dot),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = visual.text,
            )
        }
    }
}

/**
 * Card action button used for "Add Remark" / "N Remark" / "View Profile".
 * When [remarkCount] > 0, renders in the warning-orange variant with a
 * filled warning icon (design spec — signals the officer already has
 * flags on this candidate).
 */
@Composable
private fun RemarkButton(
    remarkCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasRemarks = remarkCount > 0
    val bg = if (hasRemarks) WarningOrangeBg else NeutralButtonBg
    val border = if (hasRemarks) WarningOrangeBorder else NeutralButtonBorder
    val fg = if (hasRemarks) WarningOrangeText else NeutralButtonText
    val label = if (hasRemarks) {
        stringResource(R.string.exam_candidates_remark_count_format, remarkCount)
    } else {
        stringResource(R.string.exam_candidates_action_add_remark)
    }
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            Icon(
                imageVector = if (hasRemarks) Icons.Filled.Warning else Icons.AutoMirrored.Outlined.NoteAdd,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = fg,
            )
        }
    }
}

@Composable
private fun CardActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = NeutralButtonBg,
        border = BorderStroke(1.dp, NeutralButtonBorder),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeutralButtonText,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = NeutralButtonText,
            )
        }
    }
}

/**
 * Rendered in place of the candidate list when the filter + search combine
 * to zero matches (or when the roster itself is genuinely empty). Copy adapts
 * to which of the two knobs is narrowing:
 *
 * - Search only: `No candidate matches "…"` + `Clear search` CTA.
 * - Filter only: `No candidates are in the "…" state right now.` + `Show all` CTA.
 * - Both: combined subtitle + both CTAs.
 * - Neither: fallback `No candidates are assigned to this paper yet.` (no CTA).
 */
@Composable
private fun ExamCandidatesEmptyState(
    query: String,
    activeFilterLabel: String,
    onClearSearch: () -> Unit,
    onShowAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val trimmedQuery = query.trim()
    val hasSearch = trimmedQuery.isNotEmpty()
    val hasFilter = activeFilterLabel.isNotBlank() &&
            !activeFilterLabel.equals(FILTER_ALL_LABEL, ignoreCase = true)

    val subtitle = when {
        hasSearch && hasFilter -> stringResource(
            R.string.exam_candidates_empty_subtitle_search_filter,
            trimmedQuery,
            activeFilterLabel,
        )

        hasSearch -> stringResource(
            R.string.exam_candidates_empty_subtitle_search,
            trimmedQuery,
        )

        hasFilter -> stringResource(
            R.string.exam_candidates_empty_subtitle_filter,
            activeFilterLabel,
        )

        else -> stringResource(R.string.exam_candidates_empty_subtitle_none)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Green-tinted badge with the same PersonSearch glyph as the
        // verified-list empty state so the whole app reads with one
        // "no-results" language.
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(PrimaryGreen.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonSearch,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(40.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.exam_candidates_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = SubtleGrey,
            textAlign = TextAlign.Center,
        )
        if (hasSearch || hasFilter) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (hasSearch) {
                    EmptyStateAction(
                        label = stringResource(R.string.exam_candidates_empty_action_clear_search),
                        onClick = onClearSearch,
                    )
                }
                if (hasFilter) {
                    EmptyStateAction(
                        label = stringResource(R.string.exam_candidates_empty_action_show_all),
                        onClick = onShowAll,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateAction(
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = NeutralButtonBg,
        border = BorderStroke(1.dp, NeutralButtonBorder),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = NeutralButtonText,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}
