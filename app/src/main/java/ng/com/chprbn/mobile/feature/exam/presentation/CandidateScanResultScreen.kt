package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme

@Composable
fun CandidateScanResultScreen(
    viewModel: CandidateScanResultViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onMarkAttendance: () -> Unit,
    onCancel: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CandidateScanResultContent(
        uiState = uiState,
        onBack = onBack,
        onMarkAttendance = onMarkAttendance,
        onCancel = onCancel,
    )
}

@Preview(showBackground = true)
@Composable
private fun CandidateScanResultScreenPreview() {
    ChprbnTheme {
        CandidateScanResultContent(
            uiState = CandidateScanResultUiState.fromScannedPayload(""),
            onBack = {},
            onMarkAttendance = {},
            onCancel = {},
        )
    }
}
