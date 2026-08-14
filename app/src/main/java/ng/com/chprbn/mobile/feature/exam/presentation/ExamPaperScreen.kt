package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme

@Composable
fun ExamPaperScreen(
    viewModel: ExamPaperViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onViewCandidates: () -> Unit = {},
    onScanQr: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    // The QR scan flow returns here via popBackStack, not a fresh
    // navigate(), so this ViewModel instance survives the round trip —
    // reload on every resume (covers first entry too) so a check-in
    // recorded on the scan-result screen isn't left stale here.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }

    ExamPaperContent(
        uiState = uiState,
        onBack = onBack,
        onViewCandidates = onViewCandidates,
        onSyncData = viewModel::onSyncData,
        onScanQr = onScanQr
    )
    SyncOverlay(state = syncState)
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
