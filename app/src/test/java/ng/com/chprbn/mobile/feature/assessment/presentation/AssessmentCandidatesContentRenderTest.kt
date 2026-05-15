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
class AssessmentCandidatesContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun card(id: String, name: String, score: Int = 72, level: ScoreLevel = ScoreLevel.Normal) =
        CandidateCardUiState(
            id = id,
            indexingNumber = "EX-$id",
            fullName = name,
            photoUrl = null,
            score = score,
            level = level,
        )

    @Test
    fun renders_candidates_and_total_count() {
        val state = AssessmentCandidatesUiState(
            totalCount = 3,
            query = "",
            viewMode = CandidatesViewMode.List,
            candidates = listOf(
                card("001", "Aisha Bello"),
                card("002", "Tunde Adebayo", score = 38, level = ScoreLevel.Low),
                card("003", "Chiamaka Okeke"),
            ),
        )

        composeRule.setContent {
            ChprbnTheme {
                AssessmentCandidatesContent(uiState = state)
            }
        }

        composeRule.onNodeWithText("Aisha Bello").assertExists()
        composeRule.onNodeWithText("Tunde Adebayo").assertExists()
        composeRule.onNodeWithText("Chiamaka Okeke").assertExists()
        composeRule.onNodeWithText("3 Total").assertExists()
    }

    @Test
    fun empty_state_renders_no_candidates_copy() {
        composeRule.setContent {
            ChprbnTheme {
                AssessmentCandidatesContent(uiState = AssessmentCandidatesUiState())
            }
        }

        composeRule.onNodeWithText("No candidates found").assertExists()
    }
}
