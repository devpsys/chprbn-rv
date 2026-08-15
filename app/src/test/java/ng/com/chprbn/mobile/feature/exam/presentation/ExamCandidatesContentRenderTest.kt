package ng.com.chprbn.mobile.feature.exam.presentation

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.components.LOADING_STATE_TEST_TAG
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h2400dp")
class ExamCandidatesContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loading_state_shows_the_spinner_instead_of_placeholder_content() {
        composeRule.setContent {
            ChprbnTheme {
                ExamCandidatesContent(
                    uiState = ExamCandidatesUiState.placeholder().copy(hasLoaded = false),
                    onBack = {},
                    onAddRemark = {},
                    onViewProfile = {},
                )
            }
        }

        composeRule.onNodeWithTag(LOADING_STATE_TEST_TAG).assertExists()
        composeRule.onNodeWithText("Marcus Thompson").assertDoesNotExist()
    }

    @Test
    fun placeholder_renders_filter_chips_and_each_candidate() {
        composeRule.setContent {
            ChprbnTheme {
                ExamCandidatesContent(
                    uiState = ExamCandidatesUiState.placeholder(),
                    onBack = {},
                    onAddRemark = {},
                    onViewProfile = {},
                )
            }
        }

        composeRule.onNodeWithText("Marcus Thompson").assertExists()
        composeRule.onNodeWithText("Sarah Jenkins").assertExists()
        composeRule.onNodeWithText("David Chen").assertExists()
        composeRule.onNodeWithText("Elena Rodriguez").assertExists()
        // Filter labels surface as chips; both "Signed In" and "Signed Out"
        // appear multiple times (chip + at least one row pill carrying the
        // same label), so we assert presence rather than uniqueness.
        composeRule.onNodeWithText("Flagged").assertExists()
        assertTrue(
            composeRule.onAllNodesWithText("Signed Out").fetchSemanticsNodes().isNotEmpty(),
        )
        assertTrue(
            composeRule.onAllNodesWithText("Signed In").fetchSemanticsNodes().size >= 2,
        )
    }

    @Test
    fun empty_candidate_list_still_renders_filter_chrome() {
        composeRule.setContent {
            ChprbnTheme {
                ExamCandidatesContent(
                    uiState = ExamCandidatesUiState.placeholder().copy(candidates = emptyList()),
                    onBack = {},
                    onAddRemark = {},
                    onViewProfile = {},
                )
            }
        }

        composeRule.onNodeWithText("Flagged").assertExists()
    }
}
