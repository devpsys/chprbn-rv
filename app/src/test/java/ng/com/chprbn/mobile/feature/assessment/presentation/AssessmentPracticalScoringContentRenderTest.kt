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
class AssessmentPracticalScoringContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_section_title_and_each_question_prompt() {
        val state = AssessmentPracticalScoringUiState(
            sectionTitle = "Section A — Vital Signs",
            questions = listOf(
                ScoreQuestionUiState(
                    id = "q1",
                    number = 1,
                    prompt = "Measures blood pressure correctly",
                    imageUrl = null,
                    maxScore = 5,
                    score = 4,
                ),
                ScoreQuestionUiState(
                    id = "q2",
                    number = 2,
                    prompt = "Records pulse rate accurately",
                    imageUrl = null,
                    maxScore = 5,
                    score = 0,
                ),
            ),
        )

        composeRule.setContent {
            ChprbnTheme {
                AssessmentPracticalScoringContent(uiState = state)
            }
        }

        composeRule.onNodeWithText("1. Measures blood pressure correctly").assertExists()
        composeRule.onNodeWithText("2. Records pulse rate accurately").assertExists()
        composeRule.onNodeWithText("Save Scores").assertExists()
    }
}
