package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.components.DownloadWarningDialog
import ng.com.chprbn.mobile.core.designsystem.components.DownloadingOverlay
import ng.com.chprbn.mobile.core.designsystem.components.ErrorDialog
import ng.com.chprbn.mobile.core.designsystem.components.SuccessDialog

/**
 * Paper Detail — second screen of the assessment feature, reached by tapping
 * a card on the Examination Schedules screen. Shows the paper hero, check-in
 * progress, facility/hall cards, and a candidate directory preview.
 *
 * The header "More" action triggers the per-schedule package download flow
 * (warning → loading → result) owned by the VM.
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
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    AssessmentPaperDetailContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onShare = onShare,
        onMore = viewModel::onDownloadPackageClicked,
        onCandidateClick = onCandidateClick,
        onViewFullDirectory = onViewFullDirectory,
        onScanQr = onScanQr,
    )
    AssessmentDownloadPackageOverlay(
        state = downloadState,
        onConfirm = viewModel::onDownloadConfirmed,
        onDismiss = viewModel::onDownloadDismissed,
    )
}

@Composable
private fun AssessmentDownloadPackageOverlay(
    state: DownloadPackageUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (state) {
        DownloadPackageUiState.Idle -> Unit
        DownloadPackageUiState.WarningShown -> DownloadWarningDialog(
            title = stringResource(R.string.assessment_download_warning_title),
            message = stringResource(R.string.assessment_download_warning_message),
            footnote = stringResource(R.string.assessment_download_warning_footnote),
            primaryButtonText = stringResource(R.string.exam_download_warning_confirm),
            secondaryButtonText = stringResource(R.string.exam_download_warning_cancel),
            onConfirm = onConfirm,
            onCancel = onDismiss,
        )
        DownloadPackageUiState.Downloading -> DownloadingOverlay(
            title = stringResource(R.string.assessment_download_loading_title),
            subtitle = stringResource(R.string.assessment_download_loading_subtitle),
            encryptedLabel = stringResource(R.string.download_loading_encrypted_badge),
            statusLabel = stringResource(R.string.assessment_download_loading_status),
            progressFraction = 0.1f,
        )
        is DownloadPackageUiState.Success -> SuccessDialog(
            title = stringResource(R.string.assessment_download_success_title),
            message = stringResource(
                R.string.assessment_download_success_message_format,
                state.candidatesCount,
                state.sectionsCount,
                state.questionsCount,
            ),
            primaryButtonText = stringResource(R.string.action_ok),
            onPrimary = onDismiss,
            onDismiss = onDismiss,
        )
        is DownloadPackageUiState.Error -> ErrorDialog(
            title = stringResource(R.string.assessment_download_error_title),
            message = state.message,
            primaryButtonText = stringResource(R.string.exam_download_error_action_retry),
            secondaryButtonText = stringResource(R.string.exam_download_error_action_close),
            onPrimary = onConfirm,
            onSecondary = onDismiss,
            onDismiss = onDismiss,
        )
    }
}
