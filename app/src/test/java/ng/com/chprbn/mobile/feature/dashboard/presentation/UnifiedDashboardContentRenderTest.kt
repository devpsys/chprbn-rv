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
    fun default_state_renders_feature_grid_and_welcome_copy() {
        composeRule.setContent {
            ChprbnTheme {
                UnifiedDashboardContent(
                    uiState = DashboardUiState(),
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
}
