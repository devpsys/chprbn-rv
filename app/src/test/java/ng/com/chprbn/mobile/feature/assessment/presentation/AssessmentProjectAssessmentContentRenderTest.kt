package ng.com.chprbn.mobile.feature.assessment.presentation

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
class AssessmentProjectAssessmentContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_candidate_role_and_score_input() {
        val state = AssessmentProjectAssessmentUiState(
            candidateName = "Aisha Bello",
            examId = "EX-2024-007",
            role = "Clinical Practitioner",
            photoUrl = null,
            verified = true,
            scoreText = "8.5",
            maxScore = 10,
        )

        composeRule.setContent {
            ChprbnTheme {
                AssessmentProjectAssessmentContent(uiState = state)
            }
        }

        composeRule.onNodeWithText("Aisha Bello").assertExists()
        composeRule.onNodeWithText("Clinical Practitioner").assertExists()
        // The scoreText is rendered inside an input field; assert max-score
        // suffix instead since it's a plain Text element.
        composeRule.onNodeWithText("/ 10").assertExists()
        composeRule.onNodeWithText("Save Score").assertExists()
    }

    @Test
    fun empty_state_renders_placeholder_score() {
        composeRule.setContent {
            ChprbnTheme {
                AssessmentProjectAssessmentContent(
                    uiState = AssessmentProjectAssessmentUiState(),
                )
            }
        }

        // The default-empty scoreText path uses a 0.0 placeholder.
        composeRule.onNodeWithText("0.0").assertExists()
    }
}
