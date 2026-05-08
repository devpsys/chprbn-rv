package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme

@Composable
fun ExamPaperScreen(
    viewModel: ExamPaperViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onViewCandidates: () -> Unit = {},
    onSyncData: () -> Unit = {},
    onScanQr: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExamPaperContent(
        uiState = uiState,
        onBack = onBack,
        onViewCandidates = onViewCandidates,
        onSyncData = onSyncData,
        onScanQr = onScanQr
    )
}

@Preview(showBackground = true)
@Composable
private fun ExamPaperScreenPreview() {
    ChprbnTheme {
        ExamPaperContent(
            uiState = ExamPaperUiState.placeholder(),
            onBack = {},
            onViewCandidates = {},
            onSyncData = {},
            onScanQr = {}
        )
    }
}
