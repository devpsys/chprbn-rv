package ng.com.chprbn.mobile.feature.verification.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.feature.verification.domain.model.IrregularityRemark

@Composable
fun ReportIrregularityScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSubmitted: () -> Unit = {},
    viewModel: ReportIrregularityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.submitState) {
        if (uiState.submitState is ReportIrregularitySubmitState.Success) {
            viewModel.consumeSubmitState()
            onSubmitted()
        }
    }

    ReportIrregularityContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onNameOnCardChange = viewModel::onNameOnCardChange,
        onLicenseNumberChange = viewModel::onLicenseNumberChange,
        onCadreChange = viewModel::onCadreChange,
        onGenderChange = viewModel::onGenderChange,
        onRemarkSelected = viewModel::onRemarkSelected,
        onSnapshotUriChange = viewModel::onSnapshotUriChange,
        onClearSnapshot = viewModel::clearSnapshot,
        onSubmit = viewModel::submit
    )
}

@Composable
fun ReportIrregularityContent(
    modifier: Modifier = Modifier,
    uiState: ReportIrregularityUiState,
    onBack: () -> Unit = {},
    onNameOnCardChange: (String) -> Unit = {},
    onLicenseNumberChange: (String) -> Unit = {},
    onCadreChange: (String) -> Unit = {},
    onGenderChange: (String) -> Unit = {},
    onRemarkSelected: (IrregularityRemark) -> Unit = {},
    onSnapshotUriChange: (String) -> Unit = {},
    onClearSnapshot: () -> Unit = {},
    onSubmit: () -> Unit = {}
) {
    val context = LocalContext.current

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onSnapshotUriChange(uri?.toString() ?: "")
    }

    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCaptureUri?.let { onSnapshotUriChange(it.toString()) }
        }
        pendingCaptureUri = null
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createIrregularitySnapshotFileUri(context)
            pendingCaptureUri = uri
            takePicture.launch(uri)
        }
    }

    val launchCamera: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            val uri = createIrregularitySnapshotFileUri(context)
            pendingCaptureUri = uri
            takePicture.launch(uri)
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
        ReportIrregularityHeader(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .padding(bottom = 120.dp)
        ) {
            Text(
                text = "Report license irregularity",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )
            Text(
                text = "Provide accurate details from the physical license. A clear photo helps investigations.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            FormOutlinedField(
                label = "Name on card",
                value = uiState.nameOnCard,
                onValueChange = onNameOnCardChange,
                error = uiState.fieldErrors.nameOnCard
            )
            Spacer(modifier = Modifier.height(20.dp))

            FormOutlinedField(
                label = "License number",
                value = uiState.licenseNumber,
                onValueChange = onLicenseNumberChange,
                error = uiState.fieldErrors.licenseNumber
            )
            Spacer(modifier = Modifier.height(20.dp))

            FormOutlinedField(
                label = "Cadre",
                value = uiState.cadre,
                onValueChange = onCadreChange,
                error = uiState.fieldErrors.cadre
            )
            Spacer(modifier = Modifier.height(20.dp))

            FormOutlinedField(
                label = "Gender",
                value = uiState.gender,
                onValueChange = onGenderChange,
                error = uiState.fieldErrors.gender
            )
            Spacer(modifier = Modifier.height(20.dp))

            RemarkDropdown(
                selected = uiState.selectedRemark,
                onSelect = onRemarkSelected,
                error = uiState.fieldErrors.remark
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Snapshot (license / document)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (uiState.fieldErrors.snapshot != null) MaterialTheme.colorScheme.error.copy(
                        alpha = 0.6f
                    )
                    else PrimaryGreen.copy(alpha = 0.15f)
                )
            ) {
                if (uiState.snapshotContentUri.isNullOrBlank()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = null,
                            tint = PrimaryGreen.copy(alpha = 0.45f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No image selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(uiState.snapshotContentUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "License snapshot preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        IconButton(
                            onClick = { onClearSnapshot() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove image",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            uiState.fieldErrors.snapshot?.let { err ->
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        pickImage.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (uiState.snapshotContentUri.isNullOrBlank()) "Gallery"
                        else "Replace (gallery)",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                OutlinedButton(
                    onClick = launchCamera,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (uiState.snapshotContentUri.isNullOrBlank()) "Camera"
                        else "Retake (camera)",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            val submitErr = uiState.submitState as? ReportIrregularitySubmitState.Error
            if (submitErr != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = submitErr.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val submitting = uiState.submitState is ReportIrregularitySubmitState.Submitting
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = !submitting,
                            onClick = { onSubmit() }
                        ),
                    shape = RoundedCornerShape(12.dp),
                    color = if (submitting) PrimaryGreen.copy(alpha = 0.5f) else PrimaryGreen
                ) {
                    Text(
                        text = if (submitting) "Submitting…" else "Submit report",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun FormOutlinedField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = error != null,
            supportingText = if (error != null) {
                { Text(error, color = MaterialTheme.colorScheme.error) }
            } else null,
            shape = RoundedCornerShape(12.dp),
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                focusedIndicatorColor = PrimaryGreen,
                unfocusedIndicatorColor = PrimaryGreen.copy(alpha = 0.2f),
                errorIndicatorColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = PrimaryGreen
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemarkDropdown(
    selected: IrregularityRemark?,
    onSelect: (IrregularityRemark) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Remark",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selected?.displayLabel.orEmpty(),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                placeholder = {
                    Text(
                        "Select remark",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                isError = error != null,
                supportingText = if (error != null) {
                    { Text(error, color = MaterialTheme.colorScheme.error) }
                } else null,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = PrimaryGreen.copy(alpha = 0.2f),
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = PrimaryGreen
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 280.dp)
            ) {
                IrregularityRemark.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayLabel) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportIrregularityHeader(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Irregularity report",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

private fun createIrregularitySnapshotFileUri(context: Context): Uri {
    val dir = File(context.cacheDir, "irregularity_snapshots").apply { mkdirs() }
    val file = File(dir, "snap_${System.currentTimeMillis()}.jpg")
    if (!file.exists()) {
        file.createNewFile()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}
