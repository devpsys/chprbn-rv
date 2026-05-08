package ng.com.chprbn.mobile.feature.verification.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.verification.domain.model.SaveVerifiedLicenseResult
import ng.com.chprbn.mobile.feature.verification.domain.repository.VerifiedRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveVerifiedLicenseUseCaseTest {

    private lateinit var verifiedRepository: VerifiedRepository
    private lateinit var useCase: SaveVerifiedLicenseUseCase

    @Before
    fun setUp() {
        verifiedRepository = mockk()
        useCase = SaveVerifiedLicenseUseCase(verifiedRepository)
    }

    @Test
    fun `invoke returns Error when remark is empty`() = runTest {
        val result = useCase(sampleRecord(), remark = "", verifiedAt = 1L)

        assertTrue(result is SaveVerifiedLicenseResult.Error)
        assertEquals(
            "Please select an officer remark.",
            (result as SaveVerifiedLicenseResult.Error).message
        )
        coVerify(exactly = 0) { verifiedRepository.saveVerifiedLicense(any(), any(), any()) }
    }

    @Test
    fun `invoke returns Error when remark is only whitespace`() = runTest {
        val result = useCase(sampleRecord(), remark = "   ", verifiedAt = 1L)

        assertTrue(result is SaveVerifiedLicenseResult.Error)
        coVerify(exactly = 0) { verifiedRepository.saveVerifiedLicense(any(), any(), any()) }
    }

    @Test
    fun `invoke trims remark and delegates with provided verifiedAt`() = runTest {
        val record = sampleRecord()
        val remarkSlot = slot<String>()
        coEvery {
            verifiedRepository.saveVerifiedLicense(
                licenseRecord = record,
                remark = capture(remarkSlot),
                verifiedAt = 12345L
            )
        } returns SaveVerifiedLicenseResult.Success

        val result = useCase(record, remark = "  Identity confirmed  ", verifiedAt = 12345L)

        assertEquals(SaveVerifiedLicenseResult.Success, result)
        assertEquals("Identity confirmed", remarkSlot.captured)
    }

    @Test
    fun `invoke uses System currentTimeMillis when verifiedAt is omitted`() = runTest {
        val record = sampleRecord()
        val verifiedAtSlot = slot<Long>()
        coEvery {
            verifiedRepository.saveVerifiedLicense(
                licenseRecord = record,
                remark = "Identity confirmed",
                verifiedAt = capture(verifiedAtSlot)
            )
        } returns SaveVerifiedLicenseResult.Success

        val before = System.currentTimeMillis()
        useCase(record, remark = "Identity confirmed")
        val after = System.currentTimeMillis()

        // The default arg evaluates System.currentTimeMillis() at call-site,
        // which must fall within the bracketing window we measured.
        assertTrue(verifiedAtSlot.captured in before..after)
    }

    private fun sampleRecord() = LicenseRecord(
        registrationNumber = "REG-123",
        fullName = "Jane Doe",
        photoUrl = null,
        profession = "Pharmacist",
        licenseStatus = "Active",
        expiryDate = "2027-01-01",
        subtitle = null
    )
}
