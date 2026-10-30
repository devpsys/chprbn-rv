package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Accreditation Hub — first screen of the accreditation feature, reached
 * from the unified dashboard's ACCREDITATION card. Presentation-only for
 * now: tile / institution callbacks are wired by the nav host and stay
 * no-ops until downstream screens land.
 */
@Composable
fun AccreditationHubScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onQuickSync: () -> Unit = {},
    onStartNewAssessment: (institutionId: String) -> Unit = {},
    onContinueAssessment: () -> Unit = {},
    onLocalDrafts: () -> Unit = {},
    onSyncStatus: () -> Unit = {},
    onInstitutionClick: (institutionId: String) -> Unit = {},
    onStartAssessment: (institutionId: String) -> Unit = {},
    viewModel: AccreditationHubViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AccreditationHubContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onQuickSync = onQuickSync,
        onStartNewAssessment = {
            viewModel.firstActiveInstitutionId()?.let(onStartNewAssessment)
        },
        onContinueAssessment = onContinueAssessment,
        onLocalDrafts = onLocalDrafts,
        onSyncStatus = onSyncStatus,
        onInstitutionClick = onInstitutionClick,
        onStartAssessment = onStartAssessment,
    )
}
