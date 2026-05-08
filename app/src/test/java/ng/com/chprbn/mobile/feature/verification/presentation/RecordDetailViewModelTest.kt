package ng.com.chprbn.mobile.feature.verification.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecordResult
import ng.com.chprbn.mobile.feature.verification.domain.usecase.GetLicenseRecordUseCase
import ng.com.chprbn.mobile.feature.verification.domain.usecase.RefreshLicenseRecordUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getLicenseRecordUseCase: GetLicenseRecordUseCase
    private lateinit var refreshLicenseRecordUseCase: RefreshLicenseRecordUseCase
    private lateinit var viewModel: RecordDetailViewModel

    @Before
    fun setUp() {
        getLicenseRecordUseCase = mockk()
        refreshLicenseRecordUseCase = mockk()
        viewModel = RecordDetailViewModel(getLicenseRecordUseCase, refreshLicenseRecordUseCase)
    }

    @Test
    fun `loadRecord emits Success when use case returns the record`() = runTest {
        val record = sampleRecord("REG-123")
        coEvery { getLicenseRecordUseCase("REG-123") } returns LicenseRecordResult.Success(record)
        coEvery { refreshLicenseRecordUseCase("REG-123") } returns null

        viewModel.loadRecord("REG-123")

        val state = viewModel.state.value
        assertTrue(state is RecordDetailUiState.Success)
        assertEquals("REG-123", (state as RecordDetailUiState.Success).record.registrationNumber)
    }

    @Test
    fun `loadRecord emits NotFound when use case reports NotFound`() = runTest {
        coEvery { getLicenseRecordUseCase("REG-404") } returns LicenseRecordResult.NotFound

        viewModel.loadRecord("REG-404")

        assertEquals(RecordDetailUiState.NotFound, viewModel.state.value)
        coVerify(exactly = 0) { refreshLicenseRecordUseCase(any()) }
    }

    @Test
    fun `loadRecord emits Error when use case reports Error`() = runTest {
        coEvery { getLicenseRecordUseCase("REG-123") } returns
            LicenseRecordResult.Error("Network down")

        viewModel.loadRecord("REG-123")

        val state = viewModel.state.value
        assertTrue(state is RecordDetailUiState.Error)
        assertEquals("Network down", (state as RecordDetailUiState.Error).message)
        coVerify(exactly = 0) { refreshLicenseRecordUseCase(any()) }
    }

    @Test
    fun `silent refresh updates state when remote returns a fresher record`() = runTest {
        val cached = sampleRecord("REG-123", fullName = "Old Name")
        val refreshed = sampleRecord("REG-123", fullName = "New Name")
        coEvery { getLicenseRecordUseCase("REG-123") } returns LicenseRecordResult.Success(cached)
        coEvery { refreshLicenseRecordUseCase("REG-123") } returns refreshed

        viewModel.loadRecord("REG-123")

        val state = viewModel.state.value
        assertTrue(state is RecordDetailUiState.Success)
        assertEquals("New Name", (state as RecordDetailUiState.Success).record.fullName)
    }

    @Test
    fun `silent refresh leaves state unchanged when remote returns null`() = runTest {
        val cached = sampleRecord("REG-123")
        coEvery { getLicenseRecordUseCase("REG-123") } returns LicenseRecordResult.Success(cached)
        coEvery { refreshLicenseRecordUseCase("REG-123") } returns null

        viewModel.loadRecord("REG-123")

        val state = viewModel.state.value
        assertTrue(state is RecordDetailUiState.Success)
        assertEquals(cached, (state as RecordDetailUiState.Success).record)
    }

    @Test
    fun `retry re-invokes loadRecord with the same registration number`() = runTest {
        coEvery { getLicenseRecordUseCase("REG-123") } returns LicenseRecordResult.NotFound

        viewModel.retry("REG-123")

        coVerify(exactly = 1) { getLicenseRecordUseCase("REG-123") }
        assertEquals(RecordDetailUiState.NotFound, viewModel.state.value)
    }

    private fun sampleRecord(registrationNumber: String, fullName: String = "Jane Doe") =
        LicenseRecord(
            registrationNumber = registrationNumber,
            fullName = fullName,
            photoUrl = null,
            profession = "Pharmacist",
            licenseStatus = "Active",
            expiryDate = "2027-01-01",
            subtitle = null
        )
}
