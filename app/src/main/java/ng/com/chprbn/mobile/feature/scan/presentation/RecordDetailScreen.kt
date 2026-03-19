package ng.com.chprbn.mobile.feature.scan.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.SuccessGreen
import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord

/**
 * Record detail screen: loads license record by registration number (local-first, then API), displays or error.
 */
@Composable
fun RecordDetailScreen(
    modifier: Modifier = Modifier,
    registrationNumber: String,
    onBack: () -> Unit = {},
    onMenu: () -> Unit = {},
    onProceedToVerification: (practitionerName: String, registrationNumber: String) -> Unit = { _, _ -> },
    viewModel: RecordDetailViewModel = hiltViewModel(),
    recordOverride: LicenseRecord? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(registrationNumber) {
        if (recordOverride == null) viewModel.loadRecord(registrationNumber)
    }

    val record = recordOverride ?: (state as? RecordDetailUiState.Success)?.record
    val digitalLicenseId = record?.registrationNumber?.takeIf { it.isNotBlank() } ?: registrationNumber.ifEmpty { "—" }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(Modifier.fillMaxSize()) {
            RecordDetailHeader(onBack = onBack, onMenu = onMenu)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 140.dp)
            ) {
                when {
                    record != null -> RecordDetailContent(record = record)
                    state is RecordDetailUiState.Loading -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                    state is RecordDetailUiState.NotFound -> RecordDetailErrorContent(
                        message = "License record not found.",
                        onRetry = { viewModel.retry(registrationNumber) }
                    )
                    state is RecordDetailUiState.Error -> RecordDetailErrorContent(
                        message = (state as RecordDetailUiState.Error).message,
                        onRetry = { viewModel.retry(registrationNumber) }
                    )
                }
            }
        }
        if (record != null) {
            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                    shadowElevation = 0.dp,
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = { onProceedToVerification(record.fullName, record.registrationNumber) }),
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryGreen
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 24.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VerifiedUser,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "Proceed to Verification",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            text = "Digital License ID: $digitalLicenseId",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryGreen.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordDetailContent(record: LicenseRecord) {
    val photoUrl = record.photoUrl?.takeIf { it.isNotBlank() }
        ?: "https://lh3.googleusercontent.com/aida-public/AB6AXuDGwNrw6oZQaZ3azGcP4PrP56q5MV3bz6F2bl-IuygmFXDriKJgdpSy_ndOO9YBkKsOQRk5TrJCLaOwOJNwGnnK5PqCLWyvbNrkAhkwQJD1mE2BcM0rn2nG23k3fmuanXead12LR4L8GCyJieC4dL2mbIFUzchSlO0WQIbmqu6v6paUkf3jYicZtQBrEIOXDf7WFpm6jPbtSsfYHwaEHzuwCfqDNp81YmnQS40CoNyYqqVIxgzym_iSQA5hGYhPX6ekEVIMS63rA6t9"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(PrimaryGreen.copy(alpha = 0.1f))
                .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .padding(4.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = record.fullName.ifEmpty { "—" },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen,
            textAlign = TextAlign.Center
        )
        Text(
            text = record.subtitle?.takeIf { it.isNotBlank() } ?: "Medical Professional ID",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryGreen.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .height(90.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LicenseStatusCard(
            modifier = Modifier.weight(1f),
            status = record.licenseStatus.ifEmpty { "Active" }
        )
        ExpiryDateCard(
            modifier = Modifier.weight(1f),
            expiryDate = record.expiryDate.ifEmpty { "Dec 2026" }
        )
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.05f))
    ) {
        Column {
            DetailRow(
                label = "LICENSE NUMBER",
                value = record.registrationNumber.ifEmpty { "—" },
                showDivider = true
            )
            DetailRow(
                label = "PROFESSION",
                value = record.profession.ifEmpty { "—" },
                showDivider = true
            )
            DetailRow(
                label = "AUTHORITY",
                value = record.authority.ifEmpty { "—" },
                showDivider = false
            )
        }
    }
}

@Composable
private fun RecordDetailErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onRetry) {
            Text("Retry", color = PrimaryGreen)
        }
    }
}

@Composable
private fun RecordDetailHeader(onBack: () -> Unit, onMenu: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                text = "Verification Panel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
private fun LicenseStatusCard(
    modifier: Modifier = Modifier,
    status: String = "Active"
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "LICENSE STATUS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = SuccessGreen.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SuccessGreen
                    )
                    Text(
                        text = status.ifEmpty { "Active" },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpiryDateCard(
    modifier: Modifier = Modifier,
    expiryDate: String = "Dec 2026"
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "EXPIRY DATE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Event,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = PrimaryGreen.copy(alpha = 0.6f)
                )
                Text(
                    text = expiryDate.ifEmpty { "—" },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    showDivider: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryGreen.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PrimaryGreen.copy(alpha = 0.05f))
            )
        }
    }
}

private val previewRecord = LicenseRecord(
    registrationNumber = "MED-12345",
    fullName = "Dr. Jane Doe",
    photoUrl = null,
    profession = "General Practitioner",
    authority = "Medical Council",
    licenseStatus = "Active",
    expiryDate = "Dec 2026",
    subtitle = "Medical Professional ID"
)

@Preview(showBackground = true)
@Composable
private fun RecordDetailScreenPreview() {
    ChprbnTheme {
        RecordDetailScreen(
            registrationNumber = "MED-12345",
            onBack = {},
            onMenu = {},
            onProceedToVerification = { _, _ -> },
            recordOverride = previewRecord
        )
    }
}
