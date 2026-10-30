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
class AssessmentOverviewContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun placeholder_renders_progress_sections_and_actions() {
        composeRule.setContent {
            ChprbnTheme {
                AssessmentOverviewContent(uiState = AssessmentOverviewUiState.placeholder())
            }
        }

        composeRule.onNodeWithText("Accreditation Assessment").assertExists()
        composeRule.onNodeWithText("Ahmadu Bello University Teaching Hospital").assertExists()
        composeRule.onNodeWithText("50% Completed").assertExists()
        composeRule.onNodeWithText("Section A: General Information").assertExists()
        composeRule.onNodeWithText("Continue to Criteria Checklist").assertExists()
        composeRule.onNodeWithText("Save All Drafts to Vault").assertExists()
    }
}
