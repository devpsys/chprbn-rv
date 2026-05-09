package ng.com.chprbn.mobile.feature.verification.presentation

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen

/**
 * Typed entry for a registration / indexing value. Verification uses license wording;
 * [forExamIndexing] uses exam attendance and indexing wording.
 */
@Composable
fun ManualEntryScreen(
    modifier: Modifier = Modifier,
    forExamIndexing: Boolean = false,
    onBack: () -> Unit = {},
    onVerifyLicense: (String) -> Unit = {},
    viewModel: ManualEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ManualEntryContent(
        modifier = modifier,
        forExamIndexing = forExamIndexing,
        uiState = uiState,
        onBack = onBack,
        onLicenseNumberChange = viewModel::onLicenseNumberChange,
        onVerifyLicense = onVerifyLicense
    )
}

@Composable
fun ManualEntryContent(
    modifier: Modifier = Modifier,
    forExamIndexing: Boolean = false,
    uiState: ManualEntryUiState,
    onBack: () -> Unit = {},
    onLicenseNumberChange: (String) -> Unit = {},
    onVerifyLicense: (String) -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val headline = stringResource(
        if (forExamIndexing) R.string.manual_entry_exam_headline
        else R.string.manual_entry_license_headline
    )
    val description = stringResource(
        if (forExamIndexing) R.string.manual_entry_exam_description
        else R.string.manual_entry_license_description
    )
    val fieldLabel = stringResource(
        if (forExamIndexing) R.string.manual_entry_exam_field_label
        else R.string.manual_entry_license_field_label
    )
    val placeholder = stringResource(
        if (forExamIndexing) R.string.manual_entry_exam_placeholder
        else R.string.manual_entry_license_placeholder
    )
    val primaryButtonLabel = stringResource(
        if (forExamIndexing) R.string.manual_entry_exam_button
        else R.string.manual_entry_license_button
    )
    val trustCaption = stringResource(
        if (forExamIndexing) R.string.manual_entry_exam_trust_caption
        else R.string.manual_entry_license_trust_caption
    )
    val headerTitle = stringResource(
        if (forExamIndexing) R.string.manual_entry_exam_header
        else R.string.manual_entry_license_header
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = scheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ManualEntryHeader(onBack = onBack, title = headerTitle)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .padding(bottom = 96.dp)
            ) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = fieldLabel,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = uiState.licenseNumber,
                    onValueChange = onLicenseNumberChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    placeholder = {
                        Text(
                            placeholder,
                            color = scheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedIndicatorColor = PrimaryGreen,
                        unfocusedIndicatorColor = PrimaryGreen.copy(alpha = 0.2f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = PrimaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable {
                            val value = uiState.licenseNumber.trim()
                            if (value.isNotEmpty()) onVerifyLicense(value)
                        },
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryGreen,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = primaryButtonLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Trust indicator
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (forExamIndexing) {
                            Icons.AutoMirrored.Filled.Assignment
                        } else {
                            Icons.Filled.Gavel
                        },
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = PrimaryGreen.copy(alpha = 0.6f)
                    )
                    Text(
                        text = trustCaption,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
                        modifier = Modifier.padding(top = 12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun ManualEntryHeader(onBack: () -> Unit, title: String) {
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
                    contentDescription = stringResource(R.string.manual_entry_action_back),
                    tint = PrimaryGreen
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 48.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Verification")
@Composable
private fun ManualEntryScreenPreview() {
    ChprbnTheme {
        ManualEntryContent(uiState = ManualEntryUiState())
    }
}

@Preview(showBackground = true, name = "Exam indexing")
@Composable
private fun ManualEntryExamPreview() {
    ChprbnTheme {
        ManualEntryContent(
            forExamIndexing = true,
            uiState = ManualEntryUiState(),
        )
    }
}

