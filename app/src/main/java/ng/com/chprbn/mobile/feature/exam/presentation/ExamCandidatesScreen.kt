package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme

@Composable
fun ExamCandidatesScreen(
    viewModel: ExamCandidatesViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onViewProfile: (candidateId: String, paperId: String) -> Unit = { _, _ -> },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val remarkDialogState by viewModel.remarkDialogState.collectAsStateWithLifecycle()

    // The candidate profile screen (remark clearing) returns here via
    // popBackStack, not a fresh navigate(), so this ViewModel instance
    // survives the round trip — reload on every resume (covers first
    // entry too) so a cleared candidate's remark count isn't left stale.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }

    ExamCandidatesContent(
        uiState = uiState,
        onBack = onBack,
        onAddRemark = viewModel::onAddRemarkClicked,
        onViewProfile = { candidateId -> onViewProfile(candidateId, viewModel.paperId) },
        onQueryChange = viewModel::onQueryChange,
        onFilterSelected = viewModel::onFilterChange,
    )

    val dialogState = remarkDialogState
    if (dialogState is AddRemarkUiState.Open) {
        AddRemarkDialog(
            state = dialogState,
            onSelectType = viewModel::onSelectRemarkType,
            onDismiss = viewModel::onDismissRemarkDialog,
            onSave = viewModel::onSaveRemark,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExamCandidatesScreenPreview() {
    ChprbnTheme {
        ExamCandidatesContent(
            uiState = ExamCandidatesUiState.placeholder(),
            onBack = {},
            onAddRemark = {},
            onViewProfile = {}
        )
    }
}
