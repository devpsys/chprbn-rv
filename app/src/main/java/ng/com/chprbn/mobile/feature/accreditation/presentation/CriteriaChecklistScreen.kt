package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Accreditation criteria checklist (Guideline §1–8). Presentation-only until
 * draft persistence lands; attachment capture wired via Prompt H.
 */
@Composable
fun CriteriaChecklistScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onContinueToScoreEntry: (institutionId: String) -> Unit = {},
    viewModel: CriteriaChecklistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CriteriaChecklistContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onMetStatusChange = viewModel::onMetStatusChange,
        onCheckItemToggled = viewModel::onCheckItemToggled,
        onEvidenceNotesChange = viewModel::onEvidenceNotesChange,
        onHrCountChange = viewModel::onHrCountChange,
        onCapitalAmountChange = viewModel::onCapitalAmountChange,
        onStatementOfAccountConfirmed = viewModel::onStatementOfAccountConfirmed,
        onSalaryChange = viewModel::onSalaryChange,
        onRemoveAttachment = viewModel::onRemoveAttachment,
        onAddAttachment = viewModel::onAttachmentAdded,
        onSaveChecklistDraft = viewModel::onSaveChecklistDraft,
        onSaveAsDraft = viewModel::onSaveAsDraft,
        onContinueToScoreEntry = onContinueToScoreEntry,
        onDismissSavedDialog = viewModel::dismissSavedDialog,
    )
}
