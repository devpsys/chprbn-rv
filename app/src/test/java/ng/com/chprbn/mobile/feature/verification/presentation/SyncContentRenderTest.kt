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
class SyncContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loaded_state_renders_action_buttons() {
        composeRule.setContent {
            ChprbnTheme {
                SyncContent(uiState = SyncUiState(isLoading = false))
            }
        }

        composeRule.onNodeWithText("SYNC ALL RECORDS").assertExists()
        composeRule.onNodeWithText("RETRY FAILED SYNC").assertExists()
    }

    @Test
    fun loading_state_renders_loading_copy() {
        composeRule.setContent {
            ChprbnTheme {
                SyncContent(uiState = SyncUiState(isLoading = true))
            }
        }

        composeRule.onNodeWithText("Loading sync status…").assertExists()
    }
}
