package ng.com.chprbn.mobile.feature.profile.presentation

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.feature.auth.domain.model.User
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h2400dp")
class ProfileContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sampleUser = User(
        id = "u1",
        username = "MED-12345",
        email = "michael.chen@regulator.gov",
        fullName = "Michael Chen",
        accessToken = "",
        permissions = emptyList(),
        userPhoto = null,
        role = "Senior Field Officer",
        organization = "CHPRBN",
        lastLoginAt = "Today, 10:30 AM",
    )

    @Test
    fun success_state_renders_user_fields_and_security_actions() {
        composeRule.setContent {
            ChprbnTheme {
                ProfileContent(
                    state = ProfileUiState.Success(sampleUser),
                )
            }
        }

        composeRule.onNodeWithText("Michael Chen").assertExists()
        composeRule.onNodeWithText("michael.chen@regulator.gov").assertExists()
        composeRule.onNodeWithText("Today, 10:30 AM").assertExists()
        composeRule.onNodeWithText("Change Password").assertExists()
        composeRule.onNodeWithText("Logout").assertExists()
    }

    @Test
    fun loading_state_renders_chrome_without_crashing() {
        composeRule.setContent {
            ChprbnTheme {
                ProfileContent(state = ProfileUiState.Loading)
            }
        }
        // Smoke test — Loading should compose without throwing.
    }
}
