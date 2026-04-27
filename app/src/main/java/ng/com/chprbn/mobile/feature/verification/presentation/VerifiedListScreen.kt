package ng.com.chprbn.mobile.feature.verification.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.ErrorRed
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.SuccessGreen
import ng.com.chprbn.mobile.core.designsystem.WarningYellow
import ng.com.chprbn.mobile.core.designsystem.components.BottomNavBar
import ng.com.chprbn.mobile.core.designsystem.components.BottomNavTab
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Presentation-only implementation of the `verified_list_updated` design.
 */
@Composable
fun VerifiedListScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onMenu: () -> Unit = {},
    onPractitionerClicked: (VerifiedPractitioner) -> Unit = {},
    refreshRequested: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    onHome: () -> Unit = {},
    onScanQr: () -> Unit = {},
    onSync: () -> Unit = {},
    onProfile: () -> Unit = {},
    viewModel: VerifiedListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val items = uiState.filteredPractitioners
    LaunchedEffect(refreshRequested) {
        if (refreshRequested) {
            viewModel.refresh()
            onRefreshConsumed()
        }
    }

    VerifiedListContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onMenu = onMenu,
        onPractitionerClicked = onPractitionerClicked,
        onQueryChange = viewModel::onQueryChange,
        onFilterSelected = viewModel::onFilterSelected,
        onHome = onHome,
        onScanQr = onScanQr,
        onSync = onSync,
        onProfile = onProfile
    )
}

@Composable
fun VerifiedListContent(
    modifier: Modifier = Modifier,
    uiState: VerifiedListUiState,
    onBack: () -> Unit = {},
    onMenu: () -> Unit = {},
    onPractitionerClicked: (VerifiedPractitioner) -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    onFilterSelected: (VerifiedFilter) -> Unit = {},
    onHome: () -> Unit = {},
    onScanQr: () -> Unit = {},
    onSync: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    val items = uiState.filteredPractitioners

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            VerifiedHeader(onBack = onBack, onMenu = onMenu)
            SearchBar(
                query = uiState.query,
                onQueryChange = onQueryChange
            )
            FilterTabs(
                selected = uiState.selectedFilter,
                onFilterSelected = onFilterSelected
            )

            when {
                uiState.isLoading -> {
                    Text(
                        text = "Loading verified practitioners...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryGreen.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        textAlign = TextAlign.Center
                    )
                }

                items.isEmpty() -> {
                    VerifiedPractitionersNoRecordsFound(modifier = Modifier.weight(1f))
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items) { practitioner ->
                            VerifiedPractitionerRow(
                                practitioner = practitioner,
                                onClick = { onPractitionerClicked(practitioner) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }

        BottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedTab = BottomNavTab.Verified,
            onHome = onHome,
            onVerified = { /* already on verified */ },
            onScanQr = onScanQr,
            onSync = onSync,
            onProfile = onProfile
        )
        }
    }
}

@Composable
private fun VerifiedPractitionersNoRecordsFound(
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration (matches `ui-designs/verified_practitioners_no_records_found`)
        Box(
            modifier = Modifier
                .size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer brand glow behind the card
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(PrimaryGreen.copy(alpha = 0.15f))
                    .blur(24.dp)
            )

            Surface(
                modifier = Modifier.size(140.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Box(modifier = Modifier.matchParentSize()) {
                    // Radial gradient overlay (low opacity)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        PrimaryGreen.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonSearch,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(4.dp)
                                    .background(
                                        PrimaryGreen.copy(alpha = 0.35f),
                                        RoundedCornerShape(999.dp)
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(4.dp)
                                    .background(PrimaryGreen, RoundedCornerShape(999.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(4.dp)
                                    .background(
                                        PrimaryGreen.copy(alpha = 0.35f),
                                        RoundedCornerShape(999.dp)
                                    )
                            )
                        }
                    }
                }
            }

            // Secondary floating icon (positioned slightly outside the card)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .graphicsLayer {
                        // Position outside the card boundary without using negative padding.
                        translationX = with(density) { 6.dp.toPx() }
                        translationY = with(density) { 6.dp.toPx() }
                    },
                shape = RoundedCornerShape(16.dp),
                color = PrimaryGreen,
                border = BorderStroke(4.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Records Found",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = "We couldn't find any verified practitioners matching your criteria. Try adjusting your filters or start a fresh verification.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun VerifiedHeader(
    onBack: () -> Unit,
    onMenu: () -> Unit
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryGreen
                )
            }
            Text(
                text = "Verified Practitioners",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center
            )
            IconButton(onClick = onMenu, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More",
                    tint = PrimaryGreen
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = PrimaryGreen.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            },
            placeholder = {
                Text(
                    text = "Search by name, ID or specialty...",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryGreen.copy(alpha = 0.4f)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = PrimaryGreen,
                unfocusedIndicatorColor = PrimaryGreen.copy(alpha = 0.25f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = PrimaryGreen
            )
        )
    }
}

@Composable
private fun FilterTabs(
    selected: VerifiedFilter,
    onFilterSelected: (VerifiedFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterPillTab(
            text = "All",
            selected = selected == VerifiedFilter.All,
            onClick = { onFilterSelected(VerifiedFilter.All) }
        )
        FilterPillTab(
            text = "Active",
            selected = selected == VerifiedFilter.Active,
            onClick = { onFilterSelected(VerifiedFilter.Active) }
        )
        FilterPillTab(
            text = "Expired",
            selected = selected == VerifiedFilter.Expired,
            onClick = { onFilterSelected(VerifiedFilter.Expired) }
        )
        FilterPillTab(
            text = "Pending Sync",
            selected = selected == VerifiedFilter.PendingSync,
            onClick = { onFilterSelected(VerifiedFilter.PendingSync) }
        )
    }
}

@Composable
private fun FilterPillTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) PrimaryGreen else MaterialTheme.colorScheme.surface,
        border = if (selected) null else BorderStroke(
            1.dp,
            PrimaryGreen.copy(alpha = 0.2f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun VerifiedPractitionerRow(
    practitioner: VerifiedPractitioner,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                if (practitioner.photoUrl != null) {
                    AsyncImage(
                        model = practitioner.photoUrl,
                        contentDescription = practitioner.name,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = null,
                            tint = PrimaryGreen
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            when (practitioner.status) {
                                VerifiedStatus.Active -> SuccessGreen
                                VerifiedStatus.Expired -> ErrorRed
                                VerifiedStatus.Syncing -> Color.Gray
                            }
                        )
                        .align(Alignment.BottomEnd)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = practitioner.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = when (practitioner.status) {
                            VerifiedStatus.Active -> Icons.Filled.Sync
                            VerifiedStatus.Expired -> Icons.Filled.SyncProblem
                            VerifiedStatus.Syncing -> Icons.Filled.Sync
                        },
                        contentDescription = null,
                        tint = when (practitioner.status) {
                            VerifiedStatus.Active -> PrimaryGreen.copy(alpha = 0.4f)
                            VerifiedStatus.Expired -> WarningYellow
                            VerifiedStatus.Syncing -> PrimaryGreen.copy(alpha = 0.4f)
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "License: ${practitioner.license}",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryGreen.copy(alpha = 0.7f)
                )
                Text(
                    text = "Verified: ${practitioner.verifiedAtText}",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryGreen.copy(alpha = 0.7f)
                )
                when (practitioner.syncStatus) {
                    VerifiedSyncStatus.Pending -> {
                        Text(
                            text = "Pending Sync",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarningYellow
                        )
                    }
                    VerifiedSyncStatus.Failed -> {
                        Text(
                            text = "Sync failed — retry from Sync tab",
                            style = MaterialTheme.typography.labelSmall,
                            color = ErrorRed
                        )
                    }
                    VerifiedSyncStatus.Synced -> { /* no extra line */ }
                }
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(status = practitioner.status, expiry = practitioner.expiryText)
                }
            }

            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = null,
                tint = PrimaryGreen.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun StatusChip(
    status: VerifiedStatus,
    expiry: String
) {
    val (bg, textColor, label) = when (status) {
        VerifiedStatus.Active -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            "Active"
        )

        VerifiedStatus.Expired -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFC62828),
            "Expired"
        )

        VerifiedStatus.Syncing -> Triple(
            Color(0xFFE0E0E0),
            Color(0xFF616161),
            "Syncing"
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .background(bg, RoundedCornerShape(999.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
        Text(
            text = expiry,
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryGreen.copy(alpha = 0.4f)
        )
    }
}

data class VerifiedPractitioner(
    val name: String,
    val license: String,
    val status: VerifiedStatus,
    val expiryText: String,
    val verifiedAtText: String = "",
    val syncStatus: VerifiedSyncStatus = VerifiedSyncStatus.Synced,
    val photoUrl: String?
)

enum class VerifiedStatus {
    Active, Expired, Syncing
}

enum class VerifiedSyncStatus {
    Pending, Synced, Failed
}

// Static sample data matching the design content.
val samplePractitioners = listOf(
    VerifiedPractitioner(
        name = "Dr. Sarah Jenkins",
        license = "#MD-99201-B",
        status = VerifiedStatus.Active,
        expiryText = "• Exp: Oct 2025",
        photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDVyCPEnhAe7V2an7rqBLsMsyhH2o4ITzmmjlg5Ar_4zR9oY2pv034Jv0-in9y6lqZpSXAuEhsH3-n2M4QWDRBL6wNeH-qIfGD6HylMLdUjv22cfgPVT1_WOdjWxpQhGuge3iVIuf8ef_DPnEF4W-Mys6DaP5XjzLJn-2-TOqQkJQRXOfmb7fOMULGnnNs1KJ3q7Tv-5WkUG7VswqNkNA1_YYaxfI5X1QZHfRgLWuhsG1kfPwqqt0KOin4rjIbT_Y0NeCmHmt2DY__J"
    ),
    VerifiedPractitioner(
        name = "Dr. Michael Chen",
        license = "#NP-44211-X",
        status = VerifiedStatus.Expired,
        expiryText = "• Exp: Jan 2024",
        photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCrK3H-TyYi8fV3fzAQ34XZ2DhBozSVIJtALrUlc7E1EHe9NoguUYqk2CDJbf9lkl8m61MI8qjvS3YO0ScSATvPHEw_Jjxt7JIr59RTCYMWCIeJ8z7OT8vtCap9tfFkmu0Ah3gLmab2tH2FjgXdroXs9Obi6gZVhQY40EZWRSi-Y7gD-nKWNeltEqyWobnzZpzqwbFFdSKEv_wyRYcVd27_hStXTPxuHZ-AvSEPtfWmzoEb80B4w0PRnKguMJ9gLvcocyJ2nw1NVhFk"
    ),
    VerifiedPractitioner(
        name = "Dr. Elena Rodriguez",
        license = "#MD-33100-R",
        status = VerifiedStatus.Active,
        expiryText = "• Exp: Dec 2026",
        photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCUfOWwDbxqi4hlK5mVF0De0mBHaisIIaAJkIm3fe9-cPzCKzlkqJgxC6QaZS9rVy34Ovi-1TPbRl1wZRJyYAGIc1PBnMwLdncalqbeSKqEK-B4z3BvJWAHon_6Z3cByzyFemdUp3Ss5feLO_wudIL4jYgs7KGdMynlMrttt6Fzl9cwBLBrDBlRFkuRFjiad4c3hyy97LRFfNPjAfSgSUe8uTAT8CYphUz3lv_mHFGWsqE1Kmii9dGV-4hWvz2Nv0mTBS_wCr6mgM0p"
    ),
    VerifiedPractitioner(
        name = "Dr. James Wilson",
        license = "#DO-88229-P",
        status = VerifiedStatus.Syncing,
        expiryText = "• Pending Update",
        photoUrl = null
    ),
    VerifiedPractitioner(
        name = "Dr. Robert Miller",
        license = "#MD-11993-C",
        status = VerifiedStatus.Active,
        expiryText = "• Exp: May 2025",
        photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDjA6sFqgPNQmmR89ykH-fW1KnvUtlC8_Fry9HCNYXDPrlZL_r6VoMr-jwEjkqWR1tA7AQy1eDSpSRWLeTXxBPE7f75KIXzj2EV618LohxXj4RHqAR6_uYKR7N2H20TCI97roufYDeOdihUsdtCsYkfWejvTKz6hWmavI8eu0vnr5k_YOxuibIJe3ITKjNhg_Re3DEXhszS4R3-kTvKLBl3X9yxcW1kb7v5r3_5EfWZpYIkUqXy5Qa7J-wZfeSr0MkgF6ugpAebpEKp"
    )
)

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun VerifiedListScreenPreview() {
    ChprbnTheme {
        VerifiedListContent(uiState = VerifiedListUiState(practitioners = samplePractitioners))
    }
}

