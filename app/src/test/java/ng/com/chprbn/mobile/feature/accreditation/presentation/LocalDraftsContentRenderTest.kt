package ng.com.chprbn.mobile.feature.accreditation.presentation

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h2400dp")
class LocalDraftsContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun placeholder_renders_drafts_filters_and_actions() {
        composeRule.setContent {
            ChprbnTheme {
                LocalDraftsContent(uiState = LocalDraftsUiState.placeholder())
            }
        }

        composeRule.onNodeWithText("LOCAL DRAFTS").assertExists()
        composeRule.onNodeWithText("All").assertExists()
        assertTrue(
            composeRule.onAllNodesWithText("Pending Sync", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty(),
        )
        composeRule.onNodeWithText("Ahmadu Bello University Teaching Hospital").assertExists()
        composeRule.onNodeWithText("School of Health Technology, Minna").assertExists()
        composeRule.onNodeWithText("5 of 6 sections").assertExists()
        composeRule.onNodeWithText("Sync All Drafts").assertExists()
        composeRule.onNodeWithText("Discard Selected").assertExists()
    }

    @Test
    fun empty_state_renders_no_local_drafts() {
        composeRule.setContent {
            ChprbnTheme {
                LocalDraftsContent(
                    uiState = LocalDraftsUiState.placeholder().copy(
                        drafts = emptyList(),
                        visibleDrafts = emptyList(),
                    ),
                )
            }
        }

        composeRule.onNodeWithText("No local drafts").assertExists()
    }
}
