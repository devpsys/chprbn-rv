package ng.com.chprbn.mobile.feature.exam.presentation

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h2400dp")
class CandidateProfileContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_candidate_identity_and_each_remark() {
        composeRule.setContent {
            ChprbnTheme {
                CandidateProfileContent(
                    uiState = CandidateProfileUiState(
                        candidateName = "Jane Doe",
                        examNumberLabel = "ID: EX-1",
                        photoUrl = null,
                        statusPillLabel = "Signed In",
                        remarks = listOf(
                            RemarkRowUiState(
                                id = "r1",
                                body = "Absenteeism",
                                severity = RemarkSeverity.Info,
                                createdAtLabel = "Jun 12, 2024 at 9:05 AM",
                            ),
                            RemarkRowUiState(
                                id = "r2",
                                body = "Exams Malpractice",
                                severity = RemarkSeverity.Critical,
                                createdAtLabel = "Jun 13, 2024 at 10:00 AM",
                            ),
                        ),
                        hasLoaded = true,
                    ),
                    onBack = {},
                    onClearAllRemarks = {},
                )
            }
        }

        composeRule.onNodeWithText("Jane Doe").assertExists()
        composeRule.onNodeWithText("ID: EX-1").assertExists()
        composeRule.onNodeWithText("Signed In").assertExists()
        composeRule.onNodeWithText("Absenteeism").assertExists()
        composeRule.onNodeWithText("Jun 12, 2024 at 9:05 AM").assertExists()
        composeRule.onNodeWithText("Exams Malpractice").assertExists()
        composeRule.onNodeWithText("Clear All Remarks").assertExists()
    }

    @Test
    fun defaults_the_status_pill_to_Pending_when_unset() {
        composeRule.setContent {
            ChprbnTheme {
                CandidateProfileContent(
                    uiState = CandidateProfileUiState(
                        candidateName = "Jane Doe",
                        examNumberLabel = "ID: EX-1",
                        photoUrl = null,
                        remarks = emptyList(),
                        hasLoaded = true,
                    ),
                    onBack = {},
                    onClearAllRemarks = {},
                )
            }
        }

        composeRule.onNodeWithText("Pending").assertExists()
    }

    @Test
    fun empty_remarks_shows_the_empty_state_and_hides_the_clear_button() {
        composeRule.setContent {
            ChprbnTheme {
                CandidateProfileContent(
                    uiState = CandidateProfileUiState(
                        candidateName = "Jane Doe",
                        examNumberLabel = "ID: EX-1",
                        photoUrl = null,
                        remarks = emptyList(),
                        hasLoaded = true,
                    ),
                    onBack = {},
                    onClearAllRemarks = {},
                )
            }
        }

        composeRule.onNodeWithText("No Remarks Logged").assertExists()
        composeRule.onNodeWithText("Clear All Remarks").assertDoesNotExist()
    }

    @Test
    fun clear_all_button_invokes_the_callback() {
        var cleared = 0
        composeRule.setContent {
            ChprbnTheme {
                CandidateProfileContent(
                    uiState = CandidateProfileUiState(
                        candidateName = "Jane Doe",
                        examNumberLabel = "ID: EX-1",
                        photoUrl = null,
                        remarks = listOf(
                            RemarkRowUiState(
                                id = "r1",
                                body = "Absenteeism",
                                severity = RemarkSeverity.Info,
                                createdAtLabel = "Jun 12, 2024 at 9:05 AM",
                            ),
                        ),
                        hasLoaded = true,
                    ),
                    onBack = {},
                    onClearAllRemarks = { cleared++ },
                )
            }
        }

        composeRule.onNodeWithText("Clear All Remarks").performClick()

        assertEquals(1, cleared)
    }

    @Test
    fun not_found_state_renders_instead_of_candidate_content() {
        composeRule.setContent {
            ChprbnTheme {
                CandidateProfileContent(
                    uiState = CandidateProfileUiState(
                        candidateName = "",
                        examNumberLabel = "",
                        photoUrl = null,
                        remarks = emptyList(),
                        hasLoaded = true,
                        notFound = true,
                    ),
                    onBack = {},
                    onClearAllRemarks = {},
                )
            }
        }

        composeRule.onNodeWithText("Candidate Not Found").assertExists()
    }
}
