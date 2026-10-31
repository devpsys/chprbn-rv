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
class AccreditationComponentsRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun section_completion_chip_renders_sync_failed() {
        composeRule.setContent {
            ChprbnTheme {
                SectionCompletionChip(status = SectionDraftStatus.SyncFailed)
            }
        }
        composeRule.onNodeWithText("Sync Failed").assertExists()
    }

    @Test
    fun sync_error_row_strips_hosts_from_message() {
        composeRule.setContent {
            ChprbnTheme {
                SyncErrorRow(
                    institutionName = "ABUTH Zaria",
                    errorMessage = "Unable to reach https://api.chprbn.gov.ng/sync",
                    onRetry = {},
                )
            }
        }
        composeRule.onNodeWithText("ABUTH Zaria").assertExists()
        composeRule.onNodeWithText("Retry").assertExists()
        composeRule.onNodeWithText("https://api.chprbn.gov.ng/sync", substring = true)
            .assertDoesNotExist()
    }

    @Test
    fun draft_status_badge_renders_saved_locally() {
        composeRule.setContent {
            ChprbnTheme {
                DraftStatusBadge(kind = DraftBadgeKind.SavedLocally)
            }
        }
        composeRule.onNodeWithText("Saved Locally").assertExists()
    }
}
