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
class CriteriaChecklistContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun checklist_renders_header_and_criterion_groups() {
        composeRule.setContent {
            ChprbnTheme {
                CriteriaChecklistContent(
                    uiState = CriteriaChecklistUiState.placeholder(),
                )
            }
        }

        composeRule.onNodeWithText("ACCREDITATION CRITERIA").assertExists()
        composeRule.onNodeWithText("6 of 8 Evaluated").assertExists()
        composeRule.onNodeWithText("Location & Land Area").assertExists()
        composeRule.onNodeWithText("Human Resources Cadre").assertExists()
        composeRule.onNodeWithText("Rural / semi-urban setting verified").assertExists()
        composeRule.onNodeWithText("Save Checklist Draft").assertExists()
        composeRule.onNodeWithText("Continue to Score Entry").assertExists()
    }
}
