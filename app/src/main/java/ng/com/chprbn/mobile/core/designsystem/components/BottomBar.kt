package ng.com.chprbn.mobile.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen

/**
 * Global bottom navigation bar used across screens.
 *
 * The specific icons/labels used here match the enforcement dashboard design,
 * but the composable itself can be reused wherever this layout is desired.
 */
@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onScanQr: () -> Unit,
    onSync: () -> Unit,
    onSettings: () -> Unit
) {
    // Bottom bar: top border only, no bottom border; floating center FAB
    Column(modifier = modifier.fillMaxWidth()) {
        // Top border only (full width)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PrimaryGreen.copy(alpha = 0.15f))
        ) {}
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Filled.Home,
                label = "Home",
                selected = true,
                onClick = onHome
            )
            BottomNavItem(
                icon = Icons.Filled.People,
                label = "Practitioners",
                selected = false,
                onClick = onSearch
            )
            // Floating center FAB: offset -24.dp, 4.dp surface border, shadow
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = (-24).dp)
                    .shadow(8.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.25f))
                    .clip(CircleShape)
                    .background(PrimaryGreen)
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
                    .clickable(onClick = onScanQr),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = "Scan QR",
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }
            BottomNavItem(
                icon = Icons.Filled.Sync,
                label = "Sync",
                selected = false,
                onClick = onSync
            )
            BottomNavItem(
                icon = Icons.Filled.Person,
                label = "Profile",
                selected = false,
                onClick = onSettings
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(64.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (selected) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
        )
        androidx.compose.material3.Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
            color = if (selected) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

