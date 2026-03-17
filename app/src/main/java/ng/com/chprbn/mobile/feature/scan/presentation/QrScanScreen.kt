package ng.com.chprbn.mobile.feature.scan.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen

@Composable
fun QrScanScreen(
    viewModel: QrScanViewModel = hiltViewModel(),
    onManualEntry: () -> Unit = {},
    onQrScanned: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val cameraViewportHeight = screenHeight * 0.75f // 75% of screen height

    // Navigate to student details when a registration is scanned
    LaunchedEffect(uiState.scannedRegistrationNumber) {
        uiState.scannedRegistrationNumber?.let { registrationNumber ->
            Log.d("ScanScreen", "Scanned: $registrationNumber")
            onQrScanned(registrationNumber)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Main Camera Viewport - Takes 65% of screen height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cameraViewportHeight) // Explicitly set to 65% of screen height
                    .background(Color(0xFF0F172A)) // Dark slate background
            ) {
                CameraScanPreview(
                    isTorchOn = uiState.isTorchOn,
                    onQrScanned = { value ->
                        viewModel.handleEvent(ScanUiEvent.RegistrationScanned(value))
                    }
                )

                // Scanning Overlay (unchanged)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Instruction Text
                    Text(
                        text = "Position the practitioner's QR within the frame",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )

                    // Scanning Frame
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // (Corner boxes, scan line, and inner QR icon copied from original ScanScreen)
                        val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
                        val scanLineProgress by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1_800),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scanLineProgress"
                        )
                        // Top-left, top-right, bottom-left, bottom-right corners...
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .width(48.dp)
                                .height(48.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .width(48.dp)
                                    .height(4.dp)
                                    .background(PrimaryGreen)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .width(4.dp)
                                    .height(48.dp)
                                    .background(PrimaryGreen)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .width(48.dp)
                                .height(48.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .width(48.dp)
                                    .height(4.dp)
                                    .background(PrimaryGreen)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .width(4.dp)
                                    .height(48.dp)
                                    .background(PrimaryGreen)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .width(48.dp)
                                .height(48.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .width(48.dp)
                                    .height(4.dp)
                                    .background(PrimaryGreen)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .width(4.dp)
                                    .height(48.dp)
                                    .background(PrimaryGreen)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .width(48.dp)
                                .height(48.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .width(48.dp)
                                    .height(4.dp)
                                    .background(PrimaryGreen)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .width(4.dp)
                                    .height(48.dp)
                                    .background(PrimaryGreen)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .graphicsLayer {
                                    translationY = (scanLineProgress * 200 - 100).dp.toPx()
                                }
                                .background(PrimaryGreen.copy(alpha = 0.8f))
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.White.copy(alpha = 0.2f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Torch Toggle Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            .clickable {
                                viewModel.handleEvent(ScanUiEvent.ToggleTorch)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashlightOn,
                            contentDescription = "Toggle Torch",
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            // Footer (status, progress, manual entry, help) – can be re-used or adjusted as needed
            QrScanFooter(
                statusText = "Waiting for QR code...",
                onManualEntry = onManualEntry
            )
        }
    }
}

@Composable
@SuppressLint("UnsafeOptInUsageError")
private fun CameraScanPreview(
    isTorchOn: Boolean,
    onQrScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hasScanned = remember { mutableStateOf(false) }
    val currentOnQrScanned by rememberUpdatedState(onQrScanned)
    val cameraRef = remember { mutableStateOf<Camera?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Log.e("ScanScreen", "Camera permission denied for ScanScreen")
        } else {
            Log.d("ScanScreen", "Camera permission granted for ScanScreen")
        }
    }

    LaunchedEffect(Unit) {
        Log.d("ScanScreen", "requesting CAMERA permission")
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener(
                {
                    try {
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        val options = BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                            .build()
                        val scanner = BarcodeScanning.getClient(options)

                        analysis.setAnalyzer(
                            ContextCompat.getMainExecutor(ctx)
                        ) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage == null) {
                                imageProxy.close()
                                return@setAnalyzer
                            }

                            if (hasScanned.value) {
                                imageProxy.close()
                                return@setAnalyzer
                            }

                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    val qr = barcodes.firstOrNull { it.rawValue != null }
                                    val value = qr?.rawValue
                                    if (!value.isNullOrBlank() && !hasScanned.value) {
                                        Log.d("ScanScreen", "ScanScreen - QR detected: $value")
                                        hasScanned.value = true
                                        currentOnQrScanned(value)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("ScanScreen", "ScanScreen - QR scan failed")
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            analysis
                        )
                        cameraRef.value = camera
                        Log.d("ScanScreen", "ScanScreen - CameraX initialized")
                    } catch (e: Exception) {
                        Log.e("ScanScreen", "ScanScreen - Failed to start camera")
                    }
                },
                ContextCompat.getMainExecutor(ctx)
            )

            previewView
        },
        update = {
            cameraRef.value?.cameraControl?.enableTorch(isTorchOn)
        }
    )
}

@Composable
fun QrScanFooter(
    statusText: String,
    onManualEntry: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scanning status",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryGreen.copy(alpha = 0.7f)
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryGreen
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFE5E7EB))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.33f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(999.dp))
                            .background(PrimaryGreen)
                    )
                }
            }

            // Manual entry button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onManualEntry),
                color = PrimaryGreen,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Keyboard,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Enter License Manually",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Text(
                text = "Having trouble? Ensure the QR code is well-lit and not reflective.",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

