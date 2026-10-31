package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Accreditation outbox sync monitor. Close maps to [onBack].
 */
@Composable
fun SyncStatusScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: SyncStatusViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SyncStatusContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onRetryDraft = viewModel::onRetryDraft,
        onRetryFailed = viewModel::onRetryFailed,
        onDismissRetrySuccess = viewModel::dismissRetrySuccess,
    )
}
