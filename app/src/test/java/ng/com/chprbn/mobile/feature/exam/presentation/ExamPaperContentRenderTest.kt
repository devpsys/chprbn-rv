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
class ExamPaperContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun placeholder_renders_paper_title_institution_and_progress() {
        composeRule.setContent {
            ChprbnTheme {
                ExamPaperContent(
                    uiState = ExamPaperUiState.placeholder(),
                    onBack = {},
                    onViewCandidates = {},
                    onSyncData = {},
                    onScanQr = {},
                )
            }
        }

        composeRule.onNodeWithText("Mathematics - Paper II").assertExists()
        composeRule.onNodeWithText("National Institute of Technology").assertExists()
        composeRule.onNodeWithText("NIT-405").assertExists()
        composeRule.onNodeWithText("85 of 120 candidates checked in").assertExists()
        composeRule.onNodeWithText("70%").assertExists()
        composeRule.onNodeWithText("Cloud Synced").assertExists()
    }

    @Test
    fun pending_sync_label_surfaces_in_state() {
        composeRule.setContent {
            ChprbnTheme {
                ExamPaperContent(
                    uiState = ExamPaperUiState.placeholder().copy(
                        syncStatusLabel = "Pending Sync (5)",
                    ),
                    onBack = {},
                    onViewCandidates = {},
                    onSyncData = {},
                    onScanQr = {},
                )
            }
        }

        composeRule.onNodeWithText("Pending Sync (5)").assertExists()
    }
}
