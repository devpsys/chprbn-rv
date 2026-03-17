package ng.com.chprbn.mobile.feature.sync.presentation

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.SuccessGreen
import ng.com.chprbn.mobile.core.designsystem.components.BottomNavBar
import ng.com.chprbn.mobile.core.designsystem.components.BottomNavTab

/**
 * Presentation-only implementation of the `sync_status_updated` design.
 */
@Composable
fun SyncScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onSyncAll: () -> Unit = {},
    onRetryFailed: () -> Unit = {},
    onViewAllHistory: () -> Unit = {},
    onHome: () -> Unit = {},
    onVerified: () -> Unit = {},
    onScanQr: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            SyncHeader(onBack = onBack, onRefresh = onRefresh)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                SyncProgressSection()
                Spacer(modifier = Modifier.height(16.dp))
                SyncStatsGrid()
                Spacer(modifier = Modifier.height(16.dp))
                SyncActionsSection(
                    onSyncAll = onSyncAll,
                    onRetryFailed = onRetryFailed
                )
                Spacer(modifier = Modifier.height(16.dp))
                SyncRecentStatusSection(onViewAll = onViewAllHistory)
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
        BottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedTab = BottomNavTab.Sync,
            onHome = onHome,
            onVerified = onVerified,
            onScanQr = onScanQr,
            onSync = { /* already on sync */ },
            onProfile = onProfile
        )
    }
}

@Composable
private fun SyncHeader(
    onBack: () -> Unit,
    onRefresh: () -> Unit
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
                text = "Sync Records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    tint = PrimaryGreen
                )
            }
        }
    }
}

@Composable
private fun SyncProgressSection() {
    val isConnected = rememberConnectivityStatus()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular progress ring showing 89% completion
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val strokeWidth = 8.dp.toPx()
                    val diameter = size.minDimension
                    val topLeft = Offset(
                        (size.width - diameter) / 2f + strokeWidth / 2f,
                        (size.height - diameter) / 2f + strokeWidth / 2f
                    )
                    val arcSize = Size(
                        width = diameter - strokeWidth,
                        height = diameter - strokeWidth
                    )

                    // Background circle
                    drawArc(
                        color = PrimaryGreen.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Foreground progress arc at 89%
                    val progress = 0.89f
                    drawArc(
                        color = PrimaryGreen,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "89%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Text(
                        text = "COMPLETE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Connected / Offline pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (isConnected) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isConnected) Color(0xFFC8E6C9) else Color(0xFFFFCDD2),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier.size(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) SuccessGreen else Color(0xFFC62828))
                    )
                }
                Text(
                    text = if (isConnected) "CONNECTED" else "OFFLINE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isConnected) SuccessGreen else Color(0xFFC62828)
                )
            }

            Text(
                text = if (isConnected) "Last synced 2 minutes ago" else "Waiting for connection…",
                style = MaterialTheme.typography.bodySmall,
                color = PrimaryGreen.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun SyncStatsGrid() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SyncStatCard(
            modifier = Modifier.weight(1f),
            label = "Total",
            value = "45",
            valueColor = PrimaryGreen
        )
        SyncStatCard(
            modifier = Modifier.weight(1f),
            label = "Synced",
            value = "40",
            valueColor = PrimaryGreen
        )
        SyncStatCard(
            modifier = Modifier.weight(1f),
            label = "Pending",
            value = "5",
            valueColor = Color(0xFFC62828)
        )
    }
}

@Composable
private fun SyncStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = PrimaryGreen.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun SyncActionsSection(
    onSyncAll: () -> Unit,
    onRetryFailed: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSyncAll,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Filled.CloudSync,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "SYNC ALL RECORDS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Button(
            onClick = onRetryFailed,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PrimaryGreen
            ),
            border = BorderStroke(2.dp, PrimaryGreen.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "RETRY FAILED SYNC",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SyncRecentStatusSection(
    onViewAll: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT STATUS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen.copy(alpha = 0.7f)
            )
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = PrimaryGreen,
                modifier = Modifier.clickable(onClick = onViewAll)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SyncStatusItem(
                iconBackground = Color(0xFFE8F5E9),
                iconTint = SuccessGreen,
                icon = Icons.Filled.CheckCircle,
                title = "Record #8829-B",
                subtitle = "Successfully uploaded to Central Registry",
                time = "10:45 AM"
            )
            SyncStatusItem(
                iconBackground = Color(0xFFE8F5E9),
                iconTint = SuccessGreen,
                icon = Icons.Filled.CheckCircle,
                title = "Record #8830-A",
                subtitle = "Successfully uploaded to Central Registry",
                time = "10:42 AM"
            )
            SyncStatusItem(
                iconBackground = Color(0xFFFFEBEE),
                iconTint = Color(0xFFC62828),
                icon = Icons.Filled.Error,
                title = "Record #8831-C",
                subtitle = "Network timeout during transmission",
                time = "09:15 AM",
                borderColor = Color(0xFFFFCDD2)
            )
        }
    }
}

@Composable
private fun SyncStatusItem(
    iconBackground: Color,
    iconTint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    time: String,
    borderColor: Color = PrimaryGreen.copy(alpha = 0.05f)
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (iconTint == Color(0xFFC62828)) Color(0xFFC62828) else PrimaryGreen
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryGreen.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = PrimaryGreen.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Simple connectivity monitor: returns true when there is an active network
 * with internet capability, false otherwise.
 */
@Composable
private fun rememberConnectivityStatus(): Boolean {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(false) }

    DisposableEffect(context) @androidx.annotation.RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        fun currentStatus(): Boolean {
            val network = connectivityManager.activeNetwork ?: return false
            val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        isConnected = currentStatus()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnected = true
            }

            override fun onLost(network: Network) {
                isConnected = currentStatus()
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    return isConnected
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun SyncScreenPreview() {
    ChprbnTheme {
        SyncScreen()
    }
}

