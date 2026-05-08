package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme

@Composable
fun ExamCandidatesScreen(
    viewModel: ExamCandidatesViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onAddRemark: (candidateIdLabel: String) -> Unit = {},
    onViewProfile: (candidateIdLabel: String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExamCandidatesContent(
        uiState = uiState,
        onBack = onBack,
        onAddRemark = onAddRemark,
        onViewProfile = onViewProfile
    )
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

