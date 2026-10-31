package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Confirmation before transmitting a local accreditation draft.
 * Owns SyncingOverlay; on success navigates to Assessment Result.
 */
@Composable
fun SubmitForReviewScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSubmitted: (institutionId: String, decisionId: String) -> Unit = { _, _ -> },
    viewModel: SubmitForReviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val success = uiState as? SubmitForReviewUiState.Success

    LaunchedEffect(success?.data?.requestResultNavigation) {
        val data = success?.data
        if (data?.requestResultNavigation == true && data.submittedDecisionId.isNotBlank()) {
            viewModel.consumeResultNavigation()
            onSubmitted(data.institutionId, data.submittedDecisionId)
        }
    }

    SubmitForReviewContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onCancel = onBack,
        onSubmitAssessment = viewModel::onSubmitAssessment,
        onRetrySubmit = viewModel::retrySubmit,
        onDismissError = viewModel::dismissErrorDialog,
    )
}
