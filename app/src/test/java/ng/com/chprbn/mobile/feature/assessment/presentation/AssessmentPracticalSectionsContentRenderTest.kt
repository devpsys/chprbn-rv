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
class AssessmentPracticalSectionsContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_candidate_header_section_cards_and_stats() {
        val state = AssessmentPracticalSectionsUiState(
            candidateName = "Aisha Bello",
            candidateExamId = "EX-2024-007",
            candidatePhotoUrl = null,
            sectionsDone = 1,
            sectionsTotal = 3,
            sectionsRemaining = 2,
            sections = listOf(
                // footerText is fed into the screen's status-specific format
                // string (Complete: "Updated: %s", Incomplete: "%d tasks
                // remaining"), so we pass the raw values not the final
                // display copy.
                PracticalSectionUiState(
                    id = "A",
                    sectionTitle = "Section A",
                    sectionSubtitle = "Patient Assessment",
                    status = PracticalSectionStatus.Complete,
                    footerText = "09:45 AM",
                ),
                PracticalSectionUiState(
                    id = "B",
                    sectionTitle = "Section B",
                    sectionSubtitle = "Clinical Examination",
                    status = PracticalSectionStatus.Incomplete,
                    footerText = "2",
                ),
                PracticalSectionUiState(
                    id = "C",
                    sectionTitle = "Section C",
                    sectionSubtitle = "Communication",
                    status = PracticalSectionStatus.NotStarted,
                    footerText = "",
                ),
            ),
        )

        composeRule.setContent {
            ChprbnTheme {
                AssessmentPracticalSectionsContent(uiState = state)
            }
        }

        composeRule.onNodeWithText("Aisha Bello").assertExists()
        composeRule.onNodeWithText("Patient Assessment").assertExists()
        composeRule.onNodeWithText("Clinical Examination").assertExists()
        composeRule.onNodeWithText("Communication").assertExists()
        composeRule.onNodeWithText("Updated: 09:45 AM").assertExists()
        composeRule.onNodeWithText("2 tasks remaining").assertExists()
        composeRule.onNodeWithText("No data recorded").assertExists()
    }

    @Test
    fun empty_default_state_renders_without_crashing() {
        composeRule.setContent {
            ChprbnTheme {
                AssessmentPracticalSectionsContent(uiState = AssessmentPracticalSectionsUiState())
            }
        }
        // Smoke test: empty state must not crash.
    }
}
