package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items as columnItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.automirrored.filled.ViewList
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme

/** Warning shade for low-score candidates (mirrors the design's `secondary` token). */
private val LowScoreColor = Color(0xFFEF6C00)
private val LowScoreContainerColor = Color(0xFFFFF3E0)

@Composable
fun AssessmentCandidatesContent(
    modifier: Modifier = Modifier,
    uiState: AssessmentCandidatesUiState,
    onBack: () -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    onViewModeChange: (CandidatesViewMode) -> Unit = {},
    onCandidateClick: (CandidateCardUiState) -> Unit = {},
    onAddRemark: (CandidateCardUiState) -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxSize(),
        color = scheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CandidatesTopBar(
                totalCount = uiState.totalCount,
                onBack = onBack,
            )
            SearchAndToggleRow(
                query = uiState.query,
                onQueryChange = onQueryChange,
                viewMode = uiState.viewMode,
                onViewModeChange = onViewModeChange,
            )
            when {
                uiState.candidates.isEmpty() -> {
                    EmptyState(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                    )
                }

                uiState.viewMode == CandidatesViewMode.List -> {
                    CandidatesList(
                        candidates = uiState.candidates,
                        onCandidateClick = onCandidateClick,
                        onAddRemark = onAddRemark,
                        modifier = Modifier.weight(1f),
                    )
                }

                else -> {
                    CandidatesGrid(
                        candidates = uiState.candidates,
                        onCandidateClick = onCandidateClick,
                        onAddRemark = onAddRemark,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CandidatesTopBar(
    totalCount: Int,
    onBack: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        color = scheme.primary,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.assessment_candidates_action_back),
                        tint = scheme.onPrimary,
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.assessment_candidates_header_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(
                            R.string.assessment_candidates_total_format,
                            totalCount,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onPrimary.copy(alpha = 0.85f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchAndToggleRow(
    query: String,
    onQueryChange: (String) -> Unit,
    viewMode: CandidatesViewMode,
    onViewModeChange: (CandidatesViewMode) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        color = scheme.primary,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.assessment_candidates_search_placeholder),
                        color = scheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = stringResource(R.string.assessment_candidates_search_action_cd),
                        tint = scheme.primary,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = scheme.surface,
                    unfocusedContainerColor = scheme.surface,
                    focusedIndicatorColor = scheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = scheme.primary,
                ),
            )
            ViewModeToggle(
                viewMode = viewMode,
                onViewModeChange = onViewModeChange,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

@Composable
private fun ViewModeToggle(
    viewMode: CandidatesViewMode,
    onViewModeChange: (CandidatesViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = scheme.onPrimary.copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ViewModeButton(
                selected = viewMode == CandidatesViewMode.List,
                icon = Icons.AutoMirrored.Filled.ViewList,
                contentDescription = stringResource(R.string.assessment_candidates_view_toggle_list_cd),
                onClick = { onViewModeChange(CandidatesViewMode.List) },
            )
            ViewModeButton(
                selected = viewMode == CandidatesViewMode.Grid,
                icon = Icons.Filled.GridView,
                contentDescription = stringResource(R.string.assessment_candidates_view_toggle_grid_cd),
                onClick = { onViewModeChange(CandidatesViewMode.Grid) },
            )
        }
    }
}

@Composable
private fun ViewModeButton(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = if (selected) scheme.onPrimary else Color.Transparent,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (selected) scheme.primary else scheme.onPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun CandidatesList(
    candidates: List<CandidateCardUiState>,
    onCandidateClick: (CandidateCardUiState) -> Unit,
    onAddRemark: (CandidateCardUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        columnItems(
            items = candidates,
            key = { it.id },
        ) { candidate ->
            CandidateListRow(
                candidate = candidate,
                onClick = { onCandidateClick(candidate) },
                onAddRemark = { onAddRemark(candidate) },
            )
        }
    }
}

@Composable
private fun CandidatesGrid(
    candidates: List<CandidateCardUiState>,
    onCandidateClick: (CandidateCardUiState) -> Unit,
    onAddRemark: (CandidateCardUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 24.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        gridItems(
            items = candidates,
            key = { it.id },
        ) { candidate ->
            CandidateGridCard(
                candidate = candidate,
                onClick = { onCandidateClick(candidate) },
                onAddRemark = { onAddRemark(candidate) },
            )
        }
    }
}

@Composable
private fun CandidateListRow(
    candidate: CandidateCardUiState,
    onClick: () -> Unit,
    onAddRemark: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val accentColor = if (candidate.level == ScoreLevel.Low) LowScoreColor else scheme.primary
    val accentContainerColor = if (candidate.level == ScoreLevel.Low) {
        LowScoreContainerColor
    } else {
        scheme.primary.copy(alpha = 0.1f)
    }
    Surface(
        // Surface(onClick) so the ripple is clipped to the rounded shape.
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(20.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CandidateAvatar(
                photoUrl = candidate.photoUrl,
                fullName = candidate.fullName,
                size = 64.dp,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = candidate.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = candidate.indexingNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            AddRemarkButton(
                onClick = onAddRemark,
                tint = accentColor,
                background = scheme.surfaceVariant.copy(alpha = 0.6f),
                useBubbleIcon = false,
            )
            ScoreBox(
                score = candidate.score,
                accentColor = accentColor,
                containerColor = accentContainerColor,
            )
        }
    }
}

@Composable
private fun CandidateGridCard(
    candidate: CandidateCardUiState,
    onClick: () -> Unit,
    onAddRemark: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val accentColor = if (candidate.level == ScoreLevel.Low) LowScoreColor else scheme.primary
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(16.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Top accent strip — primary or secondary based on level.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(accentColor),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp, start = 12.dp, end = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    CandidateAvatar(
                        photoUrl = candidate.photoUrl,
                        fullName = candidate.fullName,
                        size = 80.dp,
                    )
                    // Score badge attached to the bottom-right of the avatar.
                    Surface(
                        modifier = Modifier
                            .offset(x = 4.dp, y = 4.dp)
                            .heightIn(min = 28.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor,
                        border = BorderStroke(2.dp, scheme.surface),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.assessment_candidates_score_label),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onPrimary.copy(alpha = 0.85f),
                            )
                            Text(
                                text = candidate.score.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                color = scheme.onPrimary,
                            )
                        }
                    }
                }
                Text(
                    text = candidate.fullName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = candidate.indexingNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            // Top-right corner add-remark button (uses chat bubble per the
            // grid design).
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                AddRemarkButton(
                    onClick = onAddRemark,
                    tint = accentColor,
                    background = scheme.surfaceVariant.copy(alpha = 0.6f),
                    useBubbleIcon = false,
                )
            }
        }
    }
}

@Composable
private fun CandidateAvatar(
    photoUrl: String?,
    fullName: String,
    size: androidx.compose.ui.unit.Dp,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(scheme.surfaceVariant)
            .border(1.dp, scheme.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = fullName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = fullName,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(size / 2),
            )
        }
    }
}

@Composable
private fun AddRemarkButton(
    onClick: () -> Unit,
    tint: Color,
    background: Color,
    useBubbleIcon: Boolean,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = if (useBubbleIcon) CircleShape else RoundedCornerShape(10.dp),
        color = background,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (useBubbleIcon) {
                    Icons.Outlined.ChatBubbleOutline
                } else {
                    Icons.Filled.RateReview
                },
                contentDescription = stringResource(R.string.assessment_candidates_action_add_remark_cd),
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ScoreBox(
    score: Int,
    accentColor: Color,
    containerColor: Color,
) {
    Surface(
        modifier = Modifier.size(width = 56.dp, height = 64.dp),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.assessment_candidates_score_label),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor.copy(alpha = 0.7f),
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = accentColor,
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = scheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(2.dp, scheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp, horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(
                    imageVector = Icons.Outlined.PersonSearch,
                    contentDescription = null,
                    tint = scheme.outline.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Assignment,
                    contentDescription = null,
                    tint = scheme.outline.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp),
                )
                Icon(
                    imageVector = Icons.Filled.Vaccines,
                    contentDescription = null,
                    tint = scheme.outline.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.assessment_candidates_empty_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.assessment_candidates_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(280.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun AssessmentCandidatesContentListPreview() {
    ChprbnTheme {
        AssessmentCandidatesContent(
            uiState = AssessmentCandidatesUiState(
                totalCount = 150,
                viewMode = CandidatesViewMode.List,
                candidates = previewCandidates(),
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun AssessmentCandidatesContentGridPreview() {
    ChprbnTheme {
        AssessmentCandidatesContent(
            uiState = AssessmentCandidatesUiState(
                totalCount = 150,
                viewMode = CandidatesViewMode.Grid,
                candidates = previewCandidates(),
            ),
        )
    }
}

private fun previewCandidates() = listOf(
    CandidateCardUiState(
        id = "EX-2024-0092",
        indexingNumber = "EX-2024-0092",
        fullName = "Johnathan Doe",
        photoUrl = null,
        score = 82,
        level = ScoreLevel.Normal,
    ),
    CandidateCardUiState(
        id = "EX-2024-0105",
        indexingNumber = "EX-2024-0105",
        fullName = "Sarah Jenkins",
        photoUrl = null,
        score = 91,
        level = ScoreLevel.Normal,
    ),
    CandidateCardUiState(
        id = "EX-2024-0088",
        indexingNumber = "EX-2024-0088",
        fullName = "Michael Abiodun",
        photoUrl = null,
        score = 45,
        level = ScoreLevel.Low,
    ),
)
