package ng.com.chprbn.mobile.feature.accreditation.presentation

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
        composeRule.onNodeWithText("Subsection A2: PHC Tutor Roster").assertExists()
        composeRule.onNodeWithText("Dr. Aisha Mohammed Garba").assertExists()
        composeRule.onNodeWithText("Senior Primary Health Care Tutor").assertExists()
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
    fun section_c_renders_ssq_c1_teaching_methods() {
        composeRule.setContent {
            ChprbnTheme {
                SsqSectionFormContent(
                    uiState = SsqSectionFormUiState.placeholder(sectionKey = SsqSectionKey.C),
                )
            }
        }

        composeRule.onNodeWithText("SECTION C: CURRICULUM & TEACHING METHODS").assertExists()
        composeRule.onNodeWithText("Core Methods").assertExists()
        composeRule.onNodeWithText("Digital Tech").assertExists()
        composeRule.onNodeWithText("Assessment").assertExists()
        composeRule.onNodeWithText(
            "Competency-Based Medical Education (CBME)",
        ).assertExists()
        composeRule.onAllNodesWithText("Available")[0].assertExists()
        composeRule.onNodeWithText("8 of 18 Evaluated").assertExists()
        composeRule.onNodeWithText("Upload Evidence & Syllabi").assertExists()
        composeRule.onNodeWithText("Save as Draft").assertExists()
    }

    @Test
    fun section_d_renders_ssq_d1_d2_d3() {
        composeRule.setContent {
            ChprbnTheme {
                SsqSectionFormContent(
                    uiState = SsqSectionFormUiState.placeholder(sectionKey = SsqSectionKey.D),
                )
            }
        }

        composeRule.onNodeWithText("SECTION D: STAFFING & ACADEMIC ADMINISTRATION").assertExists()
        composeRule.onNodeWithText("D1: Cadre").assertExists()
        composeRule.onNodeWithText("D2: Standards").assertExists()
        composeRule.onNodeWithText("D3: Support").assertExists()
        composeRule.onNodeWithText("D1: Academic Teaching Cadre").assertExists()
        composeRule.onNodeWithText("Professor").assertExists()
        composeRule.onNodeWithText("Associate Professor/Reader").assertExists()
        composeRule.onNodeWithText("Clinical Instructors").assertExists()
        composeRule.onNodeWithText("Adjunct/Visiting Lecturers").assertExists()
        composeRule.onAllNodesWithText("Full-Time")[0].assertExists()
        composeRule.onAllNodesWithText("Part-Time")[0].assertExists()
        composeRule.onNodeWithText("Upload Evidence & Staff Dossier").assertExists()
        composeRule.onNodeWithText("23 of 29 Evaluated").assertExists()

        composeRule.onNodeWithText("D2: Standards").performClick()
        composeRule.onNodeWithText(
            "Staff professionally registered with CHPRBN",
        ).assertExists()
        composeRule.onNodeWithText(
            "International collaborations/exchanges available",
        ).assertExists()
        composeRule.onAllNodesWithText("Compliant (Yes)")[0].assertExists()

        composeRule.onNodeWithText("D3: Support").performClick()
        composeRule.onNodeWithText("Administrative Officers").assertExists()
        composeRule.onNodeWithText("Laboratory Scientists/Technologists").assertExists()
        composeRule.onNodeWithText("Simulation Technicians").assertExists()
        composeRule.onNodeWithText("Support Staff").assertExists()
    }
}
