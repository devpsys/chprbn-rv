package ng.com.chprbn.mobile.feature.sync.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.components.BottomNavBar
import ng.com.chprbn.mobile.core.designsystem.components.BottomNavTab

/**
 * Presentation-only implementation of the `sync_history` design.
 */
@Composable
fun SyncHistoryScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onItemClick: (SyncHistoryItem) -> Unit = {},
    onHome: () -> Unit = {},
    onVerified: () -> Unit = {},
    onScanQr: () -> Unit = {},
    onProfile: () -> Unit = {},
    viewModel: SyncHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SyncHistoryHeader(onBack = onBack)
            SyncHistorySearchAndFilter(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                filter = uiState.filter,
                onFilterSelected = viewModel::onFilterSelected
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredItems) { item ->
                    SyncHistoryRow(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        BottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedTab = BottomNavTab.Sync,
            onHome = onHome,
            onVerified = onVerified,
            onScanQr = onScanQr,
            onSync = { /* already on sync section */ },
            onProfile = onProfile
        )
    }
}

@Composable
private fun SyncHistoryHeader(
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryGreen
                )
            }
            Text(
                text = "Sync History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
private fun SyncHistorySearchAndFilter(
    query: String,
    onQueryChange: (String) -> Unit,
    filter: SyncHistoryFilter,
    onFilterSelected: (SyncHistoryFilter) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = PrimaryGreen.copy(alpha = 0.6f)
                    )
                },
                placeholder = {
                    Text(
                        text = "Search record ID or date...",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryGreen.copy(alpha = 0.4f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    text = "All",
                    selected = filter == SyncHistoryFilter.All,
                    onClick = { onFilterSelected(SyncHistoryFilter.All) }
                )
                FilterChip(
                    text = "Synced",
                    selected = filter == SyncHistoryFilter.Synced,
                    onClick = { onFilterSelected(SyncHistoryFilter.Synced) }
                )
                FilterChip(
                    text = "Failed",
                    selected = filter == SyncHistoryFilter.Failed,
                    onClick = { onFilterSelected(SyncHistoryFilter.Failed) }
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) PrimaryGreen else PrimaryGreen.copy(alpha = 0.08f)
    val contentColor = if (selected) Color.White else PrimaryGreen

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
private fun SyncHistoryRow(
    item: SyncHistoryItem,
    onClick: () -> Unit
) {
    val (iconBg, iconTint, messageColor, borderColor) = when (item.status) {
        SyncStatus.Success -> Quadruple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            Color(0xFF2E7D32),
            PrimaryGreen.copy(alpha = 0.05f)
        )

        SyncStatus.Failed -> Quadruple(
            Color(0xFFFFEBEE),
            Color(0xFFC62828),
            Color(0xFFC62828),
            Color(0xFFFFCDD2)
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.status) {
                        SyncStatus.Success -> Icons.Filled.CheckCircle
                        SyncStatus.Failed -> Icons.Filled.Error
                    },
                    contentDescription = null,
                    tint = iconTint
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.id,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                    )
                }
                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = messageColor
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun SyncHistoryScreenPreview() {
    ChprbnTheme {
        SyncHistoryScreen()
    }
}

