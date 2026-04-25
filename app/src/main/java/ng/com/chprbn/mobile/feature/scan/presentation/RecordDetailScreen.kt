package ng.com.chprbn.mobile.feature.scan.presentation

import android.graphics.BitmapFactory
import android.util.Base64
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.ErrorRed
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.core.designsystem.SuccessGreen
import ng.com.chprbn.mobile.feature.scan.domain.model.InstitutionAttended
import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord
import java.util.Locale
import kotlin.text.ifEmpty

/**
 * Record detail screen: loads license record by registration number (local-first, then API), displays or error.
 */
@Composable
fun RecordDetailScreen(
    modifier: Modifier = Modifier,
    registrationNumber: String,
    onBack: () -> Unit = {},
    onMenu: () -> Unit = {},
    onProceedToVerification: (LicenseRecord) -> Unit = {},
    onReportIrregularity: (LicenseRecord) -> Unit = {},
    onManualEntry: () -> Unit = {},
    viewModel: RecordDetailViewModel = hiltViewModel(),
    recordOverride: LicenseRecord? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(registrationNumber) {
        if (recordOverride == null) viewModel.loadRecord(registrationNumber)
    }

    val record = recordOverride ?: (state as? RecordDetailUiState.Success)?.record
    val digitalLicenseId =
        record?.registrationNumber?.takeIf { it.isNotBlank() } ?: registrationNumber.ifEmpty { "—" }

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
                    state is RecordDetailUiState.Loading -> RecordDetailLoadingContent()
                    state is RecordDetailUiState.NotFound -> RecordDetailNoRecordContent(
                        registrationNumber = registrationNumber,
                        onRetry = { viewModel.retry(registrationNumber) },
                        onManualEntry = onManualEntry
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
                                .clickable(onClick = { onProceedToVerification(record) }),
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
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { onReportIrregularity(record) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ReportProblem,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Report irregularity",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Text(
                            text = "Digital License ID: $digitalLicenseId",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryGreen.copy(alpha = 0.75f),
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
            PractitionerPhotoBase64(photoPayload = record.photoUrl)
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
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = if (record.licenseStatus == "Active") SuccessGreen.copy(alpha = 0.2f) else ErrorRed.copy(
                alpha = 0.2f
            ),
            border = BorderStroke(
                1.dp,
                if (record.licenseStatus == "Active") SuccessGreen.copy(alpha = 0.4f) else ErrorRed.copy(
                    alpha = 0.4f
                )
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (record.licenseStatus == "Active") Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (record.licenseStatus == "Active") SuccessGreen else ErrorRed
                )
                Text(
                    text = record.licenseStatus.ifEmpty { "Active" } + " License",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (record.licenseStatus == "Active") SuccessGreen else ErrorRed
                )
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .height(90.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IssueDateCard(
            modifier = Modifier.weight(1f),
            issueDate = record.issueDate.ifBlank { "—" }
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
                label = "CERTIFICATE NO",
                value = record.certificateNo.ifEmpty { "—" },
                showDivider = true
            )
            DetailRow(
                label = "EMAIL",
                value = record.email.ifEmpty { "—" },
                showDivider = true
            )
            DetailRow(
                label = "PHONE",
                value = record.phone.ifEmpty { "—" },
                showDivider = true
            )
            DetailRow(
                label = "GENDER",
                value = record.gender.ifEmpty { "—" }.uppercase(Locale.UK),
                showDivider = true
            )
            DetailRow(
                label = "CADRE",
                value = record.profession.ifEmpty { "—" },
                showDivider = true
            )
            DetailRow(
                label = "INSTITUTION ATTENDED",
                value = record.institutionAttended?.name?.takeIf { it.isNotBlank() } ?: "—",
                showDivider = true
            )
            DetailRow(
                label = "GRADUATION DATE",
                value = record.graduationDate.ifEmpty { "—" },
                showDivider = false
            )
        }
    }
}

/**
 * Renders the license portrait by decoding **Base64** bytes only (raw string or `data:image/...;base64,...`).
 * HTTP(S) URLs are not fetched here — a placeholder icon is shown instead.
 */
@Composable
private fun PractitionerPhotoBase64(photoPayload: String?) {
    var imageBitmap by remember(photoPayload) { mutableStateOf<ImageBitmap?>(null) }
    var decoding by remember(photoPayload) { mutableStateOf(false) }

    LaunchedEffect(photoPayload) {
        imageBitmap = null
        val payload = extractLicensePhotoBase64Payload(photoPayload)
        if (payload == null) {
            decoding = false
            return@LaunchedEffect
        }
        decoding = true
        val bmp = withContext(Dispatchers.Default) {
            runCatching {
                val bytes = Base64.decode(payload, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }.getOrNull()
        }
        decoding = false
        imageBitmap = bmp?.asImageBitmap()
    }

    when {
        decoding -> CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = PrimaryGreen,
            strokeWidth = 3.dp
        )

        imageBitmap != null -> Image(
            bitmap = imageBitmap!!,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        else -> Icon(
            imageVector = Icons.Filled.VerifiedUser,
            contentDescription = null,
            tint = PrimaryGreen.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
    }
}

private fun extractLicensePhotoBase64Payload(photo: String?): String? {
    if (photo.isNullOrBlank()) return null
    val t = photo.trim().replace(Regex("\\s"), "")
    if (t.isEmpty()) return null
    if (t.startsWith("http://", ignoreCase = true) ||
        t.startsWith("https://", ignoreCase = true)
    ) {
        return null
    }
    if (t.startsWith("data:image", ignoreCase = true)) {
        val marker = "base64,"
        val idx = t.indexOf(marker, ignoreCase = true)
        if (idx == -1) return null
        return t.substring(idx + marker.length).trim()
    }
    return t
}

@Composable
private fun RecordDetailErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Network error illustration (design: cloud_off circle + floating wifi_off + sync_problem)
        Box(
            modifier = Modifier.size(256.dp),
            contentAlignment = Alignment.Center
        ) {
            // Circle with dashed-style border and cloud_off icon
            Box(
                modifier = Modifier
                    .size(192.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen.copy(alpha = 0.12f))
                    .border(
                        width = 4.dp,
                        color = PrimaryGreen.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudOff,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(80.dp)
                )
            }
            // Floating badge: wifi_off (top-right of circle)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Icon(
                        imageVector = Icons.Filled.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            // Floating badge: sync_problem (bottom-left of circle)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-16).dp, y = 32.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    Icon(
                        imageVector = Icons.Filled.SyncProblem,
                        contentDescription = null,
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Text(
            text = "Connection lost",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "We’re having trouble connecting to the national database. Please check your internet connection and try again.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable(onClick = onRetry),
                shape = RoundedCornerShape(12.dp),
                color = PrimaryGreen,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Retry connection",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
//            Surface(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(52.dp),
//                shape = RoundedCornerShape(12.dp),
//                color = MaterialTheme.colorScheme.surface,
//                border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f))
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxSize(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    Text(
//                        text = "Work offline",
//                        style = MaterialTheme.typography.titleSmall,
//                        fontWeight = FontWeight.Bold,
//                        color = PrimaryGreen
//                    )
//                }
//            }
        }

//        Row(
//            modifier = Modifier.padding(top = 8.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(8.dp)
//                    .clip(CircleShape)
//                    .background(MaterialTheme.colorScheme.error),
//            )
//            Text(
//                text = "Offline mode available",
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
    }
}

@Composable
private fun RecordDetailLoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Profile skeleton with spinning border
        Box(
            modifier = Modifier.size(128.dp),
            contentAlignment = Alignment.Center
        ) {
            val borderColor = PrimaryGreen.copy(alpha = 0.2f)
            val progressColor = PrimaryGreen
            CircularProgressIndicator(
                color = progressColor,
                strokeWidth = 4.dp,
                modifier = Modifier
                    .matchParentSize()
            )
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.VerifiedUser,
                    contentDescription = null,
                    tint = PrimaryGreen.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Text skeletons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .then(Modifier)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Box(
                modifier = Modifier
                    .height(18.dp)
                    .then(Modifier)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            )
        }

        // Progress card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Verification in progress",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "60%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(999.dp))
                            .background(PrimaryGreen)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Event,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Connecting to national database...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Detail skeleton section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "LICENSE DETAILS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
            repeat(3) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .height(14.dp)
                                .then(Modifier)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Box(
                            modifier = Modifier
                                .height(14.dp)
                                .then(Modifier)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordDetailNoRecordContent(
    registrationNumber: String,
    onRetry: () -> Unit,
    onManualEntry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Illustration
        Box(modifier = Modifier.size(192.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(192.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PersonSearch,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(2.dp, PrimaryGreen.copy(alpha = 0.3f)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = null,
                        tint = Color.Red.copy(alpha = 0.9f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Text(
            text = "No record found",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (registrationNumber.isNotBlank()) {
                "We couldn't find a practitioner matching license \"$registrationNumber\". Please try scanning again or enter the details manually."
            } else {
                "We couldn't find a practitioner matching this license number. Please try scanning again or enter the details manually."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable(onClick = onRetry),
                shape = RoundedCornerShape(12.dp),
                color = PrimaryGreen,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Try Again",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable(onClick = onManualEntry),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Manual Entry",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
            }
        }

        Text(
            text = "Need help with verification?",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
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
private fun IssueDateCard(
    modifier: Modifier = Modifier,
    issueDate: String = "Dec 2024"
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
                text = "ISSUE DATE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen.copy(alpha = 0.75f),
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
                    tint = PrimaryGreen.copy(alpha = 0.75f)
                )
                Text(
                    text = issueDate.ifEmpty { "—" },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
                color = PrimaryGreen.copy(alpha = 0.75f),
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
                    tint = PrimaryGreen.copy(alpha = 0.75f)
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
                color = PrimaryGreen.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.size(16.dp))
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
    certificateNo = "CHPRBN-CERT-2020-001",
    email = "jane.doe@example.org",
    phone = "+234 800 000 0000",
    licenseStatus = "Active",
    expiryDate = "Dec 2026",
    subtitle = "Medical Professional ID",
    issueDate = "Jan 2020",
    gender = "Female",
    graduationDate = "2019-06-30",
    institutionAttended = InstitutionAttended(name = "University of Lagos")
)

@Preview(showBackground = true)
@Composable
private fun RecordDetailScreenPreview() {
    ChprbnTheme {
        RecordDetailScreen(
            registrationNumber = "MED-12345",
            onBack = {},
            onMenu = {},
            onProceedToVerification = {},
            onReportIrregularity = {},
            onManualEntry = {},
            recordOverride = previewRecord
        )
    }
}
