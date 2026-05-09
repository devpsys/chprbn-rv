package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Full candidates directory for an assessment paper, reached from the
 * Paper Detail's "View Full Directory" button. Renders as either a
 * vertical list or a 2-column grid; the toggle sits next to the search
 * bar.
 */
@Composable
fun AssessmentCandidatesScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onCandidateClick: (CandidateCardUiState) -> Unit = {},
    onAddRemark: (CandidateCardUiState) -> Unit = {},
    viewModel: AssessmentCandidatesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AssessmentCandidatesContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onQueryChange = viewModel::onQueryChange,
        onViewModeChange = viewModel::onViewModeChange,
        onCandidateClick = onCandidateClick,
        onAddRemark = onAddRemark,
    )
}
