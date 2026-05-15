package ng.com.chprbn.mobile.feature.verification.presentation

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h2400dp")
class SyncHistoryContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun default_state_uses_sample_history_and_renders_each_item() {
        composeRule.setContent {
            ChprbnTheme {
                SyncHistoryContent(uiState = SyncHistoryUiState())
            }
        }

        composeRule.onNodeWithText("Record #8831-C").assertExists()
        composeRule.onNodeWithText("Record #8830-B").assertExists()
        composeRule.onNodeWithText("Record #8829-A").assertExists()
        composeRule.onNodeWithText("Network timeout").assertExists()
    }

    @Test
    fun failed_filter_includes_only_failed_rows() {
        composeRule.setContent {
            ChprbnTheme {
                SyncHistoryContent(
                    uiState = SyncHistoryUiState(filter = SyncHistoryFilter.Failed),
                )
            }
        }

        composeRule.onNodeWithText("Record #8829-A").assertExists()
        composeRule.onNodeWithText("Record #8812-D").assertExists()
        composeRule.onNodeWithText("Record #8831-C").assertDoesNotExist()
    }
}
