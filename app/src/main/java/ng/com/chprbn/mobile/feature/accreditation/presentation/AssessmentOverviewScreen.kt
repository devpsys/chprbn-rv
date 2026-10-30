package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Assessment Overview — SSQ A–F progress tracker. Reached from Institution
 * Profile "Start New Assessment". Section taps and continue/save are
 * nav-host callbacks until Checklist (Prompt D) lands.
 */
@Composable
fun AssessmentOverviewScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSectionClick: (institutionId: String, sectionKey: String) -> Unit = { _, _ -> },
    onContinueToChecklist: (institutionId: String) -> Unit = {},
    onSaveAllDrafts: (institutionId: String) -> Unit = {},
    viewModel: AssessmentOverviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AssessmentOverviewContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onSectionClick = onSectionClick,
        onContinueToChecklist = onContinueToChecklist,
        onSaveAllDrafts = onSaveAllDrafts,
    )
}
