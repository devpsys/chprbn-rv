package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Paper Detail — second screen of the assessment feature, reached by tapping
 * a card on the Examination Schedules screen. Shows the paper hero, check-in
 * progress, facility/hall cards, and a candidate directory preview.
 */
@Composable
fun AssessmentPaperDetailScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onShare: () -> Unit = {},
    onMore: () -> Unit = {},
    onCandidateClick: (CandidateRowUiState) -> Unit = {},
    onViewFullDirectory: () -> Unit = {},
    onScanQr: () -> Unit = {},
    viewModel: AssessmentPaperDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AssessmentPaperDetailContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onShare = onShare,
        onMore = onMore,
        onCandidateClick = onCandidateClick,
        onViewFullDirectory = onViewFullDirectory,
        onScanQr = onScanQr,
    )
}
