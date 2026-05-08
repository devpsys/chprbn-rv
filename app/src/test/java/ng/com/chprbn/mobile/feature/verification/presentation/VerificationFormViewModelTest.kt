package ng.com.chprbn.mobile.feature.verification.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.verification.domain.model.SaveVerifiedLicenseResult
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

    private val gson = Gson()
    private lateinit var saveVerifiedLicenseUseCase: SaveVerifiedLicenseUseCase

    @Before
    fun setUp() {
        saveVerifiedLicenseUseCase = mockk()
        // Uri.decode is an Android framework call; stub statically so JVM tests can run
        // without Robolectric. Identity-decode keeps the JSON payload unchanged.
        mockkStatic(Uri::class)
        every { Uri.decode(any<String>()) } answers { firstArg<String>() }
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    @Test
    fun `initial state has the license record decoded from the nav arg`() = runTest {
        val record = sampleRecord("REG-123")
        val savedStateHandle = SavedStateHandle(
            mapOf("licenseRecordJson" to gson.toJson(record))
        )

        val viewModel = makeViewModel(savedStateHandle)

        assertEquals("REG-123", viewModel.uiState.value.licenseRecord?.registrationNumber)
        assertEquals("", viewModel.uiState.value.selectedOfficerRemark)
        assertEquals(SaveVerificationState.Idle, viewModel.uiState.value.saveState)
    }

    @Test
    fun `initial state has null record when nav arg is missing`() = runTest {
        val viewModel = makeViewModel(SavedStateHandle())

        assertNull(viewModel.uiState.value.licenseRecord)
    }

    @Test
    fun `initial state has null record when nav arg JSON is malformed`() = runTest {
        val savedStateHandle = SavedStateHandle(
            mapOf("licenseRecordJson" to "{ this is not valid json")
        )

        val viewModel = makeViewModel(savedStateHandle)

        assertNull(viewModel.uiState.value.licenseRecord)
    }

    @Test
    fun `onOfficerRemarkSelected updates state`() = runTest {
        val viewModel = makeViewModel(SavedStateHandle())

        viewModel.onOfficerRemarkSelected("Documents verified")

        assertEquals("Documents verified", viewModel.uiState.value.selectedOfficerRemark)
    }

    @Test
    fun `saveVerification with no record emits Error without invoking the use case`() = runTest {
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
        coEvery {
            saveVerifiedLicenseUseCase(record, "Documents verified", any())
        } returns SaveVerifiedLicenseResult.Success

        val viewModel = makeViewModel(savedStateWith(record))
        viewModel.onOfficerRemarkSelected("Documents verified")

        viewModel.saveVerification()

        // With UnconfinedTestDispatcher, the launched save call resolves
        // synchronously, so the latest state is Success.
        assertEquals(SaveVerificationState.Success, viewModel.uiState.value.saveState)
        coVerify(exactly = 1) {
            saveVerifiedLicenseUseCase(record, "Documents verified", any())
        }
    }

    @Test
    fun `saveVerification propagates use-case error message`() = runTest {
        val record = sampleRecord("REG-123")
        coEvery {
            saveVerifiedLicenseUseCase(record, any(), any())
        } returns SaveVerifiedLicenseResult.Error("DB locked")

        val viewModel = makeViewModel(savedStateWith(record))
        viewModel.onOfficerRemarkSelected("Documents verified")

        viewModel.saveVerification()

        val saveState = viewModel.uiState.value.saveState
        assertTrue(saveState is SaveVerificationState.Error)
        assertEquals("DB locked", (saveState as SaveVerificationState.Error).message)
    }

    @Test
    fun `consumeSaveState resets save state to Idle`() = runTest {
        val record = sampleRecord("REG-123")
        coEvery {
            saveVerifiedLicenseUseCase(record, any(), any())
        } returns SaveVerifiedLicenseResult.Success

        val viewModel = makeViewModel(savedStateWith(record))
        viewModel.onOfficerRemarkSelected("Documents verified")
        viewModel.saveVerification()
        // sanity: we're at Success now
        assertEquals(SaveVerificationState.Success, viewModel.uiState.value.saveState)

        viewModel.consumeSaveState()

        assertEquals(SaveVerificationState.Idle, viewModel.uiState.value.saveState)
    }

    @Test
    fun `officerRemarkOptions exposes the four canonical choices`() {
        val options = VerificationFormViewModel.officerRemarkOptions

        assertEquals(4, options.size)
        assertTrue(options.all { it.isNotBlank() })
    }

    private fun makeViewModel(savedStateHandle: SavedStateHandle) =
        VerificationFormViewModel(savedStateHandle, gson, saveVerifiedLicenseUseCase)

    private fun savedStateWith(record: LicenseRecord) =
        SavedStateHandle(mapOf("licenseRecordJson" to gson.toJson(record)))

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
