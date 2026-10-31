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
class SubmitForReviewContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun submit_for_review_renders_summary_and_confirmation() {
        composeRule.setContent {
            ChprbnTheme {
                SubmitForReviewContent(
                    uiState = SubmitForReviewUiState.placeholder(),
                )
            }
        }

        composeRule.onNodeWithText("Ahmadu Bello University Teaching Hospital").assertExists()
        composeRule.onNodeWithText("Dossier Readiness").assertExists()
        composeRule.onNodeWithText("6 of 6").assertExists()
        composeRule.onNodeWithText("14 Files Bound").assertExists()
        composeRule.onNodeWithText("Submit this accreditation assessment for review?").assertExists()
        composeRule.onNodeWithText("Submit Assessment").assertExists()
        composeRule.onNodeWithText("Cancel").assertExists()
    }
}
