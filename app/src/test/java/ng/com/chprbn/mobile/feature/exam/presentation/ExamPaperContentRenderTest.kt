package ng.com.chprbn.mobile.feature.exam.presentation

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.core.designsystem.components.LOADING_STATE_TEST_TAG
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
    fun loading_state_shows_the_spinner_instead_of_placeholder_content() {
        composeRule.setContent {
            ChprbnTheme {
                ExamPaperContent(
                    uiState = ExamPaperUiState.placeholder().copy(isLoading = true),
                    onBack = {},
                    onViewCandidates = {},
                    onSyncData = {},
                    onScanQr = {},
                )
            }
        }

        composeRule.onNodeWithTag(LOADING_STATE_TEST_TAG).assertExists()
        composeRule.onNodeWithText("Mathematics - Paper II").assertDoesNotExist()
    }

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
    fun error_message_renders_a_banner_above_the_still_visible_content() {
        composeRule.setContent {
            ChprbnTheme {
                ExamPaperContent(
                    uiState = ExamPaperUiState.placeholder().copy(
                        errorMessage = "This paper isn't cached yet.",
                    ),
                    onBack = {},
                    onViewCandidates = {},
                    onSyncData = {},
                    onScanQr = {},
                )
            }
        }

        composeRule.onNodeWithText("This paper isn't cached yet.").assertExists()
        // The rest of the (placeholder) content still renders underneath.
        composeRule.onNodeWithText("Mathematics - Paper II").assertExists()
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
