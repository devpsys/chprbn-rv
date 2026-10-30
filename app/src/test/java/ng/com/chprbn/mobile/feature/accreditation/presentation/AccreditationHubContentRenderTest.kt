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
class AccreditationHubContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun placeholder_renders_assigned_institution_and_feature_tiles() {
        composeRule.setContent {
            ChprbnTheme {
                AccreditationHubContent(uiState = AccreditationHubUiState.placeholder())
            }
        }

        composeRule.onNodeWithText("CHPRBN").assertExists()
        composeRule.onNodeWithText("OFFICIAL USE ONLY").assertExists()
        composeRule.onNodeWithText("Start New Assessment").assertExists()
        composeRule.onNodeWithText("Continue Assessment").assertExists()
        composeRule.onNodeWithText("Local Drafts").assertExists()
        composeRule.onNodeWithText("Sync Status").assertExists()
        composeRule.onNodeWithText("Ahmadu Bello University Teaching Hospital").assertExists()
        composeRule.onNodeWithText("Synced").assertExists()
        composeRule.onNodeWithText("Start Assessment").assertExists()
        composeRule.onNodeWithText("National Institute of Health Technology").assertExists()
    }

    @Test
    fun empty_assigned_state_renders_empty_copy() {
        composeRule.setContent {
            ChprbnTheme {
                AccreditationHubContent(
                    uiState = AccreditationHubUiState.placeholder().copy(
                        assignedInstitutions = emptyList(),
                        upcomingInstitutions = emptyList(),
                    ),
                )
            }
        }

        composeRule.onNodeWithText("No institutions assigned to you").assertExists()
    }
}
