package ng.com.chprbn.mobile.feature.verification.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecordResult
import ng.com.chprbn.mobile.feature.verification.domain.model.SaveVerifiedLicenseResult
import ng.com.chprbn.mobile.feature.verification.domain.usecase.GetLicenseRecordUseCase
import ng.com.chprbn.mobile.feature.verification.domain.usecase.SaveVerifiedLicenseUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VerificationFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getLicenseRecordUseCase: GetLicenseRecordUseCase
    private lateinit var saveVerifiedLicenseUseCase: SaveVerifiedLicenseUseCase
    private lateinit var context: Context

    @Before
    fun setUp() {
        getLicenseRecordUseCase = mockk()
        saveVerifiedLicenseUseCase = mockk()
        val resources = mockk<android.content.res.Resources> {
            every { getStringArray(R.array.officer_remark_options) } returns sampleOfficerRemarkOptions
        }
        context = mockk {
            every { getString(R.string.verification_form_error_no_record) } returns
                "No license record found to verify."
            every { this@mockk.resources } returns resources
        }
        // Uri.decode is an Android framework call; stub statically so JVM tests can run
        // without Robolectric. Identity-decode keeps the registration number unchanged.
        mockkStatic(Uri::class)
        every { Uri.decode(any<String>()) } answers { firstArg<String>() }
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    @Test
    fun `missing registrationNumber arg goes to NotFound without invoking the use case`() = runTest {
        val viewModel = makeViewModel(SavedStateHandle())

        assertEquals(VerificationFormLoadState.NotFound, viewModel.uiState.value.loadState)
        assertNull(viewModel.uiState.value.licenseRecord)
        coVerify(exactly = 0) { getLicenseRecordUseCase(any()) }
    }

    @Test
    fun `whitespace-only registrationNumber goes to NotFound`() = runTest {
        val viewModel = makeViewModel(savedStateWith("   "))

        assertEquals(VerificationFormLoadState.NotFound, viewModel.uiState.value.loadState)
        coVerify(exactly = 0) { getLicenseRecordUseCase(any()) }
    }

    @Test
    fun `successful fetch transitions to Loaded with the record`() = runTest {
        val record = sampleRecord("REG-123")
        coEvery { getLicenseRecordUseCase("REG-123") } returns LicenseRecordResult.Success(record)

        val viewModel = makeViewModel(savedStateWith("REG-123"))

        assertEquals(VerificationFormLoadState.Loaded, viewModel.uiState.value.loadState)
        assertEquals(record, viewModel.uiState.value.licenseRecord)
    }

    @Test
    fun `use case NotFound surfaces NotFound load state with null record`() = runTest {
        coEvery { getLicenseRecordUseCase("REG-MISSING") } returns LicenseRecordResult.NotFound

        val viewModel = makeViewModel(savedStateWith("REG-MISSING"))

        assertEquals(VerificationFormLoadState.NotFound, viewModel.uiState.value.loadState)
        assertNull(viewModel.uiState.value.licenseRecord)
    }

    @Test
    fun `use case Error surfaces Error load state with the message`() = runTest {
        coEvery { getLicenseRecordUseCase("REG-FAIL") } returns
            LicenseRecordResult.Error("Network unavailable")

        val viewModel = makeViewModel(savedStateWith("REG-FAIL"))

        val loadState = viewModel.uiState.value.loadState
        assertTrue(loadState is VerificationFormLoadState.Error)
        assertEquals(
            "Network unavailable",
            (loadState as VerificationFormLoadState.Error).message
        )
    }

    @Test
    fun `onOfficerRemarkSelected updates state`() = runTest {
        val viewModel = makeViewModel(SavedStateHandle())

        viewModel.onOfficerRemarkSelected("Documents verified")

        assertEquals("Documents verified", viewModel.uiState.value.selectedOfficerRemark)
    }

    @Test
    fun `saveVerification with no record emits Error without invoking the save use case`() = runTest {
        val viewModel = makeViewModel(SavedStateHandle())

        viewModel.saveVerification()

        val saveState = viewModel.uiState.value.saveState
        assertTrue(saveState is SaveVerificationState.Error)
        assertEquals(
            "No license record found to verify.",
            (saveState as SaveVerificationState.Error).message
        )
        coVerify(exactly = 0) { saveVerifiedLicenseUseCase(any(), any(), any()) }
    }

    @Test
    fun `saveVerification success transitions through Saving to Success`() = runTest {
        val record = sampleRecord("REG-123")
        coEvery { getLicenseRecordUseCase("REG-123") } returns LicenseRecordResult.Success(record)
        coEvery {
            saveVerifiedLicenseUseCase(record, "Documents verified", any())
        } returns SaveVerifiedLicenseResult.Success

        val viewModel = makeViewModel(savedStateWith("REG-123"))
        viewModel.onOfficerRemarkSelected("Documents verified")

        viewModel.saveVerification()

        assertEquals(SaveVerificationState.Success, viewModel.uiState.value.saveState)
        coVerify(exactly = 1) {
            saveVerifiedLicenseUseCase(record, "Documents verified", any())
        }
    }

    @Test
    fun `saveVerification propagates use-case error message`() = runTest {
        val record = sampleRecord("REG-123")
        coEvery { getLicenseRecordUseCase("REG-123") } returns LicenseRecordResult.Success(record)
        coEvery {
            saveVerifiedLicenseUseCase(record, any(), any())
        } returns SaveVerifiedLicenseResult.Error("DB locked")

        val viewModel = makeViewModel(savedStateWith("REG-123"))
        viewModel.onOfficerRemarkSelected("Documents verified")

        viewModel.saveVerification()

        val saveState = viewModel.uiState.value.saveState
        assertTrue(saveState is SaveVerificationState.Error)
        assertEquals("DB locked", (saveState as SaveVerificationState.Error).message)
    }

    @Test
    fun `consumeSaveState resets save state to Idle`() = runTest {
        val record = sampleRecord("REG-123")
        coEvery { getLicenseRecordUseCase("REG-123") } returns LicenseRecordResult.Success(record)
        coEvery {
            saveVerifiedLicenseUseCase(record, any(), any())
        } returns SaveVerifiedLicenseResult.Success

        val viewModel = makeViewModel(savedStateWith("REG-123"))
        viewModel.onOfficerRemarkSelected("Documents verified")
        viewModel.saveVerification()
        // sanity: we're at Success now
        assertEquals(SaveVerificationState.Success, viewModel.uiState.value.saveState)

        viewModel.consumeSaveState()

        assertEquals(SaveVerificationState.Idle, viewModel.uiState.value.saveState)
    }

    @Test
    fun `officerRemarkOptions are loaded from the string-array resource into uiState`() = runTest {
        val viewModel = makeViewModel(SavedStateHandle())

        val options = viewModel.uiState.value.officerRemarkOptions
        assertEquals(sampleOfficerRemarkOptions.toList(), options)
        assertTrue(options.all { it.isNotBlank() })
    }

    private fun makeViewModel(savedStateHandle: SavedStateHandle) =
        VerificationFormViewModel(savedStateHandle, getLicenseRecordUseCase, saveVerifiedLicenseUseCase, context)

    private fun savedStateWith(registrationNumber: String) =
        SavedStateHandle(mapOf("registrationNumber" to registrationNumber))

    private val sampleOfficerRemarkOptions = arrayOf(
        "Documents verified; identity matches register",
        "Practitioner present; credentials checked",
        "Routine verification completed",
        "License confirmed valid for practice"
    )

    private fun sampleRecord(registrationNumber: String) = LicenseRecord(
        registrationNumber = registrationNumber,
        fullName = "Jane Doe",
        photoUrl = null,
        profession = "Pharmacist",
        licenseStatus = "Active",
        expiryDate = "2027-01-01",
        subtitle = null
    )
}
