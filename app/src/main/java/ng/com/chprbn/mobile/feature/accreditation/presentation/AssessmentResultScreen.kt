package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Displays server-computed accreditation grade and decision.
 * Grades / quotas / period are read-only placeholders until Prompt I sync.
 */
@Composable
fun AssessmentResultScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onDone: () -> Unit = {},
    viewModel: AssessmentResultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AssessmentResultContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onDone = onDone,
        onViewCertificate = viewModel::onViewCertificate,
        onDismissCertificateDialog = viewModel::dismissCertificateDialog,
        onRetry = viewModel::retryLoad,
        onDismissError = viewModel::dismissErrorDialog,
    )
}
