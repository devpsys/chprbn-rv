package ng.com.chprbn.mobile.core.designsystem.components

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * P3-3 render-snapshot tests for the download / sync loading overlays.
 * See [DownloadWarningDialogRenderTest] for the Robolectric + Application
 * override rationale.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ProgressOverlayRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun downloading_overlay_renders_title_subtitle_percent_and_badge() {
        composeRule.setContent {
            ChprbnTheme {
                DownloadingOverlay(
                    title = "Downloading Candidate Records…",
                    subtitle = "Please do not close the app. Fetching data from secure registry.",
                    encryptedLabel = "End-to-End Encrypted",
                    statusLabel = "Synchronizing…",
                    progressFraction = 0.65f,
                )
            }
        }

        composeRule.onNodeWithText("Downloading Candidate Records…").assertExists()
        composeRule.onNodeWithText(
            "Please do not close the app. Fetching data from secure registry.",
        ).assertExists()
        // Status label rendered uppercased by the overlay.
        composeRule.onNodeWithText("SYNCHRONIZING…").assertExists()
        // Percent derived from the fraction (65%).
        composeRule.onNodeWithText("65%").assertExists()
        // Encrypted security badge.
        composeRule.onNodeWithText("END-TO-END ENCRYPTED").assertExists()
    }

    @Test
    fun downloading_overlay_clamps_progress_fraction_above_1f_to_100_percent() {
        composeRule.setContent {
            ChprbnTheme {
                DownloadingOverlay(
                    title = "title",
                    subtitle = "sub",
                    encryptedLabel = "enc",
                    statusLabel = "status",
                    progressFraction = 1.7f,
                )
            }
        }

        composeRule.onNodeWithText("100%").assertExists()
    }

    @Test
    fun syncing_overlay_renders_title_subtitle_and_badge() {
        composeRule.setContent {
            ChprbnTheme {
                SyncingOverlay(
                    title = "Syncing Attendance Data…",
                    subtitle = "Securely uploading verified records to the central server.",
                    encryptedLabel = "Encrypted",
                )
            }
        }

        composeRule.onNodeWithText("Syncing Attendance Data…").assertExists()
        composeRule.onNodeWithText(
            "Securely uploading verified records to the central server.",
        ).assertExists()
        composeRule.onNodeWithText("ENCRYPTED").assertExists()
    }
}
