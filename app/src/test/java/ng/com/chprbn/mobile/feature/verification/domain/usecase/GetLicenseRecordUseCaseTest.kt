package ng.com.chprbn.mobile.feature.verification.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecordResult
import ng.com.chprbn.mobile.feature.verification.domain.repository.LicenseRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetLicenseRecordUseCaseTest {

    private lateinit var licenseRepository: LicenseRepository
    private lateinit var useCase: GetLicenseRecordUseCase

    @Before
    fun setUp() {
        licenseRepository = mockk()
        useCase = GetLicenseRecordUseCase(licenseRepository)
    }

    @Test
    fun `invoke returns Error when input is empty`() = runTest {
        val result = useCase("")

        assertTrue(result is LicenseRecordResult.Error)
        assertEquals("License number is required.", (result as LicenseRecordResult.Error).message)
        coVerify(exactly = 0) { licenseRepository.getLicenseRecord(any()) }
    }

    @Test
    fun `invoke returns Error when input is only whitespace`() = runTest {
        val result = useCase("   ")

        assertTrue(result is LicenseRecordResult.Error)
        coVerify(exactly = 0) { licenseRepository.getLicenseRecord(any()) }
    }

    @Test
    fun `invoke trims input and delegates to repository`() = runTest {
        val record = sampleRecord(registrationNumber = "REG-123")
        coEvery { licenseRepository.getLicenseRecord("REG-123") } returns
            LicenseRecordResult.Success(record)

        val result = useCase("  REG-123  ")

        assertEquals(LicenseRecordResult.Success(record), result)
        coVerify(exactly = 1) { licenseRepository.getLicenseRecord("REG-123") }
    }

    @Test
    fun `invoke propagates NotFound from repository`() = runTest {
        coEvery { licenseRepository.getLicenseRecord("REG-404") } returns LicenseRecordResult.NotFound

        val result = useCase("REG-404")

        assertEquals(LicenseRecordResult.NotFound, result)
    }

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
