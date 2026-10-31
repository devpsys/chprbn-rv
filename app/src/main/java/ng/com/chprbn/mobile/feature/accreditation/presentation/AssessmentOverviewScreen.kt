package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Assessment Overview — SSQ A–F progress tracker. Reached from Institution
 * Profile "Start New Assessment". Continue navigates to Criteria Checklist;
 * Save All Drafts marks sections saved locally (presentation until Prompt I).
 */
@Composable
fun AssessmentOverviewScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSectionClick: (institutionId: String, sectionKey: String) -> Unit = { _, _ -> },
    onContinueToChecklist: (institutionId: String) -> Unit = {},
    viewModel: AssessmentOverviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AssessmentOverviewContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onSectionClick = onSectionClick,
        onContinueToChecklist = onContinueToChecklist,
        onSaveAllDrafts = { viewModel.saveAllDrafts() },
    )
}
