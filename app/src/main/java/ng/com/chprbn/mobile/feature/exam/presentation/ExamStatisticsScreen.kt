package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme

@Composable
fun ExamStatisticsScreen(
    viewModel: ExamStatisticsViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onSyncNow: () -> Unit = {},
    onClearCached: () -> Unit = {},
    onExamDashboardTab: () -> Unit = {},
    onStatisticsTab: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExamStatisticsContent(
        uiState = uiState,
        onBack = onBack,
        onRefresh = onRefresh,
        onSyncNow = onSyncNow,
        onClearCached = onClearCached,
        onExamDashboardTab = onExamDashboardTab,
        onStatisticsTab = onStatisticsTab
    )
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
