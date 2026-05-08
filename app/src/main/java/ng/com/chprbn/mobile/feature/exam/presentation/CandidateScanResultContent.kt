package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CandidateScanResultContent(
    uiState: CandidateScanResultUiState,
    onBack: () -> Unit,
    onMarkAttendance: () -> Unit,
    onCancel: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val scroll = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = scheme.background,
        topBar = {
            Surface(
                color = scheme.surface,
                border = BorderStroke(1.dp, scheme.outlineVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = scheme.primary,
                        )
                    }
                    Text(
                        text = "Scan Result",
                        style = typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.015).sp,
                        ),
                        color = scheme.onSurface,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(end = 48.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(bottom = 152.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = scheme.surface,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(128.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 4.dp,
                                        color = scheme.primary.copy(alpha = 0.12f),
                                        shape = CircleShape,
                                    )
                                    .background(scheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = scheme.onSurfaceVariant,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-4).dp, y = (-4).dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(scheme.primary)
                                    .border(4.dp, scheme.surface, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = scheme.onPrimary,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = uiState.candidateName,
                            style = typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.015).sp,
                            ),
                            color = scheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.examNumberLine,
                            style = typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = scheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = scheme.surface,
                        border = BorderStroke(1.dp, scheme.outlineVariant),
                        shadowElevation = 1.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VerifiedUser,
                                    contentDescription = null,
                                    tint = scheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = uiState.verificationSectionLabel,
                                    style = typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.08.sp,
                                    ),
                                    color = scheme.onSurfaceVariant,
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = uiState.identityVerifiedHeadline,
                                    style = typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.015).sp,
                                    ),
                                    color = scheme.onSurface,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = scheme.tertiaryContainer,
                                ) {
                                    Text(
                                        text = uiState.matchLabel,
                                        style = typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = scheme.onTertiaryContainer,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DetailRow(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Event,
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        label = uiState.examDateCaption,
                        value = uiState.examDateValue,
                    )
                    DetailRow(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        label = uiState.testingCenterCaption,
                        value = uiState.testingCenterValue,
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 24.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(
                    onClick = onCancel,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = scheme.surface,
                    contentColor = scheme.onSurfaceVariant,
                    border = BorderStroke(1.dp, scheme.outline),
                    shadowElevation = 6.dp,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cancel",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                ExtendedFloatingActionButton(
                    onClick = onMarkAttendance,
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 10.dp,
                        pressedElevation = 12.dp,
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mark Attendance",
                            style = typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    leadingIcon: @Composable () -> Unit,
    label: String,
    value: String,
) {
    val scheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                leadingIcon()
                Text(
                    text = label,
                    style = typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = scheme.onSurfaceVariant,
                )
            }
            Text(
                text = value,
                style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = scheme.onSurface,
            )
        }
    }
}
