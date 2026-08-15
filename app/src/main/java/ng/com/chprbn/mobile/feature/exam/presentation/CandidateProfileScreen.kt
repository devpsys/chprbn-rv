package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.components.ConfirmationDialog
import ng.com.chprbn.mobile.core.designsystem.components.ErrorDialog
import ng.com.chprbn.mobile.core.designsystem.components.ProgressOverlay
import ng.com.chprbn.mobile.core.designsystem.components.SuccessDialog

@Composable
fun CandidateProfileScreen(
    viewModel: CandidateProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clearRemarksState by viewModel.clearRemarksState.collectAsStateWithLifecycle()

    CandidateProfileContent(
        uiState = uiState,
        onBack = onBack,
        onClearAllRemarks = viewModel::onClearAllClicked,
    )

    ClearRemarksOverlay(
        state = clearRemarksState,
        candidateName = uiState.candidateName,
        onConfirm = viewModel::onClearAllConfirmed,
        onDismiss = viewModel::onClearAllDismissed,
    )
}

@Composable
private fun ClearRemarksOverlay(
    state: ClearRemarksUiState,
    candidateName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (state) {
        ClearRemarksUiState.Idle -> Unit
        ClearRemarksUiState.WarningShown -> ConfirmationDialog(
            title = stringResource(R.string.exam_candidate_profile_clear_warning_title),
            message = stringResource(
                R.string.exam_candidate_profile_clear_warning_message,
                candidateName,
            ),
            primaryButtonText = stringResource(R.string.exam_candidate_profile_clear_warning_confirm),
            secondaryButtonText = stringResource(R.string.exam_candidate_profile_clear_warning_cancel),
            onConfirm = onConfirm,
            onCancel = onDismiss,
            onDismiss = onDismiss,
        )
        ClearRemarksUiState.Clearing -> ProgressOverlay(
            icon = Icons.Outlined.DeleteSweep,
            title = stringResource(R.string.exam_candidate_profile_clear_loading_title),
            subtitle = stringResource(R.string.exam_candidate_profile_clear_loading_subtitle),
            encryptedLabel = stringResource(R.string.sync_loading_encrypted_badge),
        )
        ClearRemarksUiState.Success -> SuccessDialog(
            title = stringResource(R.string.exam_candidate_profile_clear_success_title),
            message = stringResource(R.string.exam_candidate_profile_clear_success_message),
            primaryButtonText = stringResource(R.string.action_ok),
            onPrimary = onDismiss,
            onDismiss = onDismiss,
        )
        is ClearRemarksUiState.Error -> ErrorDialog(
            title = stringResource(R.string.exam_candidate_profile_clear_error_title),
            message = state.message,
            primaryButtonText = stringResource(R.string.exam_candidate_profile_clear_error_action_retry),
            secondaryButtonText = stringResource(R.string.exam_candidate_profile_clear_error_action_close),
            onPrimary = onConfirm,
            onSecondary = onDismiss,
            onDismiss = onDismiss,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CandidateProfileScreenPreview() {
    ChprbnTheme {
        CandidateProfileContent(
            uiState = CandidateProfileUiState.placeholder(),
            onBack = {},
            onClearAllRemarks = {},
        )
    }
}
