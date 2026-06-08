package ng.com.chprbn.mobile.feature.verification.presentation

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import ng.com.chprbn.mobile.core.designsystem.ChprbnTheme
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h2400dp")
class VerificationFormContentRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sampleRecord = LicenseRecord(
        registrationNumber = "MED-12345",
        fullName = "Dr. Sarah Jenkins",
        photoUrl = null,
        profession = "Physician",
        certificateNo = "CERT-001",
        email = "sarah@example.com",
        phone = "08012345678",
        licenseStatus = "Active",
        expiryDate = "Dec 2026",
        subtitle = "Senior Practitioner",
        issueDate = "Jan 2024",
        gender = "Female",
        graduationDate = "Jun 2010",
        institutionAttended = null,
    )

    @Test
    fun loaded_state_renders_header_and_save_action() {
        // The form-level Content composable today only renders the officer
        // remark dropdown + report-irregularity + save footer; the
        // practitioner name itself is shown by sibling screens (RecordDetail)
        // earlier in the flow, so we don't assert on `sampleRecord.fullName`.
        composeRule.setContent {
            ChprbnTheme {
                VerificationFormContent(
                    uiState = VerificationFormUiState(
                        loadState = VerificationFormLoadState.Loaded,
                        licenseRecord = sampleRecord,
                        officerRemarkOptions = listOf("Active"),
                    ),
                )
            }
        }

        composeRule.onNodeWithText("Verify Practitioner").assertExists()
        composeRule.onNodeWithText("Officer remark").assertExists()
        composeRule.onNodeWithText("Save Verification").assertExists()
    }

    @Test
    fun save_error_state_surfaces_message() {
        composeRule.setContent {
            ChprbnTheme {
                VerificationFormContent(
                    uiState = VerificationFormUiState(
                        loadState = VerificationFormLoadState.Loaded,
                        licenseRecord = sampleRecord,
                        saveState = SaveVerificationState.Error("Officer remark is required."),
                    ),
                )
            }
        }

        composeRule.onNodeWithText("Officer remark is required.").assertExists()
    }
}
