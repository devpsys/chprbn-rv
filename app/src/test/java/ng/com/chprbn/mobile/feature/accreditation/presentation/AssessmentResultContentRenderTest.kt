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
class AssessmentResultContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun assessment_result_renders_server_decision_and_actions() {
        composeRule.setContent {
            ChprbnTheme {
                AssessmentResultContent(
                    uiState = AssessmentResultUiState.placeholder(),
                )
            }
        }

        composeRule.onNodeWithText("Ahmadu Bello University Teaching Hospital").assertExists()
        composeRule.onNodeWithText("Grade: Excellent").assertExists()
        composeRule.onNodeWithText("Full Accreditation").assertExists()
        composeRule.onNodeWithText("Tenure: 5 Years Full Accreditation").assertExists()
        composeRule.onNodeWithText("Officer Raw Scores").assertExists()
        composeRule.onNodeWithText("Board Conditions & Follow-up").assertExists()
        composeRule.onNodeWithText("View Certificate").assertExists()
        composeRule.onNodeWithText("Done").assertExists()
    }
}
