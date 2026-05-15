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
class ReportIrregularityContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun idle_state_renders_form_fields_and_submit_button() {
        composeRule.setContent {
            ChprbnTheme {
                ReportIrregularityContent(
                    uiState = ReportIrregularityUiState(
                        nameOnCard = "Sarah Jenkins",
                        licenseNumber = "MED-12345",
                    ),
                )
            }
        }

        composeRule.onNodeWithText("Report license irregularity").assertExists()
        composeRule.onNodeWithText("Sarah Jenkins").assertExists()
        composeRule.onNodeWithText("Submit report").assertExists()
    }

    @Test
    fun submitting_state_swaps_button_label_to_progress() {
        composeRule.setContent {
            ChprbnTheme {
                ReportIrregularityContent(
                    uiState = ReportIrregularityUiState(
                        submitState = ReportIrregularitySubmitState.Submitting,
                    ),
                )
            }
        }

        composeRule.onNodeWithText("Submitting…").assertExists()
    }
}
