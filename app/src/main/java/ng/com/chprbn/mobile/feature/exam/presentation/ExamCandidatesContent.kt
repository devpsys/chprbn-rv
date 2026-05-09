package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ng.com.chprbn.mobile.R
import coil.request.ImageRequest
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState

@Composable
fun ExamCandidatesContent(
    uiState: ExamCandidatesUiState,
    onBack: () -> Unit,
    onAddRemark: (candidateId: String) -> Unit,
    onViewProfile: (candidateId: String) -> Unit,
    onCandidatesTab: () -> Unit = {},
    onReportsTab: () -> Unit = {},
    onSettingsTab: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = scheme.background,
        topBar = {
            ExamCandidatesTopBar(
                onBack = onBack
            )
        },
//        bottomBar = {
//            ExamCandidatesBottomBar(
//                onCandidatesTab = onCandidatesTab,
//                onReportsTab = onReportsTab,
//                onSettingsTab = onSettingsTab
//            )
//        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ExamCandidatesSearch(uiState = uiState, onQueryChange = { })
            ExamFilterChips(
                filterLabels = uiState.filterLabels,
                activeFilterLabel = uiState.activeFilterLabel,
                onFilterSelected = { /* TODO */ }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 0.dp,
                    vertical = 4.dp
                )
            ) {
                items(uiState.candidates) { candidate ->
                    ExamCandidateItem(
                        candidate = candidate,
                        onAddRemark = { onAddRemark(candidate.idLabel) },
                        onViewProfile = { onViewProfile(candidate.idLabel) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamCandidatesTopBar(
    onBack: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        color = scheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            scheme.outlineVariant.copy(alpha = 0.6f)
        ),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.exam_candidates_action_back),
                    tint = scheme.primary,
                    modifier = Modifier.size(24.dp)
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
                textAlign = TextAlign.Center
            )
//            IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(40.dp)) {
//                Icon(
//                    imageVector = Icons.Outlined.Settings,
//                    contentDescription = "Filters",
//                    tint = scheme.primary,
//                    modifier = Modifier.size(24.dp)
//                )
//            }
        }
    }
}

@Composable
private fun ExamCandidatesSearch(
    uiState: ExamCandidatesUiState,
    onQueryChange: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var query by remember(uiState.searchQuery) { mutableStateOf(uiState.searchQuery) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        color = scheme.surfaceVariant.copy(alpha = 0.65f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            scheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
//            Spacer(modifier = Modifier.width(10.dp))
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    onQueryChange(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.exam_candidates_search_placeholder)) },
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun ExamFilterChips(
    filterLabels: List<String>,
    activeFilterLabel: String,
    onFilterSelected: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        filterLabels.forEach { label ->
            val selected = label == activeFilterLabel
            Surface(
                modifier = Modifier.clickable { onFilterSelected(label) },
                shape = RoundedCornerShape(999.dp),
                color = if (selected) scheme.primary else scheme.surfaceVariant.copy(alpha = 0.65f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) scheme.onPrimary else scheme.onSurfaceVariant
                    )
                    if (label != activeFilterLabel) {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            tint = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamCandidateItem(
    candidate: ExamCandidateUiState,
    onAddRemark: () -> Unit,
    onViewProfile: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val avatarShape = RoundedCornerShape(16.dp)
    val isSignedIn = candidate.statusPillLabel.contains("Signed In", ignoreCase = true)

    val pillBg =
        if (isSignedIn) scheme.primary.copy(alpha = 0.14f) else scheme.surfaceVariant.copy(alpha = 0.65f)
    val pillBorder =
        if (isSignedIn) scheme.primary.copy(alpha = 0.25f) else scheme.outlineVariant.copy(alpha = 0.35f)
    val pillText = if (isSignedIn) scheme.primary else scheme.onSurfaceVariant

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            scheme.outlineVariant.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalCandidatesImageContext())
                            .data(candidate.avatarUrl).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(avatarShape)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = candidate.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        )
                        Text(
                            text = candidate.idLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = pillBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, pillBorder)
                    ) {
                        Text(
                            text = candidate.statusPillLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = pillText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Text(
                        text = candidate.statusSubLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    // Surface(onClick) clips the ripple to the rounded shape;
                    // a `.clickable` modifier on the outer chain would draw a
                    // sharp rectangular splash poking outside the corners.
                    onClick = onAddRemark,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = scheme.surfaceVariant.copy(alpha = 0.55f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        scheme.outlineVariant.copy(alpha = 0.25f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NoteAdd,
                            contentDescription = null,
                            tint = scheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.exam_candidates_action_add_remark),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurface
                        )
                    }
                }
                Surface(
                    onClick = onViewProfile,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = scheme.surfaceVariant.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        scheme.outlineVariant.copy(alpha = 0.25f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = null,
                            tint = scheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.exam_candidates_action_view_profile),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamCandidatesBottomBar(
    onCandidatesTab: () -> Unit,
    onReportsTab: () -> Unit,
    onSettingsTab: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        color = scheme.surface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                selected = true,
                icon = Icons.Outlined.Group,
                label = stringResource(R.string.exam_candidates_nav_candidates),
                onClick = onCandidatesTab
            )
            BottomNavItem(
                selected = false,
                icon = Icons.Outlined.BarChart,
                label = stringResource(R.string.exam_candidates_nav_reports),
                onClick = onReportsTab
            )
            BottomNavItem(
                selected = false,
                icon = Icons.Filled.Settings,
                label = stringResource(R.string.exam_candidates_nav_settings),
                onClick = onSettingsTab
            )
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) scheme.primary else scheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) scheme.primary else scheme.onSurfaceVariant
        )
    }
}

// Small helper so this file compiles without needing android Context in every call-site.
@Composable
private fun LocalCandidatesImageContext() = androidx.compose.ui.platform.LocalContext.current

