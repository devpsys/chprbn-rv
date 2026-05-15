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
class ManualEntryContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun license_variant_renders_verify_practitioner_copy() {
        composeRule.setContent {
            ChprbnTheme {
                ManualEntryContent(
                    forExamIndexing = false,
                    uiState = ManualEntryUiState(licenseNumber = ""),
                )
            }
        }

        composeRule.onNodeWithText("Verify Practitioner").assertExists()
        composeRule.onNodeWithText("Search License").assertExists()
    }

    @Test
    fun exam_indexing_variant_renders_candidate_lookup_copy() {
        composeRule.setContent {
            ChprbnTheme {
                ManualEntryContent(
                    forExamIndexing = true,
                    uiState = ManualEntryUiState(licenseNumber = ""),
                )
            }
        }

        composeRule.onNodeWithText("Look Up Candidate").assertExists()
        composeRule.onNodeWithText("Find Candidate").assertExists()
    }
}
