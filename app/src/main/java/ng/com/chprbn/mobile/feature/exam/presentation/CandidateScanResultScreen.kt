package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
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
    val markAttendanceState by viewModel.markAttendanceState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.attendanceMarked.collect { onMarkAttendance() }
    }

    CandidateScanResultContent(
        uiState = uiState,
        markAttendanceState = markAttendanceState,
        onBack = onBack,
        onMarkAttendance = viewModel::onMarkAttendance,
        onDismissMarkAttendanceError = viewModel::dismissMarkAttendanceError,
        onCancel = onCancel,
    )
}

@Preview(showBackground = true)
@Composable
private fun CandidateScanResultScreenPreview() {
    val context = LocalContext.current
    ChprbnTheme {
        CandidateScanResultContent(
            uiState = CandidateScanResultUiState.fromScannedPayload("", context),
            onBack = {},
            onMarkAttendance = {},
            onCancel = {},
        )
    }
}
