package ng.com.chprbn.mobile.feature.dashboard.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.SuccessGreen
import ng.com.chprbn.mobile.core.designsystem.components.BottomNavBar
import ng.com.chprbn.mobile.core.designsystem.components.BottomNavTab
import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.dashboard.domain.model.DashboardFeature
import ng.com.chprbn.mobile.feature.dashboard.domain.model.FeatureType

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onScanQr: () -> Unit = {},
    onVerifiedList: () -> Unit = {},
    onSync: () -> Unit = {},
    onProfile: () -> Unit = {},
    onHome: () -> Unit = {},
    onSearch: () -> Unit = {},
    onSettings: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
    featuresOverride: List<DashboardFeature>? = null
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(Modifier.fillMaxSize()) {
            DashboardTopBar(
                onNotifications = { },
                onMenu = { }
            )
            if (featuresOverride != null) {
                DashboardContent(
                    user = null,
                    featureList = featuresOverride,
                    onFeatureClick = { feature ->
                        when (feature.id) {
                            FeatureType.ScanQr -> onScanQr()
                            FeatureType.VerifiedList -> onVerifiedList()
                            FeatureType.Sync -> onSync()
                            FeatureType.Profile -> onProfile()
                        }
                    },
                    onScanQr = onScanQr,
                    onVerifiedList = onVerifiedList,
                    onSync = onSync,
                    onProfile = onProfile,
                    isLoading = false
                )
            } else {
                when (val state = uiState) {
                    is DashboardUiState.Loading -> DashboardContent(
                        user = null,
                        featureList = emptyList(),
                        onFeatureClick = { },
                        onScanQr = onScanQr,
                        onVerifiedList = onVerifiedList,
                        onSync = onSync,
                        onProfile = onProfile,
                        isLoading = true
                    )

                    is DashboardUiState.Success -> DashboardContent(
                        user = state.user,
                        featureList = state.features,
                        onFeatureClick = { feature ->
                            when (feature.id) {
                                FeatureType.ScanQr -> onScanQr()
                                FeatureType.VerifiedList -> onVerifiedList()
                                FeatureType.Sync -> onSync()
                                FeatureType.Profile -> onProfile()
                            }
                        },
                        onScanQr = onScanQr,
                        onVerifiedList = onVerifiedList,
                        onSync = onSync,
                        onProfile = onProfile,
                        isLoading = false
                    )

                    is DashboardUiState.Error -> DashboardContent(
                        user = null,
                        featureList = emptyList(),
                        onFeatureClick = { },
                        onScanQr = onScanQr,
                        onVerifiedList = onVerifiedList,
                        onSync = onSync,
                        onProfile = onProfile,
                        isLoading = false,
                        errorMessage = state.message,
                        onRetry = { viewModel.retry() }
                    )
                }
            }
        }
        BottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedTab = BottomNavTab.Home,
            onHome = onHome,
            onVerified = onSearch,
            onScanQr = onScanQr,
            onSync = onSync,
            onProfile = onProfile
        )
    }
}

@Composable
private fun DashboardContent(
    user: User?,
    featureList: List<DashboardFeature>,
    onFeatureClick: (DashboardFeature) -> Unit,
    onScanQr: () -> Unit,
    onVerifiedList: () -> Unit,
    onSync: () -> Unit,
    onProfile: () -> Unit,
    isLoading: Boolean,
    errorMessage: String? = null,
    onRetry: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 100.dp)
    ) {
        WelcomeCard(user = user)
        Spacer(modifier = Modifier.height(24.dp))
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }

            errorMessage != null -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onRetry) {
                            Text("Retry", color = PrimaryGreen)
                        }
                    }
                }
            }

            else -> FeatureGridSection(
                features = featureList,
                onFeatureClick = onFeatureClick
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        SystemStatusCard()
    }
}

@Composable
fun WelcomeCard(user: User? = null) {
    val displayName = user?.fullName?.takeIf { it.isNotBlank() } ?: "Officer"
    val role = user?.role?.takeIf { it.isNotBlank() } ?: "Senior Field Officer"
    val idAndUnit = when {
        user?.username?.isNotBlank() == true && user.unit != null ->
            "${user.username} • ${user.unit}"
        user?.username?.isNotBlank() == true -> user.username
        user?.unit != null -> user.unit
        else -> "— • —"
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen.copy(alpha = 0.1f))
                        .border(2.dp, PrimaryGreen.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = PrimaryGreen
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                )
            }
            Column {
                Text(
                    text = "Welcome, $displayName",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryGreen.copy(alpha = 0.7f)
                )
                Text(
                    text = idAndUnit,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DashboardTopBar(
    onNotifications: () -> Unit,
    onMenu: () -> Unit
) {
    // Semi-transparent bar with bottom border only (no top/side borders)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
                Column {
                    Text(
                        text = "CHPRBN",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Text(
                        text = "OFFICIAL USE ONLY",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                Box {
                    IconButton(
                        onClick = onNotifications,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            modifier = Modifier.size(24.dp),
                            tint = PrimaryGreen
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                    )
                }

            }
        }
        // Bottom border only (full width)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PrimaryGreen.copy(alpha = 0.15f))
        ) {}
    }
}

@Composable
fun FeatureGridSection(
    features: List<DashboardFeature>,
    onFeatureClick: (DashboardFeature) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FEATURE GRID",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = PrimaryGreen.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "ONLINE",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        features.chunked(2).forEachIndexed { index, rowFeatures ->
            if (index > 0) Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowFeatures.forEach { feature ->
                    FeatureCard(
                        modifier = Modifier.weight(1f),
                        title = feature.title,
                        subtitle = feature.subtitle,
                        icon = feature.id.toIcon(),
                        isPrimary = feature.isPrimary,
                        onClick = { onFeatureClick(feature) }
                    )
                }
                if (rowFeatures.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/** Maps domain [FeatureType] to UI icon (presentation layer only). */
private fun FeatureType.toIcon(): ImageVector = when (this) {
    FeatureType.ScanQr -> Icons.Filled.QrCodeScanner
    FeatureType.VerifiedList -> Icons.Filled.VerifiedUser
    FeatureType.Sync -> Icons.Filled.Sync
    FeatureType.Profile -> Icons.Filled.AccountCircle
}

@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isPrimary) PrimaryGreen else MaterialTheme.colorScheme.surface
    val contentColor = if (isPrimary) Color.White else MaterialTheme.colorScheme.onSurface
    val subtitleColor =
        if (isPrimary) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
    val iconTint = if (isPrimary) Color.White else PrimaryGreen
    val iconBg =
        if (isPrimary) Color.White.copy(alpha = 0.2f) else PrimaryGreen.copy(alpha = 0.1f)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = if (!isPrimary) BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.1f)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconTint
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor
            )
        }
    }
}

@Composable
fun SystemStatusCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SYSTEM STATUS",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Device Encryption",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Location Services",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Enabled",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(PrimaryGreen.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(PrimaryGreen)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "LOCAL DATABASE 75% CAPACITY",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun DashboardScreenPreview() {
    ChprbnTheme {
        DashboardScreen(featuresOverride = previewDashboardFeatures)
    }
}
