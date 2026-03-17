package ng.com.chprbn.mobile.feature.verified.presentation

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.Text
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
    onHome: () -> Unit = {},
    onScanQr: () -> Unit = {},
    onSync: () -> Unit = {},
    onProfile: () -> Unit = {},
    viewModel: VerifiedListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val items = uiState.filteredPractitioners

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            VerifiedHeader(onBack = onBack, onMenu = onMenu)
            SearchBar(
                query = uiState.query,
                onQueryChange = { q -> viewModel.onQueryChange(q) }
            )
            FilterTabs(
                selected = uiState.selectedFilter,
                onFilterSelected = { filter -> viewModel.onFilterSelected(filter) }
            )

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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                    text = "Search by name or license number",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryGreen.copy(alpha = 0.4f)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
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
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterTab(
            text = "All",
            selected = selected == VerifiedFilter.All,
            onClick = { onFilterSelected(VerifiedFilter.All) }
        )
        FilterTab(
            text = "Active",
            selected = selected == VerifiedFilter.Active,
            onClick = { onFilterSelected(VerifiedFilter.Active) }
        )
        FilterTab(
            text = "Expired",
            selected = selected == VerifiedFilter.Expired,
            onClick = { onFilterSelected(VerifiedFilter.Expired) }
        )
        FilterTab(
            text = "Pending Sync",
            selected = selected == VerifiedFilter.PendingSync,
            onClick = { onFilterSelected(VerifiedFilter.PendingSync) }
        )
    }
}

@Composable
private fun FilterTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) PrimaryGreen else PrimaryGreen.copy(alpha = 0.6f)
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(2.dp)
                    .background(PrimaryGreen, RoundedCornerShape(999.dp))
                    .fillMaxWidth(0.5f)
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
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
        tonalElevation = 1.dp
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
    val photoUrl: String?
)

enum class VerifiedStatus {
    Active, Expired, Syncing
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
        VerifiedListScreen()
    }
}

