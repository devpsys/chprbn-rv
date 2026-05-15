package ng.com.chprbn.mobile.feature.verification.presentation

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
class VerifiedListContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun practitioner(
        name: String,
        license: String,
        status: VerifiedStatus = VerifiedStatus.Active,
    ) = VerifiedPractitioner(
        name = name,
        license = license,
        status = status,
        expiryText = "Dec 2026",
        verifiedAtText = "Oct 24, 2023",
        syncStatus = VerifiedSyncStatus.Synced,
        photoUrl = null,
    )

    @Test
    fun success_state_renders_each_practitioner_row() {
        composeRule.setContent {
            ChprbnTheme {
                VerifiedListContent(
                    uiState = VerifiedListUiState(
                        practitioners = listOf(
                            practitioner("Dr. Sarah Jenkins", "MED-12345"),
                            practitioner("Dr. Aisha Bello", "MED-67890"),
                        ),
                        isLoading = false,
                    ),
                )
            }
        }

        composeRule.onNodeWithText("Dr. Sarah Jenkins").assertExists()
        composeRule.onNodeWithText("Dr. Aisha Bello").assertExists()
    }

    @Test
    fun empty_state_renders_no_records_copy() {
        composeRule.setContent {
            ChprbnTheme {
                VerifiedListContent(
                    uiState = VerifiedListUiState(
                        practitioners = emptyList(),
                        isLoading = false,
                    ),
                )
            }
        }

        composeRule.onNodeWithText("No Records Found").assertExists()
    }

    @Test
    fun loading_state_renders_loading_caption() {
        composeRule.setContent {
            ChprbnTheme {
                VerifiedListContent(
                    uiState = VerifiedListUiState(isLoading = true),
                )
            }
        }

        composeRule.onNodeWithText("Loading verified practitioners…").assertExists()
    }
}
