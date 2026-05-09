package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Project Assessment screen — entered by tapping the "Assess Project"
 * Extended FAB on the Practical Sections hub. Captures a single
 * project-wide score for the candidate.
 */
@Composable
fun AssessmentProjectAssessmentScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onCancel: () -> Unit = {},
    onSaveScore: () -> Unit = {},
    viewModel: AssessmentProjectAssessmentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AssessmentProjectAssessmentContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onScoreChange = viewModel::onScoreChange,
        onCancel = onCancel,
        onSaveScore = onSaveScore,
    )
}
