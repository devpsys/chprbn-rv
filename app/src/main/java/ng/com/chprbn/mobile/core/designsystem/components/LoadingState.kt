package ng.com.chprbn.mobile.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/** Semantics tag for [LoadingState], so render tests can assert on it without matching visible text. */
const val LOADING_STATE_TEST_TAG = "loading_state"

/**
 * Centered spinner filling the available space. Used while a screen's
 * first real load is still in flight, in place of that screen's
 * `placeholder()` fixture — showing fake data (or a premature "no data"
 * empty state) during a transient fetch reads as broken/wrong, not
 * loading.
 */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(LOADING_STATE_TEST_TAG),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
