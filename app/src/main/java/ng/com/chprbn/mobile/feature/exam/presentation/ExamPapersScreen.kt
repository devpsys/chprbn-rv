package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme

@Composable
fun ExamPapersScreen(
    viewModel: ExamPapersViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onOpenPaper: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    ExamPapersContent(
        uiState = uiState,
        onBack = onBack,
        onOpenPaper = onOpenPaper,
        onSyncNow = viewModel::onSyncNow
    )
    SyncOverlay(state = syncState)
}

@Preview(showBackground = true)
@Composable
private fun ExamPapersScreenPreview() {
    ChprbnTheme {
        ExamPapersContent(
            uiState = ExamPapersUiState.placeholder(),
            onBack = {},
            onOpenPaper = {},
            onSyncNow = {}
        )
    }
}
