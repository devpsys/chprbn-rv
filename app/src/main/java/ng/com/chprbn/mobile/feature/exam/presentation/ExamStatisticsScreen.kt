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
import ng.com.chprbn.mobile.core.designsystem.components.SyncingOverlay

@Composable
fun ExamStatisticsScreen(
    viewModel: ExamStatisticsViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onExamDashboardTab: () -> Unit = {},
    onStatisticsTab: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val clearCacheState by viewModel.clearCacheState.collectAsStateWithLifecycle()
    ExamStatisticsContent(
        uiState = uiState,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onSyncNow = viewModel::onSyncNow,
        onClearCached = viewModel::onClearCachedClicked,
        onExamDashboardTab = onExamDashboardTab,
        onStatisticsTab = onStatisticsTab
    )
    SyncOverlay(
        state = syncState,
        onDismissResult = viewModel::onSyncResultDismissed,
        title = stringResource(R.string.exam_statistics_sync_loading_title),
        subtitle = stringResource(R.string.exam_statistics_sync_loading_subtitle),
    )
    ClearCacheOverlay(
        state = clearCacheState,
        onConfirm = viewModel::onClearCacheConfirmed,
        onDismiss = viewModel::onClearCacheDismissed,
    )
}

/**
 * Shared by [ExamStatisticsScreen], `ExamPaperScreen`, `ExamPapersScreen`,
 * and the assessment paper-detail screen. Default copy is attendance;
 * assessment passes practical/project titles.
 */
@Composable
internal fun SyncOverlay(
    state: SyncOperationUiState,
    onDismissResult: () -> Unit = {},
    title: String = stringResource(R.string.sync_loading_title),
    subtitle: String = stringResource(R.string.sync_loading_subtitle),
) {
    when (state) {
        SyncOperationUiState.Idle -> Unit
        SyncOperationUiState.Syncing -> SyncingOverlay(
            title = title,
            subtitle = subtitle,
            encryptedLabel = stringResource(R.string.sync_loading_encrypted_badge),
        )
        is SyncOperationUiState.Result -> SuccessDialog(
            title = stringResource(R.string.exam_statistics_sync_result_title),
            message = when {
                state.succeeded == 0 && state.failed == 0 ->
                    stringResource(R.string.exam_statistics_sync_result_message_nothing_to_sync)
                state.failed == 0 -> stringResource(
                    R.string.exam_statistics_sync_result_message_all_synced_format,
                    state.succeeded,
                )
                else -> stringResource(
                    R.string.exam_statistics_sync_result_message_partial_format,
                    state.succeeded,
                    state.failed,
                )
            },
            primaryButtonText = stringResource(R.string.action_ok),
            onPrimary = onDismissResult,
            onDismiss = onDismissResult,
        )
    }
}

@Composable
internal fun ClearCacheOverlay(
    state: ClearCacheUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (state) {
        ClearCacheUiState.Idle -> Unit
        ClearCacheUiState.WarningShown -> ConfirmationDialog(
            title = stringResource(R.string.exam_statistics_clear_warning_title),
            message = stringResource(R.string.exam_statistics_clear_warning_message),
            primaryButtonText = stringResource(R.string.exam_statistics_clear_warning_confirm),
            secondaryButtonText = stringResource(R.string.exam_statistics_clear_warning_cancel),
            onConfirm = onConfirm,
            onCancel = onDismiss,
            onDismiss = onDismiss,
        )
        ClearCacheUiState.Clearing -> ProgressOverlay(
            icon = Icons.Outlined.DeleteSweep,
            title = stringResource(R.string.exam_statistics_clear_loading_title),
            subtitle = stringResource(R.string.exam_statistics_clear_loading_subtitle),
            encryptedLabel = stringResource(R.string.sync_loading_encrypted_badge),
        )
        ClearCacheUiState.Success -> SuccessDialog(
            title = stringResource(R.string.exam_statistics_clear_success_title),
            message = stringResource(R.string.exam_statistics_clear_success_message),
            primaryButtonText = stringResource(R.string.action_ok),
            onPrimary = onDismiss,
            onDismiss = onDismiss,
        )
        is ClearCacheUiState.Error -> ErrorDialog(
            title = stringResource(R.string.exam_statistics_clear_error_title),
            message = state.message,
            primaryButtonText = stringResource(R.string.exam_statistics_clear_error_action_retry),
            secondaryButtonText = stringResource(R.string.exam_statistics_clear_error_action_close),
            onPrimary = onConfirm,
            onSecondary = onDismiss,
            onDismiss = onDismiss,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExamStatisticsScreenPreview() {
    ChprbnTheme {
        ExamStatisticsContent(
            uiState = ExamStatisticsUiState.placeholder(),
            onBack = {},
            onRefresh = {},
            onSyncNow = {},
            onClearCached = {},
            onExamDashboardTab = {},
            onStatisticsTab = {}
        )
    }
}
