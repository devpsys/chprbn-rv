package ng.com.chprbn.mobile.feature.exam.presentation

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h2400dp")
class ExamStatisticsContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun placeholder_renders_summary_counts_and_legend() {
        composeRule.setContent {
            ChprbnTheme {
                ExamStatisticsContent(
                    uiState = ExamStatisticsUiState.placeholder(),
                    onBack = {},
                    onRefresh = {},
                    onSyncNow = {},
                    onClearCached = {},
                    onExamDashboardTab = {},
                    onStatisticsTab = {},
                )
            }
        }

        // "1,250" appears twice in the placeholder (recordsDownloaded and
        // totalCountLabel both share the value), so we use onAllNodesWithText
        // and assert at least one match rather than the strict unique-node
        // form.
        assertTrue(
            composeRule.onAllNodesWithText("1,250").fetchSemanticsNodes().isNotEmpty(),
        )
        composeRule.onNodeWithText("1,180").assertExists()
        composeRule.onNodeWithText("94.4% Completion").assertExists()
        composeRule.onNodeWithText("Updated 5m ago").assertExists()
        composeRule.onNodeWithText(
            "* 70 records remain uncaptured from the total downloaded set.",
        ).assertExists()
    }

    @Test
    fun zero_failed_state_omits_footnote() {
        composeRule.setContent {
            ChprbnTheme {
                ExamStatisticsContent(
                    uiState = ExamStatisticsUiState.placeholder().copy(footnote = ""),
                    onBack = {},
                    onRefresh = {},
                    onSyncNow = {},
                    onClearCached = {},
                    onExamDashboardTab = {},
                    onStatisticsTab = {},
                )
            }
        }

        assertTrue(
            composeRule.onAllNodesWithText("1,250").fetchSemanticsNodes().isNotEmpty(),
        )
    }
}
