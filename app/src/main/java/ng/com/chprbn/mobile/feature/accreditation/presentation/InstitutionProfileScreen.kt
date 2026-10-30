package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Read-only institution profile, reached from the Accreditation Hub.
 * Start New Assessment / View Documents / history taps are nav-host
 * callbacks — Assessment Overview and document screens land later.
 */
@Composable
fun InstitutionProfileScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onStartNewAssessment: (institutionId: String) -> Unit = {},
    onViewDocuments: (institutionId: String) -> Unit = {},
    onHistoryItemClick: (institutionId: String, historyId: String) -> Unit = { _, _ -> },
    viewModel: InstitutionProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    InstitutionProfileContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onStartNewAssessment = onStartNewAssessment,
        onViewDocuments = onViewDocuments,
        onHistoryItemClick = onHistoryItemClick,
    )
}
