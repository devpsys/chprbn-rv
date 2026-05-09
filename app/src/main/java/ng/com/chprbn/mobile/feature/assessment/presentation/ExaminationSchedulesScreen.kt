package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Examination Schedules — first screen of the assessment feature, reached
 * from the Exam Dashboard's "Grade Practical" action. Renders the list of
 * scheduled assessments with sync-status pills and a help banner.
 */
@Composable
fun ExaminationSchedulesScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onScheduleClick: (ScheduleCardUiState) -> Unit = {},
    viewModel: ExaminationSchedulesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExaminationSchedulesContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onScheduleClick = onScheduleClick,
    )
}
