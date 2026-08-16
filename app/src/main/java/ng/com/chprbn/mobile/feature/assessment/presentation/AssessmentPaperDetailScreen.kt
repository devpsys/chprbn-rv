package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.feature.exam.presentation.SyncOverlay

/**
 * Paper Detail — second screen of the assessment feature, reached by tapping
 * a card on the Examination Schedules screen. Shows the paper hero, check-in
 * progress, facility/hall cards, and a candidate directory preview.
 *
 * There is no per-schedule download action on this screen: practical
 * sections + questions are part of the exam dossier, so a fresh dossier
 * download populates everything this screen needs.
 */
@Composable
fun AssessmentPaperDetailScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onShare: () -> Unit = {},
    onCandidateClick: (CandidateRowUiState) -> Unit = {},
    onViewFullDirectory: () -> Unit = {},
    onScanQr: () -> Unit = {},
    viewModel: AssessmentPaperDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }

    AssessmentPaperDetailContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onShare = onShare,
        onMore = {},
        onCandidateClick = onCandidateClick,
        onViewFullDirectory = onViewFullDirectory,
        onScanQr = onScanQr,
        onSyncData = viewModel::onSyncData,
    )
    SyncOverlay(
        state = syncState,
        onDismissResult = viewModel::onSyncResultDismissed,
        title = stringResource(R.string.assessment_sync_loading_title),
        subtitle = stringResource(R.string.assessment_sync_loading_subtitle),
    )
}
