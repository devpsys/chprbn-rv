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
class AttachmentManagerRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun empty_state_renders_supporting_documents_and_no_files() {
        composeRule.setContent {
            ChprbnTheme {
                AttachmentManager(
                    attachments = emptyList(),
                    onAddAttachment = { _, _ -> },
                    onRemoveAttachment = {},
                )
            }
        }

        composeRule.onNodeWithText("Supporting Documents").assertExists()
        composeRule.onNodeWithText("No files attached").assertExists()
        composeRule.onNodeWithText("Gallery").assertExists()
        composeRule.onNodeWithText("Camera").assertExists()
    }

    @Test
    fun list_state_renders_filename_location_and_size() {
        composeRule.setContent {
            ChprbnTheme {
                AttachmentManager(
                    attachments = listOf(
                        SsqAttachmentUiState(
                            id = "1",
                            title = "Kaduna_State_Gazette_1998.pdf",
                            sizeLabel = "1.4 MB",
                            isPhoto = false,
                            locationLabel = "11.1114° N, 7.6432° E",
                        ),
                    ),
                    onAddAttachment = { _, _ -> },
                    onRemoveAttachment = {},
                )
            }
        }

        composeRule.onNodeWithText("Kaduna_State_Gazette_1998.pdf").assertExists()
        composeRule.onNodeWithText("11.1114° N, 7.6432° E").assertExists()
        composeRule.onNodeWithText("1.4 MB").assertExists()
    }
}
