package ng.com.chprbn.mobile.feature.auth.presentation.splash

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
class SplashContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_brand_and_loading_strings() {
        composeRule.setContent {
            ChprbnTheme {
                SplashContent()
            }
        }

        composeRule.onNodeWithText("CHPRBN PORTAL").assertExists()
        composeRule.onNodeWithText("Governing Health with Integrity").assertExists()
        composeRule.onNodeWithText("INITIALIZING SECURE RECORDS").assertExists()
    }
}
