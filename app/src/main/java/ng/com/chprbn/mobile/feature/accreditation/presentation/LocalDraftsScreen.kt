package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Local accreditation drafts vault. Presentation-only with placeholder
 * UiState until Room drafts + SyncBatchRunner wiring lands.
 */
@Composable
fun LocalDraftsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onResumeDraft: (institutionId: String) -> Unit = {},
    viewModel: LocalDraftsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LocalDraftsContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onFilterSelected = viewModel::onFilterSelected,
        onDraftSelectionToggled = viewModel::onDraftSelectionToggled,
        onResumeDraft = { draft -> onResumeDraft(draft.institutionId) },
        onSyncAllDrafts = viewModel::onSyncAllDrafts,
        onDiscardSelectedClicked = viewModel::onDiscardSelectedClicked,
        onConfirmDiscard = viewModel::confirmDiscardSelected,
        onDismissDiscard = viewModel::dismissDiscardConfirm,
        onDismissSyncSuccess = viewModel::dismissSyncSuccess,
    )
}
