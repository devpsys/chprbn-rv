package ng.com.chprbn.mobile.feature.assessment.presentation

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
class AssessmentPaperDetailContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_paper_title_and_candidate_preview() {
        // Note: facility / hall fields aren't currently bound (the
        // FacilityAndHallRow item is commented out in
        // AssessmentPaperDetailContent), so we only assert on the values
        // the screen actually surfaces today.
        val state = AssessmentPaperDetailUiState(
            paperTitle = "Pharmacology — Paper A",
            statusLabel = "Active Examination",
            progressFraction = 0.6f,
            checkedInCount = 12,
            totalCount = 20,
            facilityName = "Lagos University Teaching Hospital",
            facilityAddress = "10 Marina Road",
            hallName = "Practical Hall B",
            hallAddress = "Block 4, Room 12",
            candidates = listOf(
                CandidateRowUiState("c1", "JD", "Jane Doe", CandidateSyncStatus.Synced),
                CandidateRowUiState("c2", "JS", "John Smith", CandidateSyncStatus.Unsynced),
            ),
            heroImageUrl = null,
        )

        composeRule.setContent {
            ChprbnTheme {
                AssessmentPaperDetailContent(uiState = state)
            }
        }

        composeRule.onNodeWithText("Pharmacology — Paper A").assertExists()
        composeRule.onNodeWithText("Jane Doe").assertExists()
        composeRule.onNodeWithText("John Smith").assertExists()
    }

    @Test
    fun empty_default_state_renders_chrome_without_crashing() {
        composeRule.setContent {
            ChprbnTheme {
                AssessmentPaperDetailContent(uiState = AssessmentPaperDetailUiState())
            }
        }

        // Smoke test: default empty state shouldn't crash, even with no
        // paper title or candidates loaded.
    }
}
