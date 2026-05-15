package ng.com.chprbn.mobile.feature.verification.presentation

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.verification.domain.model.FeatureType
import ng.com.chprbn.mobile.feature.verification.domain.model.VerificationFeature
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h2400dp")
class VerificationContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sampleUser = User(
        id = "u1",
        username = "MED-12345",
        email = "officer@regulator.gov",
        fullName = "Officer Michael Chen",
        accessToken = "",
        permissions = emptyList(),
        userPhoto = null,
        role = "Senior Field Officer",
    )

    private val allFeatures = listOf(
        VerificationFeature(FeatureType.ScanQr, isPrimary = true),
        VerificationFeature(FeatureType.VerifiedList, isPrimary = false),
        VerificationFeature(FeatureType.Sync, isPrimary = false),
        VerificationFeature(FeatureType.Profile, isPrimary = false),
    )

    @Test
    fun success_state_renders_welcome_and_all_feature_cards() {
        composeRule.setContent {
            ChprbnTheme {
                VerificationContent(
                    user = sampleUser,
                    featureList = allFeatures,
                    onFeatureClick = {},
                    onScanQr = {},
                    onVerifiedList = {},
                    onSync = {},
                    onProfile = {},
                    isLoading = false,
                )
            }
        }

        composeRule.onNodeWithText("Scan License QR").assertExists()
        composeRule.onNodeWithText("Verified Practitioners").assertExists()
        composeRule.onNodeWithText("Sync Records").assertExists()
        composeRule.onNodeWithText("Profile").assertExists()
    }

    @Test
    fun error_state_surfaces_message_and_retry_action() {
        composeRule.setContent {
            ChprbnTheme {
                VerificationContent(
                    user = null,
                    featureList = emptyList(),
                    onFeatureClick = {},
                    onScanQr = {},
                    onVerifiedList = {},
                    onSync = {},
                    onProfile = {},
                    isLoading = false,
                    errorMessage = "Could not load practitioner profile.",
                )
            }
        }

        composeRule.onNodeWithText("Could not load practitioner profile.").assertExists()
        // The retry button copy comes from R.string.verification_retry_action.
        assertTrue(
            composeRule.onAllNodesWithText("Retry").fetchSemanticsNodes().isNotEmpty(),
        )
    }
}
