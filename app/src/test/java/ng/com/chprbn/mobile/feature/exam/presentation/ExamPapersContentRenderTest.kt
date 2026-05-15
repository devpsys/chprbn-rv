package ng.com.chprbn.mobile.feature.exam.presentation

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
}
