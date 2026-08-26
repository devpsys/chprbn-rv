package ng.com.chprbn.mobile.feature.exam.presentation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Officer-facing list of locally-captured records grouped by tab
 * (Pending / Failed). Reached from a card on the Statistics screen;
 * every row here corresponds to something the sync engine cares about
 * — either awaiting upload or blocked on an error the officer may need
 * to act on.
 *
 * Long-press on a row copies its sanitised error message to the
 * clipboard for support tickets. Search + record-type filter chips
 * refine the list without leaving the tab.
 */
@Composable
fun CachedRecordsScreen(
    onBack: () -> Unit,
    viewModel: CachedRecordsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.copyToastMessage) {
        val message = uiState.copyToastMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.onCopyToastShown()
    }

    CachedRecordsContent(
        uiState = uiState,
        onBack = onBack,
        onTabSelected = viewModel::onTabSelected,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onRecordTypeFilterToggled = viewModel::onRecordTypeFilterToggled,
        onCopyError = viewModel::onCopyError,
    )
}
