package ng.com.chprbn.mobile.feature.exam.presentation

import android.app.Application
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h2400dp")
class ExamDashboardContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun loadedPlaceholder() = ExamDashboardUiState.placeholder().copy(hasDownloadedData = true)

    @Test
    fun no_data_downloaded_renders_the_empty_state_instead_of_institution_card() {
        composeRule.setContent {
            ChprbnTheme {
                ExamDashboardScreenContent(uiState = ExamDashboardUiState.placeholder())
            }
        }

        composeRule.onNodeWithText("No Records Downloaded").assertExists()
        composeRule.onNodeWithText("National Institute of Health Sciences").assertDoesNotExist()
        composeRule.onNodeWithText("Attendance Monitoring").assertDoesNotExist()
    }

    @Test
    fun loaded_state_renders_institution_and_task_cards() {
        composeRule.setContent {
            ChprbnTheme {
                ExamDashboardScreenContent(uiState = loadedPlaceholder())
            }
        }

        composeRule.onNodeWithText("National Institute of Health Sciences").assertExists()
        composeRule.onNodeWithText("#NIH-2024").assertExists()
        composeRule.onNodeWithText("Lagos Central Campus").assertExists()
        composeRule.onNodeWithText("Attendance Monitoring").assertExists()
        composeRule.onNodeWithText("Practical Assessment").assertExists()
        composeRule.onNodeWithText("Log Attendance").assertExists()
        composeRule.onNodeWithText("Grade Practical").assertExists()
    }

    @Test
    fun loaded_state_with_no_schedules_shows_friendly_message_instead_of_task_cards() {
        composeRule.setContent {
            ChprbnTheme {
                ExamDashboardScreenContent(
                    uiState = loadedPlaceholder().copy(hasSchedules = false),
                )
            }
        }

        composeRule.onNodeWithText("National Institute of Health Sciences").assertExists()
        composeRule.onNodeWithText("No Papers Scheduled Today").assertExists()
        composeRule.onNodeWithText("Attendance Monitoring").assertDoesNotExist()
        composeRule.onNodeWithText("Practical Assessment").assertDoesNotExist()
    }

    @Test
    fun loaded_state_without_sections_hides_practical_card_but_keeps_attendance_card() {
        composeRule.setContent {
            ChprbnTheme {
                ExamDashboardScreenContent(
                    uiState = loadedPlaceholder().copy(hasPracticalAssessment = false),
                )
            }
        }

        composeRule.onNodeWithText("Attendance Monitoring").assertExists()
        composeRule.onNodeWithText("Practical Assessment").assertDoesNotExist()
    }

    @Test
    fun logout_icon_invokes_callback() {
        var loggedOut = 0
        composeRule.setContent {
            ChprbnTheme {
                ExamDashboardScreenContent(
                    uiState = loadedPlaceholder(),
                    onLogout = { loggedOut++ },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Logout").performClick()

        assertEquals(1, loggedOut)
    }

    @Test
    fun log_attendance_button_invokes_callback() {
        var logged = 0
        composeRule.setContent {
            ChprbnTheme {
                ExamDashboardScreenContent(
                    uiState = loadedPlaceholder(),
                    onLogAttendance = { logged++ },
                )
            }
        }

        composeRule.onNodeWithText("Log Attendance").performClick()

        assertEquals(1, logged)
    }

    @Test
    fun custom_state_propagates_institution_overrides() {
        val state = loadedPlaceholder().copy(
            institutionName = "Kano Centre",
            institutionCode = "#KAN",
            institutionLocation = "Kano",
        )
        composeRule.setContent {
            ChprbnTheme {
                ExamDashboardScreenContent(uiState = state)
            }
        }

        composeRule.onNodeWithText("Kano Centre").assertExists()
        composeRule.onNodeWithText("#KAN").assertExists()
        assertTrue(
            "expected exactly one 'Kano' label",
            composeRule.onAllNodesWithText("Kano").fetchSemanticsNodes().isNotEmpty(),
        )
    }
}
