package ng.com.chprbn.mobile.feature.verified.presentation

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.PrimaryGreen
import ng.com.chprbn.mobile.feature.scan.domain.model.InstitutionAttended
import ng.com.chprbn.mobile.feature.scan.domain.model.LicenseRecord

/**
 * Verification form screen (presentation layer).
 * Officer remark is chosen from a fixed list; [licenseRecord] is the source of truth when provided.
 */
@Composable
fun VerificationFormScreen(
    modifier: Modifier = Modifier,
    licenseRecord: LicenseRecord? = null,
    lastVerifiedText: String = "Last verified: Oct 24, 2023 at 09:45 AM",
    onBack: () -> Unit = {},
    onSaveVerification: () -> Unit = {},
    onReportIrregularity: () -> Unit = {},
    viewModel: VerificationFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val record = licenseRecord ?: uiState.licenseRecord

    LaunchedEffect(uiState.saveState) {
        if (uiState.saveState is SaveVerificationState.Success) {
            viewModel.consumeSaveState()
            onSaveVerification()
        }
    }

    VerificationFormContent(
        modifier = modifier,
        uiState = uiState,
        lastVerifiedText = lastVerifiedText,
        onBack = onBack,
        onSaveVerification = viewModel::saveVerification,
        onReportIrregularity = onReportIrregularity,
        onOfficerRemarkSelected = viewModel::onOfficerRemarkSelected
    )
}

@Composable
fun VerificationFormContent(
    modifier: Modifier = Modifier,
    uiState: VerificationFormUiState,
    lastVerifiedText: String = "Last verified: Oct 24, 2023 at 09:45 AM",
    onBack: () -> Unit = {},
    onSaveVerification: () -> Unit = {},
    onReportIrregularity: () -> Unit = {},
    onOfficerRemarkSelected: (String) -> Unit = {}
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        VerificationFormHeader(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PrimaryGreen.copy(alpha = 0.1f))
            )
            Spacer(modifier = Modifier.height(24.dp))

            OfficerRemarkDropdown(
                selectedRemark = uiState.selectedOfficerRemark,
                options = VerificationFormViewModel.officerRemarkOptions,
                onSelect = onOfficerRemarkSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onReportIrregularity,
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

            val saveError = uiState.saveState as? SaveVerificationState.Error
            if (saveError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = saveError.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        VerificationFormFooter(
            onSaveVerification = onSaveVerification,
            lastVerifiedText = lastVerifiedText
        )
    }
}

@Composable
private fun OfficerRemarkDropdown(
    selectedRemark: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Officer remark",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedRemark,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                placeholder = {
                    Text(
                        "Select an option",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = PrimaryGreen.copy(alpha = 0.8f)
                    )
                },
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedIndicatorColor = PrimaryGreen,
                    unfocusedIndicatorColor = PrimaryGreen.copy(alpha = 0.35f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    disabledIndicatorColor = PrimaryGreen.copy(alpha = 0.35f),
                    cursorColor = PrimaryGreen
                )
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VerificationFormHeader(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Verify Practitioner",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 48.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun VerificationFormFooter(
    onSaveVerification: () -> Unit,
    lastVerifiedText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSaveVerification),
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
                        text = "Save Verification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Text(
                text = lastVerifiedText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VerificationFormScreenPreview() {
    val previewRecord = LicenseRecord(
        registrationNumber = "MED-99284-TX",
        fullName = "Dr. Sarah Elizabeth Jenkins",
        photoUrl = null,
        profession = "Physician",
        certificateNo = "CERT-MED-PREV-001",
        email = "sarah.jenkins@preview.ng",
        phone = "+2348000000000",
        licenseStatus = "Active",
        expiryDate = "Mar 2027",
        subtitle = "Registered Practitioner",
        issueDate = "2021-03-15",
        gender = "Female",
        graduationDate = "2020-11-20",
        institutionAttended = InstitutionAttended(name = "College of Medicine, UNN")
    )
    ChprbnTheme {
        VerificationFormContent(
            uiState = VerificationFormUiState(licenseRecord = previewRecord)
        )
    }
}
