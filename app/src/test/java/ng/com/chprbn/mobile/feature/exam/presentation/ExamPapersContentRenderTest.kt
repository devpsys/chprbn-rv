package ng.com.chprbn.mobile.feature.exam.presentation

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h2400dp")
class ExamPapersContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun placeholder_renders_daily_overview_and_each_paper_card() {
        composeRule.setContent {
            ChprbnTheme {
                ExamPapersContent(
                    uiState = ExamPapersUiState.placeholder(),
                    onBack = {},
                    onOpenPaper = {},
                    onSyncNow = {},
                )
            }
        }

        composeRule.onNodeWithText("Monday, June 12").assertExists()
        composeRule.onNodeWithText("Paper I (P1)").assertExists()
        composeRule.onNodeWithText("Paper II (P2)").assertExists()
        composeRule.onNodeWithText("Paper III (P3)").assertExists()
        composeRule.onNodeWithText("Mark Attendance").assertExists()
    }

    @Test
    fun empty_papers_list_still_renders_overview_chrome() {
        composeRule.setContent {
            ChprbnTheme {
                ExamPapersContent(
                    uiState = ExamPapersUiState.placeholder().copy(papers = emptyList()),
                    onBack = {},
                    onOpenPaper = {},
                    onSyncNow = {},
                )
            }
        }

        composeRule.onNodeWithText("Monday, June 12").assertExists()
    }

    @Test
    fun real_empty_state_shows_empty_message_instead_of_placeholder_cards() {
        composeRule.setContent {
            ChprbnTheme {
                ExamPapersContent(
                    uiState = ExamPapersUiState.placeholder().copy(
                        papers = emptyList(),
                        hasDownloadedData = true,
                    ),
                    onBack = {},
                    onOpenPaper = {},
                    onSyncNow = {},
                )
            }
        }

        composeRule.onNodeWithText("No Papers Found").assertExists()
        composeRule.onNodeWithText("Paper I (P1)").assertDoesNotExist()
    }

    @Test
    fun sync_fab_is_visible_and_invokes_onSyncNow() {
        var syncClicked = false
        composeRule.setContent {
            ChprbnTheme {
                ExamPapersContent(
                    uiState = ExamPapersUiState.placeholder(),
                    onBack = {},
                    onOpenPaper = {},
                    onSyncNow = { syncClicked = true },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Sync").performClick()

        assertTrue("tapping the sync FAB should invoke onSyncNow", syncClicked)
    }
}
