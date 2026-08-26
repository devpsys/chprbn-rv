package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.CachedRecordEntry
import ng.com.chprbn.mobile.feature.exam.domain.model.RecordType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AmberError = Color(0xFFEF6C00)

@Composable
fun CachedRecordsContent(
    uiState: CachedRecordsUiState,
    onBack: () -> Unit,
    onTabSelected: (CachedRecordsTab) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onRecordTypeFilterToggled: (RecordType) -> Unit,
    onCopyError: (CachedRecordEntry) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = scheme.background,
        topBar = { CachedRecordsTopBar(onBack = onBack) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            CachedRecordsTabRow(
                selected = uiState.selectedTab,
                pendingCount = uiState.pendingCount,
                failedCount = uiState.failedCount,
                onTabSelected = onTabSelected,
            )
            CachedRecordsSearchField(
                query = uiState.searchQuery,
                onQueryChanged = onSearchQueryChanged,
            )
            CachedRecordsFilterChips(
                selected = uiState.recordTypeFilters,
                onToggle = onRecordTypeFilterToggled,
            )
            when {
                uiState.isLoading -> LoadingBody()
                uiState.visibleEntries.isEmpty() -> EmptyBody(tab = uiState.selectedTab)
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items = uiState.visibleEntries, key = { it.id }) { entry ->
                        CachedRecordCard(
                            entry = entry,
                            onLongPress = { onCopyError(entry) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CachedRecordsTopBar(onBack: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(color = scheme.surface, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = scheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.cached_records_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
        }
    }
}

@Composable
private fun CachedRecordsTabRow(
    selected: CachedRecordsTab,
    pendingCount: Int,
    failedCount: Int,
    onTabSelected: (CachedRecordsTab) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    TabRow(
        selectedTabIndex = if (selected == CachedRecordsTab.Pending) 0 else 1,
        containerColor = scheme.surface,
    ) {
        Tab(
            selected = selected == CachedRecordsTab.Pending,
            onClick = { onTabSelected(CachedRecordsTab.Pending) },
            text = {
                Text(
                    text = stringResource(R.string.cached_records_tab_pending_format, pendingCount),
                    fontWeight = FontWeight.SemiBold,
                )
            },
        )
        Tab(
            selected = selected == CachedRecordsTab.Failed,
            onClick = { onTabSelected(CachedRecordsTab.Failed) },
            text = {
                Text(
                    text = stringResource(R.string.cached_records_tab_failed_format, failedCount),
                    fontWeight = FontWeight.SemiBold,
                )
            },
        )
    }
}

@Composable
private fun CachedRecordsSearchField(query: String, onQueryChanged: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        singleLine = true,
        placeholder = { Text(stringResource(R.string.cached_records_search_placeholder)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
    )
}

@Composable
private fun CachedRecordsFilterChips(
    selected: Set<RecordType>,
    onToggle: (RecordType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecordType.entries.forEach { type ->
            val isOn = type in selected
            FilterChip(
                selected = isOn,
                onClick = { onToggle(type) },
                label = { Text(type.displayLabel()) },
                leadingIcon = if (isOn) {
                    { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryGreen.copy(alpha = 0.12f),
                    selectedLabelColor = PrimaryGreen,
                    selectedLeadingIconColor = PrimaryGreen,
                ),
            )
        }
    }
}

@Composable
private fun LoadingBody() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.CircularProgressIndicator(color = PrimaryGreen)
    }
}

@Composable
private fun EmptyBody(tab: CachedRecordsTab) {
    val scheme = MaterialTheme.colorScheme
    val (icon, title, subtitle) = when (tab) {
        CachedRecordsTab.Pending -> Triple(
            Icons.Outlined.HowToReg,
            stringResource(R.string.cached_records_pending_empty_title),
            stringResource(R.string.cached_records_pending_empty_subtitle),
        )
        CachedRecordsTab.Failed -> Triple(
            Icons.Filled.CheckCircle,
            stringResource(R.string.cached_records_failed_empty_title),
            stringResource(R.string.cached_records_failed_empty_subtitle),
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CachedRecordCard(entry: CachedRecordEntry, onLongPress: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val isFailed = entry.syncStatus == SyncStatus.Failed || entry.syncStatus == SyncStatus.Abandoned
    val accent = if (isFailed) AmberError else PrimaryGreen
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress,
            ),
        shape = RoundedCornerShape(16.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CandidateAvatar(name = entry.candidateName, accent = accent)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.candidateName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (entry.examNumber.isNotBlank()) {
                        Text(
                            text = entry.examNumber,
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
                RecordTypeChip(entry.recordType)
            }
            if (entry.paperTitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = entry.paperTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.cached_records_captured_format,
                    FRIENDLY_DATE.format(Date(entry.capturedAt)),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
            if (isFailed && !entry.syncError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = AmberError,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entry.syncError,
                        style = MaterialTheme.typography.bodySmall,
                        color = AmberError,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (entry.syncStatus == SyncStatus.Abandoned) {
                    Text(
                        text = stringResource(R.string.cached_records_abandoned_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CandidateAvatar(name: String, accent: Color) {
    val initials = remember(name) { name.toInitials() }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = accent,
        )
    }
}

@Composable
private fun RecordTypeChip(type: RecordType) {
    val (label, icon) = when (type) {
        RecordType.Attendance -> stringResource(R.string.cached_records_type_attendance) to Icons.Outlined.HowToReg
        RecordType.Remark -> stringResource(R.string.cached_records_type_remark) to Icons.Outlined.Description
        RecordType.PracticalScore -> stringResource(R.string.cached_records_type_practical) to Icons.Outlined.School
        RecordType.ProjectScore -> stringResource(R.string.cached_records_type_project) to Icons.Outlined.EventBusy
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = PrimaryGreen.copy(alpha = 0.10f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(12.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryGreen,
            )
        }
    }
}

@Composable
private fun RecordType.displayLabel(): String = when (this) {
    RecordType.Attendance -> stringResource(R.string.cached_records_type_attendance)
    RecordType.Remark -> stringResource(R.string.cached_records_type_remark)
    RecordType.PracticalScore -> stringResource(R.string.cached_records_type_practical)
    RecordType.ProjectScore -> stringResource(R.string.cached_records_type_project)
}

/** "Jane Doe" → "JD"; "Cher" → "C"; empty → "?". */
private fun String.toInitials(): String =
    trim().split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

/** Locale-friendly, unambiguous captured-at label ("Wed, 26 Aug · 14:03"). */
private val FRIENDLY_DATE: SimpleDateFormat =
    SimpleDateFormat("EEE, d MMM · HH:mm", Locale.getDefault())
