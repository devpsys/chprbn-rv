package ng.com.chprbn.mobile.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme

/**
 * Full-screen blocking overlay shown while a long-running secure
 * operation is in flight. Used by both download and sync flows so the
 * visual is consistent.
 *
 * Renders as a [Dialog] (uncancelable) so the host screen stays
 * composed underneath, but no taps reach it. Pass [progressFraction]
 * (0f..1f) to render a determinate progress bar; pass `null` for the
 * indeterminate spinner variant.
 */
@Composable
fun ProgressOverlay(
    icon: ImageVector,
    title: String,
    subtitle: String,
    encryptedLabel: String,
    statusLabel: String? = null,
    progressFraction: Float? = null,
) {
    Dialog(
        onDismissRequest = { /* uncancelable */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (progressFraction == null) {
                    SpinningIconBadge(icon = icon)
                } else {
                    StaticIconBadge(icon = icon)
                }
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .padding(top = 8.dp),
                )
                Spacer(modifier = Modifier.height(32.dp))
                if (progressFraction != null) {
                    val safeFraction = progressFraction.coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { safeFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = statusLabel?.uppercase().orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${(safeFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                EncryptedBadge(label = encryptedLabel)
            }
        }
    }
}

@Composable
private fun StaticIconBadge(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(120.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp),
            )
        }
    }
}

@Composable
private fun SpinningIconBadge(icon: ImageVector) {
    val transition = rememberInfiniteTransition(label = "spinning_icon")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spin_angle",
    )
    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(160.dp)
                .rotate(angle),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 12.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp),
                )
            }
        }
    }
}

@Composable
private fun EncryptedBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

/**
 * Convenience shape for the dossier / package download flow. Matches
 * `ui-designs/exams/download_loading_screen_1`: static cloud_download
 * glyph + determinate progress bar.
 */
@Composable
fun DownloadingOverlay(
    title: String,
    subtitle: String,
    encryptedLabel: String,
    statusLabel: String,
    progressFraction: Float,
) {
    ProgressOverlay(
        icon = Icons.Filled.CloudDownload,
        title = title,
        subtitle = subtitle,
        encryptedLabel = encryptedLabel,
        statusLabel = statusLabel,
        progressFraction = progressFraction,
    )
}

/**
 * Convenience shape for the sync flow. Matches
 * `ui-designs/exams/sync_loading_screen`: spinning cloud_upload glyph,
 * no determinate progress.
 */
@Composable
fun SyncingOverlay(
    title: String,
    subtitle: String,
    encryptedLabel: String,
) {
    ProgressOverlay(
        icon = Icons.Filled.CloudUpload,
        title = title,
        subtitle = subtitle,
        encryptedLabel = encryptedLabel,
        progressFraction = null,
    )
}

@Preview(showBackground = true)
@Composable
private fun DownloadingOverlayPreview() {
    ChprbnTheme {
        DownloadingOverlay(
            title = "Downloading Candidate Records…",
            subtitle = "Please do not close the app. Fetching data from secure registry.",
            encryptedLabel = "End-to-End Encrypted",
            statusLabel = "Synchronizing…",
            progressFraction = 0.65f,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncingOverlayPreview() {
    ChprbnTheme {
        SyncingOverlay(
            title = "Syncing Attendance Data…",
            subtitle = "Securely uploading verified records to the central server.",
            encryptedLabel = "Encrypted",
        )
    }
}
