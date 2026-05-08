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
    onOpenPaper: () -> Unit = {},
    onSyncNow: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExamPapersContent(
        uiState = uiState,
        onBack = onBack,
        onOpenPaper = onOpenPaper,
        onSyncNow = onSyncNow
    )
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

