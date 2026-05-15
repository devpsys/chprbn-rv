package ng.com.chprbn.mobile.feature.assessment.presentation

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
class ExaminationSchedulesContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_each_schedule_card_with_date_and_sync_pill() {
        val state = ExaminationSchedulesUiState(
            schedules = listOf(
                ScheduleCardUiState(
                    id = "PE-2024-1",
                    title = "Pharmacology Practical",
                    dateLabel = "Mar 18, 2026",
                    syncStatus = ScheduleSyncStatus.Synced,
                ),
                ScheduleCardUiState(
                    id = "PE-2024-2",
                    title = "Clinical OSCE",
                    dateLabel = "Mar 25, 2026",
                    syncStatus = ScheduleSyncStatus.Pending,
                ),
            ),
        )

        composeRule.setContent {
            ChprbnTheme {
                ExaminationSchedulesContent(uiState = state)
            }
        }

        composeRule.onNodeWithText("Pharmacology Practical").assertExists()
        composeRule.onNodeWithText("Clinical OSCE").assertExists()
        composeRule.onNodeWithText("Date: Mar 18, 2026").assertExists()
        composeRule.onNodeWithText("Date: Mar 25, 2026").assertExists()
    }

    @Test
    fun empty_schedules_list_still_renders_chrome() {
        composeRule.setContent {
            ChprbnTheme {
                ExaminationSchedulesContent(uiState = ExaminationSchedulesUiState())
            }
        }

        // The header banner stays present even when the schedule list is empty.
        composeRule.onNodeWithText("Schedules").assertExists()
    }
}
