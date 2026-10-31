package ng.com.chprbn.mobile.feature.accreditation.presentation

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h2400dp")
class SsqSectionFormContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun section_a_renders_ssq_a1_and_a2_fields() {
        composeRule.setContent {
            ChprbnTheme {
                SsqSectionFormContent(
                    uiState = SsqSectionFormUiState.placeholder(sectionKey = SsqSectionKey.A),
                )
            }
        }

        composeRule.onNodeWithText("Section A: General Information").assertExists()
        composeRule.onNodeWithText("Federal").assertExists()
        composeRule.onNodeWithText("CHEW / HND").assertExists()
        composeRule.onNodeWithText("JCHEW / ND").assertExists()
        composeRule.onNodeWithText("Approved Number of Students (BCHS)").assertExists()
        composeRule.onNodeWithText("Number of Indexed Students").assertExists()
        composeRule.onNodeWithText("Number of Unindexed Students").assertExists()
        composeRule.onNodeWithText("NBTE Accreditation").assertExists()
        composeRule.onNodeWithText("Number of Students Enrolled").assertExists()
        composeRule.onNodeWithText("Institutional Staff Summary").assertExists()
        composeRule.onNodeWithText("Academic").assertExists()
        composeRule.onNodeWithText("Technical").assertExists()
        composeRule.onAllNodesWithText("Dr. Aisha Mohammed Garba")[0].assertExists()
        composeRule.onAllNodesWithText("Highest Qualification")[0].assertExists()
        composeRule.onAllNodesWithText("Employment Status")[0].assertExists()
        composeRule.onNodeWithText("Add New PHC Tutor").assertExists()
        composeRule.onNodeWithText("Save Section Draft").assertExists()
        composeRule.onNodeWithText("Supporting Documents & Evidence").assertExists()
    }

    @Test
    fun section_b_renders_ssq_b1_and_b2_items() {
        composeRule.setContent {
            ChprbnTheme {
                SsqSectionFormContent(
                    uiState = SsqSectionFormUiState.placeholder(sectionKey = SsqSectionKey.B),
                )
            }
        }

        composeRule.onNodeWithText("SECTION B: PHILOSOPHY & GOVERNANCE").assertExists()
        composeRule.onNodeWithText("B1: Objectives").assertExists()
        composeRule.onNodeWithText("B2: Governance").assertExists()
        composeRule.onNodeWithText(
            "Clearly stated philosophy aligned with national health goals",
        ).assertExists()
        composeRule.onNodeWithText(
            "ICT governance and e-learning policy available",
        ).assertExists()
        composeRule.onNodeWithText("Functional departmental board").assertExists()
        composeRule.onAllNodesWithText("Available")[0].assertExists()
        composeRule.onNodeWithText("Upload Evidence & Minutes (B1)").assertExists()
        composeRule.onNodeWithText("Upload Evidence & Minutes (B2)").assertExists()
        composeRule.onNodeWithText("3 of 20 Evaluated").assertExists()
    }

    @Test
    fun section_c_renders_checklist_availability_controls() {
        composeRule.setContent {
            ChprbnTheme {
                SsqSectionFormContent(
                    uiState = SsqSectionFormUiState.placeholder(sectionKey = SsqSectionKey.C),
                )
            }
        }

        composeRule.onNodeWithText("Section C: Curriculum & Teaching Methods").assertExists()
        composeRule.onAllNodesWithText("Available")[0].assertExists()
        composeRule.onNodeWithText("Save as Draft").assertExists()
    }
}
