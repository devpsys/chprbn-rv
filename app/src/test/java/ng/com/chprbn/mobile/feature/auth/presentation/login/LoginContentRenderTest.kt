package ng.com.chprbn.mobile.feature.auth.presentation.login

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
class LoginContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun idle_state_renders_form_fields_and_sign_in_button() {
        composeRule.setContent {
            ChprbnTheme {
                LoginContent(
                    uiState = LoginUiState(),
                    onSignInClick = { _, _ -> },
                )
            }
        }

        composeRule.onNodeWithText("Verification Login").assertExists()
        composeRule.onNodeWithText("Username").assertExists()
        composeRule.onNodeWithText("Access Key").assertExists()
        composeRule.onNodeWithText("Sign In").assertExists()
    }

    @Test
    fun error_state_surfaces_error_message() {
        composeRule.setContent {
            ChprbnTheme {
                LoginContent(
                    uiState = LoginUiState(errorMessage = "Username and password are required."),
                    onSignInClick = { _, _ -> },
                )
            }
        }

        composeRule.onNodeWithText("Username and password are required.").assertExists()
    }
}
