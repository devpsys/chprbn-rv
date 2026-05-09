package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Practical Sections Hub — landed by the assessment-side QR scan once a
 * candidate's code has been read. Shows the candidate summary, three
 * section progress cards (Complete / Incomplete / Not Started), and a
 * single "Assess Project" Extended FAB.
 */
@Composable
fun AssessmentPracticalSectionsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSectionClick: (PracticalSectionUiState) -> Unit = {},
    onAssessProject: () -> Unit = {},
    viewModel: AssessmentPracticalSectionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AssessmentPracticalSectionsContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onSectionClick = onSectionClick,
        onAssessProject = onAssessProject,
    )
}
