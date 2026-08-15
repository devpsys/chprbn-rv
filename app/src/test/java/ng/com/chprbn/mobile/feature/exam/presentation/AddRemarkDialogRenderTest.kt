package ng.com.chprbn.mobile.feature.exam.presentation

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h2400dp")
class AddRemarkDialogRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun openState(
        selectedType: RemarkType? = null,
        isSaving: Boolean = false,
        errorMessage: String? = null,
    ) = AddRemarkUiState.Open(
        candidateId = "c1",
        candidateName = "Jane Doe",
        selectedType = selectedType,
        isSaving = isSaving,
        errorMessage = errorMessage,
    )

    @Test
    fun renders_title_subtitle_sections_and_every_remark_type() {
        composeRule.setContent {
            ChprbnTheme {
                AddRemarkDialog(
                    state = openState(),
                    onSelectType = {},
                    onDismiss = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("Candidate Remarks").assertExists()
        composeRule.onNodeWithText("Review clinical observations and behavioral notes.").assertExists()
        composeRule.onNodeWithText("Absenteeism").assertExists()
        composeRule.onNodeWithText("Absent with Excuse").assertExists()
        composeRule.onNodeWithText("Absent without Excuse").assertExists()
        composeRule.onNodeWithText("Project Incomplete").assertExists()
        composeRule.onNodeWithText("Sick").assertExists()
        composeRule.onNodeWithText("Exams Malpractice").assertExists()
        composeRule.onNodeWithText("Cancel").assertExists()
        composeRule.onNodeWithText("Save Remark").assertExists()
    }

    @Test
    fun tapping_a_remark_type_card_invokes_onSelectType() {
        var selected: RemarkType? = null
        composeRule.setContent {
            ChprbnTheme {
                AddRemarkDialog(
                    state = openState(),
                    onSelectType = { selected = it },
                    onDismiss = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithTag("ABS").performClick()

        assertEquals(RemarkType.Absenteeism, selected)
    }

    @Test
    fun close_button_invokes_onDismiss() {
        var dismissed = 0
        composeRule.setContent {
            ChprbnTheme {
                AddRemarkDialog(
                    state = openState(),
                    onSelectType = {},
                    onDismiss = { dismissed++ },
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Close").performClick()

        assertEquals(1, dismissed)
    }

    @Test
    fun cancel_button_invokes_onDismiss() {
        var dismissed = 0
        composeRule.setContent {
            ChprbnTheme {
                AddRemarkDialog(
                    state = openState(),
                    onSelectType = {},
                    onDismiss = { dismissed++ },
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("Cancel").performClick()

        assertEquals(1, dismissed)
    }

    @Test
    fun save_button_invokes_onSave_once_a_type_is_selected() {
        var saved = 0
        composeRule.setContent {
            ChprbnTheme {
                AddRemarkDialog(
                    state = openState(selectedType = RemarkType.Sick),
                    onSelectType = {},
                    onDismiss = {},
                    onSave = { saved++ },
                )
            }
        }

        composeRule.onNodeWithText("Save Remark").performClick()

        assertEquals(1, saved)
    }

    @Test
    fun save_button_does_not_invoke_onSave_without_a_selected_type() {
        var saved = 0
        composeRule.setContent {
            ChprbnTheme {
                AddRemarkDialog(
                    state = openState(selectedType = null),
                    onSelectType = {},
                    onDismiss = {},
                    onSave = { saved++ },
                )
            }
        }

        composeRule.onNodeWithText("Save Remark").performClick()

        assertEquals(0, saved)
    }

    @Test
    fun error_message_renders_when_present() {
        composeRule.setContent {
            ChprbnTheme {
                AddRemarkDialog(
                    state = openState(errorMessage = "Unable to save remark. Please try again."),
                    onSelectType = {},
                    onDismiss = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("Unable to save remark. Please try again.").assertExists()
    }
}
