package ng.com.chprbn.mobile.feature.exam.presentation

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
class CandidateScanResultContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun sampleUiState(examNumber: String = "ABC-12345-XY") = CandidateScanResultUiState(
        candidateName = "Johnathan Doe",
        examNumberLine = "Exam Number: $examNumber",
        verificationSectionLabel = "Identity Verification",
        identityVerifiedHeadline = "Identity Verified",
        matchLabel = "MATCH 98%",
        examDateCaption = "Exam Date",
        examDateValue = "Oct 24, 2023",
        testingCenterCaption = "Testing Center",
        testingCenterValue = "Hall B - Room 12",
    )

    @Test
    fun renders_candidate_name_exam_number_and_verification_pill() {
        composeRule.setContent {
            ChprbnTheme {
                CandidateScanResultContent(
                    uiState = sampleUiState(),
                    onBack = {},
                    onMarkAttendance = {},
                    onCancel = {},
                )
            }
        }

        composeRule.onNodeWithText("Johnathan Doe").assertExists()
        composeRule.onNodeWithText("Exam Number: ABC-12345-XY").assertExists()
        composeRule.onNodeWithText("Identity Verified").assertExists()
        composeRule.onNodeWithText("MATCH 98%").assertExists()
        composeRule.onNodeWithText("Hall B - Room 12").assertExists()
    }

    @Test
    fun scanned_payload_propagates_into_exam_number_line() {
        composeRule.setContent {
            ChprbnTheme {
                CandidateScanResultContent(
                    uiState = sampleUiState(examNumber = "ZZZ-99999-AA"),
                    onBack = {},
                    onMarkAttendance = {},
                    onCancel = {},
                )
            }
        }

        composeRule.onNodeWithText("Exam Number: ZZZ-99999-AA").assertExists()
    }
}
