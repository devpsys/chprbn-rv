package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
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
    ExamStatisticsContent(
        uiState = uiState,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onSyncNow = viewModel::onSyncNow,
        onClearCached = viewModel::onClearCached,
        onExamDashboardTab = onExamDashboardTab,
        onStatisticsTab = onStatisticsTab
    )
    SyncOverlay(state = syncState)
}

@Composable
internal fun SyncOverlay(state: SyncOperationUiState) {
    if (state is SyncOperationUiState.Syncing) {
        SyncingOverlay(
            title = stringResource(R.string.sync_loading_title),
            subtitle = stringResource(R.string.sync_loading_subtitle),
            encryptedLabel = stringResource(R.string.sync_loading_encrypted_badge),
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
