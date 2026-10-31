package ng.com.chprbn.mobile.feature.accreditation.presentation

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.network.stripHostsAndUrls
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h2400dp")
class SyncStatusContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun placeholder_renders_monitor_failed_rows_and_actions() {
        val placeholder = SyncStatusUiState.placeholder()
        val rawError = placeholder.failedDrafts.first().lastRawError.orEmpty()
        val sanitised = rawError.stripHostsAndUrls()

        assertTrue(rawError.contains("https://"))
        assertFalse(sanitised.contains("https://"))
        assertFalse(sanitised.contains("registry.chprbn.gov.ng"))

        composeRule.setContent {
            ChprbnTheme {
                SyncStatusContent(uiState = placeholder)
            }
        }

        composeRule.onNodeWithText("SYNC STATUS").assertExists()
        composeRule.onNodeWithText("OUTBOX SYNC MONITOR").assertExists()
        composeRule.onNodeWithText("Sync Errors Detected").assertExists()
        assertTrue(
            composeRule.onAllNodesWithText("School of Health Technology, Minna")
                .fetchSemanticsNodes()
                .isNotEmpty(),
        )
        composeRule.onNodeWithText("Retry Failed").assertExists()
        composeRule.onNodeWithText("Close").assertExists()
        // Sanitised error must appear; raw host must not.
        composeRule.onNodeWithText("registry.chprbn.gov.ng", substring = true).assertDoesNotExist()
        assertTrue(
            composeRule.onAllNodesWithText("[server]", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty(),
        )
    }

    @Test
    fun empty_outbox_renders_clear_copy() {
        composeRule.setContent {
            ChprbnTheme {
                SyncStatusContent(
                    uiState = SyncStatusUiState.placeholder().copy(
                        overallStatus = OverallSyncStatusUi.Synced,
                        progressFraction = 1f,
                        failedDrafts = emptyList(),
                    ),
                )
            }
        }

        composeRule.onNodeWithText("All sync outbox queues clear").assertExists()
        composeRule.onNodeWithText("All Drafts Synced").assertExists()
    }
}
