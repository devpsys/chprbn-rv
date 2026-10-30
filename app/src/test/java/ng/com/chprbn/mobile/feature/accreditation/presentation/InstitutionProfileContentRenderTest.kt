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
class InstitutionProfileContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun placeholder_renders_name_sections_and_actions() {
        composeRule.setContent {
            ChprbnTheme {
                InstitutionProfileContent(uiState = InstitutionProfileUiState.placeholder())
            }
        }

        composeRule.onNodeWithText("Institution Profile").assertExists()
        composeRule.onNodeWithText("Ahmadu Bello University Teaching Hospital").assertExists()
        composeRule.onNodeWithText("General & Geographic Info").assertExists()
        composeRule.onNodeWithText("Programmes Offered").assertExists()
        composeRule.onNodeWithText("Institutional Capacity").assertExists()
        composeRule.onNodeWithText("Accreditation History").assertExists()
        composeRule.onNodeWithText("Start New Assessment").assertExists()
        composeRule.onNodeWithText("View Documents").assertExists()
        composeRule.onNodeWithText("Cycle 2021 / 2022").assertExists()
        composeRule.onNodeWithText("Score: 84.5%").assertExists()
    }

    @Test
    fun not_found_renders_empty_copy() {
        composeRule.setContent {
            ChprbnTheme {
                InstitutionProfileContent(
                    uiState = InstitutionProfileUiState.NotFound("missing-id"),
                )
            }
        }

        composeRule.onNodeWithText("Institution not found").assertExists()
        composeRule.onNodeWithText("Back to Hub").assertExists()
    }
}
