package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Raw score entry for accreditation criteria groups. Server computes grades;
 * this screen only captures officer-entered numbers locally.
 */
@Composable
fun ScoreEntryScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSubmitForReview: (institutionId: String) -> Unit = {},
    viewModel: ScoreEntryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val success = uiState as? ScoreEntryUiState.Success

    LaunchedEffect(success?.data?.requestSubmitNavigation) {
        if (success?.data?.requestSubmitNavigation == true) {
            viewModel.consumeSubmitNavigation()
            onSubmitForReview(success.data.institutionId)
        }
    }

    ScoreEntryContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onRawScoreChange = viewModel::onRawScoreChange,
        onObservationsChange = viewModel::onObservationsChange,
        onRemoveAttachment = viewModel::onRemoveAttachment,
        onAddPhoto = { /* TODO: Prompt H camera capture */ },
        onAddDocument = { /* TODO: Prompt H gallery / PDF */ },
        onSaveDraft = viewModel::onSaveDraft,
        onSubmitForServerReview = viewModel::onSubmitForServerReview,
        onDismissSavedDialog = viewModel::dismissSavedDialog,
    )
}
