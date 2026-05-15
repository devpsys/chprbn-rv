package ng.com.chprbn.mobile.core.designsystem.components

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * P3-3 render-snapshot test for the destructive download confirmation.
 * Runs on the JVM via Robolectric — no emulator required — and asserts
 * the dialog renders with the expected title / message / buttons and
 * dispatches the right callbacks.
 *
 * Uses `application = Application::class` to bypass ChprbnApplication,
 * whose onCreate loads the sqlcipher JNI library that isn't available
 * in the unit-test JVM. Pure Compose rendering doesn't need Hilt or DB.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class DownloadWarningDialogRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_title_message_footnote_and_buttons() {
        composeRule.setContent {
            ChprbnTheme {
                DownloadWarningDialog(
                    title = "Download Candidate Records",
                    message = "Warning: This operation will erase all currently cached records on this device.",
                    footnote = "Once initiated, the local database will be wiped.",
                    primaryButtonText = "Download & Erase",
                    secondaryButtonText = "Cancel",
                    onConfirm = {},
                    onCancel = {},
                )
            }
        }

        // Use assertExists() rather than assertIsDisplayed(): the JVM-side
        // Compose test environment renders to a small virtual viewport, so
        // content positioned below the fold (Cancel button at the bottom)
        // fails the stricter "displayed" check even though it's composed.
        composeRule.onNodeWithText("Download Candidate Records").assertExists()
        composeRule.onNodeWithText(
            "Warning: This operation will erase all currently cached records on this device.",
        ).assertExists()
        composeRule.onNodeWithText("Once initiated, the local database will be wiped.")
            .assertExists()
        composeRule.onNodeWithText("Download & Erase").assertExists()
        composeRule.onNodeWithText("Cancel").assertExists()
    }

    @Test
    fun primary_button_invokes_onConfirm() {
        var confirmed = 0
        composeRule.setContent {
            ChprbnTheme {
                DownloadWarningDialog(
                    title = "title",
                    message = "msg",
                    footnote = null,
                    primaryButtonText = "Confirm",
                    secondaryButtonText = "Cancel",
                    onConfirm = { confirmed++ },
                    onCancel = {},
                )
            }
        }

        composeRule.onNodeWithText("Confirm").performClick()

        assertEquals(1, confirmed)
    }

    @Test
    fun secondary_button_invokes_onCancel() {
        var cancelled = 0
        composeRule.setContent {
            ChprbnTheme {
                DownloadWarningDialog(
                    title = "title",
                    message = "msg",
                    footnote = null,
                    primaryButtonText = "Confirm",
                    secondaryButtonText = "Cancel",
                    onConfirm = {},
                    onCancel = { cancelled++ },
                )
            }
        }

        composeRule.onNodeWithText("Cancel").performClick()

        assertEquals(1, cancelled)
    }
}
