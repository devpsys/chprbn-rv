package ng.com.chprbn.mobile.feature.dashboard.presentation

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
class UnifiedDashboardContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun feature_grid_renders_only_tiles_unlocked_by_user_roles() {
        composeRule.setContent {
            ChprbnTheme {
                UnifiedDashboardContent(
                    uiState = DashboardUiState(
                        roles = listOf("osyvalac", "examination", "accreditation")
                    ),
                    onNavigateToScan = {},
                    onNavigateToExamAttendance = {},
                    onNavigateToPracticalAssessment = {},
                    onNavigateToAccreditation = {},
                    onViewRecentLogs = {},
                )
            }
        }

        composeRule.onNodeWithText("OSYVALAC").assertExists()
        composeRule.onNodeWithText("EXAMS").assertExists()
        composeRule.onNodeWithText("ACCREDITATION").assertExists()
    }

    @Test
    fun feature_grid_hides_tiles_the_user_has_no_role_for() {
        composeRule.setContent {
            ChprbnTheme {
                UnifiedDashboardContent(
                    uiState = DashboardUiState(roles = listOf("osyvalac")),
                    onNavigateToScan = {},
                    onNavigateToExamAttendance = {},
                    onNavigateToPracticalAssessment = {},
                    onNavigateToAccreditation = {},
                    onViewRecentLogs = {},
                )
            }
        }

        composeRule.onNodeWithText("OSYVALAC").assertExists()
        composeRule.onNodeWithText("EXAMS").assertDoesNotExist()
        composeRule.onNodeWithText("ACCREDITATION").assertDoesNotExist()
    }

    @Test
    fun feature_grid_is_empty_when_user_has_no_matching_roles() {
        composeRule.setContent {
            ChprbnTheme {
                UnifiedDashboardContent(
                    uiState = DashboardUiState(roles = emptyList()),
                    onNavigateToScan = {},
                    onNavigateToExamAttendance = {},
                    onNavigateToPracticalAssessment = {},
                    onNavigateToAccreditation = {},
                    onViewRecentLogs = {},
                )
            }
        }

        composeRule.onNodeWithText("OSYVALAC").assertDoesNotExist()
        composeRule.onNodeWithText("EXAMS").assertDoesNotExist()
        composeRule.onNodeWithText("ACCREDITATION").assertDoesNotExist()
    }
}
