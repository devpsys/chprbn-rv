package ng.com.chprbn.mobile.feature.accreditation.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.ErrorRed
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen

/**
 * Reusable gallery + camera attachment capture UI (Prompt H).
 * Attached evidence is shown as a vertical list (thumbnail + metadata + remove).
 */
@Composable
fun AttachmentManager(
    attachments: List<SsqAttachmentUiState>,
    onAddAttachment: (contentUri: String, isPhoto: Boolean) -> Unit,
    onRemoveAttachment: (attachmentId: String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.accreditation_attachment_supporting_documents),
    emptyLabel: String = stringResource(R.string.accreditation_attachment_no_files),
    galleryLabel: String = stringResource(R.string.accreditation_attachment_gallery),
    cameraLabel: String = stringResource(R.string.accreditation_attachment_camera),
    errorText: String? = null,
) {
    val context = LocalContext.current
    var captureError by remember { mutableStateOf<String?>(null) }
    val displayError = errorText ?: captureError

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        captureError = null
        if (uri != null) {
            onAddAttachment(uri.toString(), true)
        }
    }

    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            pendingCaptureUri?.let { onAddAttachment(it.toString(), true) }
            captureError = null
        } else {
            captureError = context.getString(R.string.accreditation_attachment_capture_error)
        }
        pendingCaptureUri = null
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            runCatching {
                val uri = createAccreditationCaptureFileUri(context)
                pendingCaptureUri = uri
                takePicture.launch(uri)
            }.onFailure {
                captureError = context.getString(R.string.accreditation_attachment_capture_error)
            }
        } else {
            captureError = context.getString(R.string.accreditation_attachment_capture_error)
        }
    }

    val launchCamera: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            runCatching {
                val uri = createAccreditationCaptureFileUri(context)
                pendingCaptureUri = uri
                takePicture.launch(uri)
            }.onFailure {
                captureError = context.getString(R.string.accreditation_attachment_capture_error)
            }
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryGreen.copy(alpha = 0.75f),
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        if (attachments.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (displayError != null) {
                        ErrorRed
                    } else {
                        PrimaryGreen.copy(alpha = 0.15f)
                    },
                ),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        tint = PrimaryGreen.copy(alpha = 0.45f),
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = emptyLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                attachments.forEach { item ->
                    AttachmentEvidenceRow(
                        attachment = item,
                        onRemove = { onRemoveAttachment(item.id) },
                    )
                }
            }
        }

        displayError?.let { err ->
            Text(
                text = err,
                style = MaterialTheme.typography.bodySmall,
                color = ErrorRed,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = {
                    pickImage.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = galleryLabel,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OutlinedButton(
                onClick = launchCamera,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = cameraLabel,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun AttachmentEvidenceRow(
    attachment: SsqAttachmentUiState,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AttachmentListThumbnail(attachment = attachment)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attachment.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val location = attachment.locationLabel
                    if (!location.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Explore,
                                    contentDescription = stringResource(
                                        R.string.accreditation_attachment_location_cd,
                                    ),
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = location,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    Text(
                        text = attachment.sizeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            if (attachment.uploadState == AttachmentUploadUi.Uploading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(18.dp),
                    color = PrimaryGreen,
                    strokeWidth = 2.dp,
                )
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.accreditation_attachment_remove_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun AttachmentListThumbnail(
    attachment: SsqAttachmentUiState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val showPhoto = attachment.isPhoto && !attachment.contentUri.isNullOrBlank()

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        if (showPhoto) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(attachment.contentUri)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.accreditation_attachment_preview_cd),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = if (attachment.isPhoto) {
                    Icons.Filled.Image
                } else {
                    Icons.Filled.Description
                },
                contentDescription = null,
                tint = PrimaryGreen.copy(alpha = 0.55f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/** Compact square preview used by Prompt F / grid layouts. */
@Composable
internal fun AttachmentThumbnail(
    attachment: SsqAttachmentUiState,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val showPhoto = attachment.isPhoto && !attachment.contentUri.isNullOrBlank()

    Box(
        modifier = modifier
            .size(88.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
    ) {
        if (showPhoto) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(attachment.contentUri)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.accreditation_attachment_preview_cd),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (attachment.isPhoto) {
                        Icons.Filled.Image
                    } else {
                        Icons.Filled.Description
                    },
                    contentDescription = null,
                    tint = PrimaryGreen.copy(alpha = 0.55f),
                    modifier = Modifier.size(32.dp),
                )
            }
        }

        if (attachment.uploadState == AttachmentUploadUi.Uploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = PrimaryGreen,
                    strokeWidth = 2.dp,
                )
            }
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(28.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    shape = CircleShape,
                ),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.accreditation_attachment_remove_cd),
                tint = ErrorRed,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

fun createAccreditationCaptureFileUri(context: Context): Uri {
    val dir = File(context.cacheDir, "accreditation_attachments").apply { mkdirs() }
    val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
    if (!file.exists()) {
        file.createNewFile()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun AttachmentManagerEmptyPreview() {
    ChprbnTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            AttachmentManager(
                attachments = emptyList(),
                onAddAttachment = { _, _ -> },
                onRemoveAttachment = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun AttachmentManagerWithFilesPreview() {
    ChprbnTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            AttachmentManager(
                attachments = listOf(
                    SsqAttachmentUiState(
                        id = "1",
                        title = "Kaduna_State_Gazette_1998.pdf",
                        sizeLabel = "1.4 MB",
                        isPhoto = false,
                        locationLabel = "11.1114° N, 7.6432° E",
                    ),
                    SsqAttachmentUiState(
                        id = "2",
                        title = "Campus_Demo_Clinic_Exterior.jpg",
                        sizeLabel = "3.8 MB",
                        isPhoto = true,
                        locationLabel = "11.1119° N, 7.6428° E",
                    ),
                    SsqAttachmentUiState(
                        id = "3",
                        title = "Skills_Lab_Audit_01.jpg",
                        sizeLabel = "2.9 MB",
                        isPhoto = true,
                        locationLabel = "11.1117° N, 7.6430° E",
                        uploadState = AttachmentUploadUi.Uploading,
                    ),
                ),
                onAddAttachment = { _, _ -> },
                onRemoveAttachment = {},
            )
        }
    }
}
