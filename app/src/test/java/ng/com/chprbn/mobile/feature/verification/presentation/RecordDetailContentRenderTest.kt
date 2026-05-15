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
class RecordDetailContentRenderTest {

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
    fun success_state_renders_record_fields_and_proceed_action() {
        composeRule.setContent {
            ChprbnTheme {
                RecordDetailContent(
                    state = RecordDetailUiState.Success(sampleRecord),
                    registrationNumber = "MED-12345",
                    record = sampleRecord,
                )
            }
        }

        composeRule.onNodeWithText("Dr. Sarah Jenkins").assertExists()
        composeRule.onNodeWithText("Proceed to Verification").assertExists()
    }

    @Test
    fun not_found_state_renders_no_record_copy() {
        composeRule.setContent {
            ChprbnTheme {
                RecordDetailContent(
                    state = RecordDetailUiState.NotFound,
                    registrationNumber = "MED-99999",
                    record = null,
                )
            }
        }

        composeRule.onNodeWithText("No record found").assertExists()
    }

    @Test
    fun error_state_surfaces_retry_action() {
        composeRule.setContent {
            ChprbnTheme {
                RecordDetailContent(
                    state = RecordDetailUiState.Error("network down"),
                    registrationNumber = "MED-12345",
                    record = null,
                )
            }
        }

        composeRule.onNodeWithText("Connection lost").assertExists()
        composeRule.onNodeWithText("Retry connection").assertExists()
    }
}
