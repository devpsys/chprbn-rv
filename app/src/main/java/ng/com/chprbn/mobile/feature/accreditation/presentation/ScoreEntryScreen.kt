package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Raw score entry for accreditation criteria groups. Server computes grades;
 * this screen only captures officer-entered numbers locally.
 * "Submit for Review" navigates to the confirmation screen (Prompt E) —
 * sync overlay lives on [SubmitForReviewScreen].
 */
@Composable
fun ScoreEntryScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSubmitForReview: (institutionId: String) -> Unit = {},
    viewModel: ScoreEntryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScoreEntryContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onRawScoreChange = viewModel::onRawScoreChange,
        onObservationsChange = viewModel::onObservationsChange,
        onRemoveAttachment = viewModel::onRemoveAttachment,
        onAddAttachment = viewModel::onAttachmentAdded,
        onSaveDraft = viewModel::onSaveDraft,
        onSubmitForServerReview = {
            val id = (uiState as? ScoreEntryUiState.Success)?.data?.institutionId.orEmpty()
            if (id.isNotBlank()) onSubmitForReview(id)
        },
        onDismissSavedDialog = viewModel::dismissSavedDialog,
    )
}
