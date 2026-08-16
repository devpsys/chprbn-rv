package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Practical-scoring screen — entered by tapping a section card on the
 * Practical Sections hub. Shows the section's questions with per-question
 * score steppers and a "Save Scores" Extended FAB. A successful save
 * pops back to the hub via [onSaveScores]; the hub's live section
 * summaries already reflect the committed scores.
 */
@Composable
fun AssessmentPracticalScoringScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onSaveScores: () -> Unit = {},
    viewModel: AssessmentPracticalScoringViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.sectionSaved.collect { onSaveScores() }
    }

    AssessmentPracticalScoringContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onInfoClick = onInfoClick,
        onIncrement = viewModel::onIncrement,
        onDecrement = viewModel::onDecrement,
        onSaveScores = viewModel::onSaveScores,
    )
}
