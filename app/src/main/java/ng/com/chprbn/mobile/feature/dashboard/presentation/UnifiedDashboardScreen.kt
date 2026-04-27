package ng.com.chprbn.mobile.feature.dashboard.presentation

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.SuccessGreen
import ng.com.chprbn.mobile.core.designsystem.components.AppTopBar
import ng.com.chprbn.mobile.core.designsystem.components.GridMenuCard

@Composable
fun UnifiedDashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToVerification: () -> Unit = {},
    onNavigateToVerifiedList: () -> Unit = {},
    onNavigateToSync: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToExamAttendance: () -> Unit = {},
    onNavigateToPracticalAssessment: () -> Unit = {},
    onNavigateToAccreditation: () -> Unit = {},
    onViewRecentLogs: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(Modifier.fillMaxSize()) {
            DashboardHeader()
            UnifiedDashboardContent(
                uiState = uiState,
                onNavigateToScan = onNavigateToVerification,
                onNavigateToExamAttendance = onNavigateToExamAttendance,
                onNavigateToPracticalAssessment = onNavigateToPracticalAssessment,
                onNavigateToAccreditation = onNavigateToAccreditation,
                onViewRecentLogs = onViewRecentLogs
            )
        }
    }
}

@Composable
private fun DashboardHeader() {
    AppTopBar(onNotifications = { })
}

@Composable
private fun UnifiedDashboardContent(
    uiState: DashboardUiState,
    onNavigateToScan: () -> Unit,
    onNavigateToExamAttendance: () -> Unit,
    onNavigateToPracticalAssessment: () -> Unit,
    onNavigateToAccreditation: () -> Unit,
    onViewRecentLogs: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 24.dp)
    ) {
        DashboardWelcomeCard(
            userName = uiState.userName,
            userEmail = uiState.userEmail,
            userStatus = uiState.userStatus
        )
        Spacer(modifier = Modifier.height(24.dp))
        DashboardFeatureGridSection(
            onLicenseVerification = onNavigateToScan,
            onExamAttendance = onNavigateToExamAttendance,
            onPracticalAssessment = onNavigateToPracticalAssessment,
            onAccreditation = onNavigateToAccreditation
        )
        Spacer(modifier = Modifier.height(24.dp))
//        QuickActionsSection(
//            lastSyncTime = uiState.lastSyncTime,
//            onViewRecentLogs = onViewRecentLogs
//        )
    }
}

@Composable
private fun DashboardWelcomeCard(
    userName: String,
    userEmail: String,
    userStatus: String
) {
    val displayName = userName.ifBlank { "Officer" }
    val role = userStatus.ifBlank { "Senior Field Officer" }
    val idAndUnit = userEmail.ifBlank { "— • —" }
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

private data class DashboardGridItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val isPrimary: Boolean,
    val onClick: () -> Unit
)

@Composable
private fun DashboardFeatureGridSection(
    onLicenseVerification: () -> Unit,
    onExamAttendance: () -> Unit,
    onPracticalAssessment: () -> Unit,
    onAccreditation: () -> Unit
) {
    val items = listOf(
        DashboardGridItem(
            title = "OSYVALAC",
            subtitle = "Operation show your license",
            icon = Icons.Outlined.VerifiedUser,
            isPrimary = false,
            onClick = onLicenseVerification
        ),
        DashboardGridItem(
            title = "EXAMS",
            subtitle = "Exam attendance & practical assessment",
            icon = Icons.Outlined.AccountCircle,
            isPrimary = false,
            onClick = onExamAttendance
        ),
        DashboardGridItem(
            title = "ACCREDITATION",
            subtitle = "Accreditation",
            icon = Icons.Outlined.WorkspacePremium,
            isPrimary = false,
            onClick = onAccreditation
        ),
//        DashboardGridItem(
//            title = "PRACTICAL",
//            subtitle = "Practical assessment",
//            icon = Icons.Filled.Science,
//            isPrimary = false,
//            onClick = onPracticalAssessment
//        )
    )

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

        items.chunked(2).forEachIndexed { index, rowItems ->
            if (index > 0) Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { item ->
                    GridMenuCard(
                        modifier = Modifier.weight(1f),
                        title = item.title,
                        subtitle = item.subtitle,
                        icon = item.icon,
                        isPrimary = item.isPrimary,
                        onClick = item.onClick
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    lastSyncTime: String,
    onViewRecentLogs: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onViewRecentLogs),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = "Recent Logs",
                        modifier = Modifier.size(24.dp),
                        tint = PrimaryGreen
                    )
                }
                Column {
                    Text(
                        text = "QUICK ACTIONS",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "View Recent Logs",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Last sync: $lastSyncTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Go",
                modifier = Modifier.size(24.dp),
                tint = PrimaryGreen.copy(alpha = 0.6f)
            )
        }
    }
}
