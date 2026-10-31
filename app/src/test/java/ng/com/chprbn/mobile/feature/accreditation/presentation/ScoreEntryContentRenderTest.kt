package ng.com.chprbn.mobile.feature.accreditation.presentation

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
class ScoreEntryContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun score_entry_renders_heading_and_criterion_labels() {
        composeRule.setContent {
            ChprbnTheme {
                ScoreEntryContent(
                    uiState = ScoreEntryUiState.placeholder(),
                )
            }
        }

        composeRule.onNodeWithText("REGULATORY RAW SCORE SUBMISSION").assertExists()
        composeRule.onNodeWithText("6 of 6 Completed").assertExists()
        composeRule.onNodeWithText("Location & Land Area (§1)").assertExists()
        composeRule.onNodeWithText("Human Resources & Teaching Cadre (§3)").assertExists()
        composeRule.onNodeWithText("Submit for Review").assertExists()
    }
}
