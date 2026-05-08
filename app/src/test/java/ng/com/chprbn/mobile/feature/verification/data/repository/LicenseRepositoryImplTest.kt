package ng.com.chprbn.mobile.feature.verification.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.verification.data.local.LicenseRecordDao
import ng.com.chprbn.mobile.feature.verification.data.local.LicenseRecordEntity
import ng.com.chprbn.mobile.feature.verification.data.source.LicenseRecordRemoteSource
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecordResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class LicenseRepositoryImplTest {

    private lateinit var licenseRecordDao: LicenseRecordDao
    private lateinit var remoteSource: LicenseRecordRemoteSource
    private lateinit var repository: LicenseRepositoryImpl

    @Before
    fun setUp() {
        licenseRecordDao = mockk(relaxed = true)
        remoteSource = mockk()
        repository = LicenseRepositoryImpl(licenseRecordDao, remoteSource)
    }

    @Test
    fun `getLicenseRecord returns NotFound when input is empty`() = runTest {
        val result = repository.getLicenseRecord("")

        assertEquals(LicenseRecordResult.NotFound, result)
        coVerify(exactly = 0) { remoteSource.getLicenseRecord(any()) }
        verify(exactly = 0) { licenseRecordDao.getByRegistrationNumber(any()) }
    }

    @Test
    fun `getLicenseRecord returns NotFound when input is only whitespace`() = runTest {
        val result = repository.getLicenseRecord("   ")

        assertEquals(LicenseRecordResult.NotFound, result)
        coVerify(exactly = 0) { remoteSource.getLicenseRecord(any()) }
    }

    @Test
    fun `getLicenseRecord returns cached record without hitting remote`() = runTest {
        every { licenseRecordDao.getByRegistrationNumber("REG-123") } returns sampleEntity()

        val result = repository.getLicenseRecord("REG-123")

        assertTrue(result is LicenseRecordResult.Success)
        assertEquals("REG-123", (result as LicenseRecordResult.Success).record.registrationNumber)
        coVerify(exactly = 0) { remoteSource.getLicenseRecord(any()) }
    }

    @Test
    fun `getLicenseRecord trims input before querying cache`() = runTest {
        every { licenseRecordDao.getByRegistrationNumber("REG-123") } returns sampleEntity()

        repository.getLicenseRecord("  REG-123  ")

        verify(exactly = 1) { licenseRecordDao.getByRegistrationNumber("REG-123") }
    }

    @Test
    fun `getLicenseRecord caches and returns remote record on cache miss`() = runTest {
        every { licenseRecordDao.getByRegistrationNumber("REG-123") } returns null
        coEvery { remoteSource.getLicenseRecord("REG-123") } returns sampleRecord("REG-123")

        val result = repository.getLicenseRecord("REG-123")

        assertTrue(result is LicenseRecordResult.Success)
        verify(exactly = 1) { licenseRecordDao.insertOrUpdate(any()) }
    }

    @Test
    fun `getLicenseRecord returns NotFound when remote returns null`() = runTest {
        every { licenseRecordDao.getByRegistrationNumber("REG-404") } returns null
        coEvery { remoteSource.getLicenseRecord("REG-404") } returns null

        val result = repository.getLicenseRecord("REG-404")

        assertEquals(LicenseRecordResult.NotFound, result)
        verify(exactly = 0) { licenseRecordDao.insertOrUpdate(any()) }
    }

    @Test
    fun `getLicenseRecord maps IOException to a network-error message`() = runTest {
        every { licenseRecordDao.getByRegistrationNumber("REG-123") } returns null
        coEvery { remoteSource.getLicenseRecord("REG-123") } throws IOException("DNS failure")

        val result = repository.getLicenseRecord("REG-123")

        assertTrue(result is LicenseRecordResult.Error)
        assertEquals(
            "Network error. Please check your connection.",
            (result as LicenseRecordResult.Error).message
        )
        verify(exactly = 0) { licenseRecordDao.insertOrUpdate(any()) }
    }

    @Test
    fun `getLicenseRecord propagates non-IO exception message in Error`() = runTest {
        every { licenseRecordDao.getByRegistrationNumber("REG-123") } returns null
        coEvery {
            remoteSource.getLicenseRecord("REG-123")
        } throws RuntimeException("Unexpected JSON shape")

        val result = repository.getLicenseRecord("REG-123")

        assertTrue(result is LicenseRecordResult.Error)
        assertEquals(
            "Unexpected JSON shape",
            (result as LicenseRecordResult.Error).message
        )
    }

    @Test
    fun `refreshLicenseRecord returns null and skips cache write for empty input`() = runTest {
        val result = repository.refreshLicenseRecord("   ")

        assertEquals(null, result)
        coVerify(exactly = 0) { remoteSource.getLicenseRecord(any()) }
        verify(exactly = 0) { licenseRecordDao.insertOrUpdate(any()) }
    }

    @Test
    fun `refreshLicenseRecord caches and returns the remote record on success`() = runTest {
        coEvery { remoteSource.getLicenseRecord("REG-123") } returns sampleRecord("REG-123")

        val result = repository.refreshLicenseRecord("REG-123")

        assertEquals("REG-123", result?.registrationNumber)
        verify(exactly = 1) { licenseRecordDao.insertOrUpdate(any()) }
    }

    @Test
    fun `refreshLicenseRecord returns null and skips cache write when remote returns null`() = runTest {
        coEvery { remoteSource.getLicenseRecord("REG-404") } returns null

        val result = repository.refreshLicenseRecord("REG-404")

        assertEquals(null, result)
        verify(exactly = 0) { licenseRecordDao.insertOrUpdate(any()) }
    }

    @Test
    fun `refreshLicenseRecord swallows IOException and leaves the cache untouched`() = runTest {
        coEvery {
            remoteSource.getLicenseRecord("REG-123")
        } throws IOException("DNS failure")

        val result = repository.refreshLicenseRecord("REG-123")

        assertEquals(null, result)
        verify(exactly = 0) { licenseRecordDao.insertOrUpdate(any()) }
    }

    @Test
    fun `refreshLicenseRecord swallows non-IO exceptions too`() = runTest {
        coEvery {
            remoteSource.getLicenseRecord("REG-123")
        } throws RuntimeException("Unexpected JSON shape")

        val result = repository.refreshLicenseRecord("REG-123")

        assertEquals(null, result)
        verify(exactly = 0) { licenseRecordDao.insertOrUpdate(any()) }
    }

    private fun sampleEntity() = LicenseRecordEntity(
        registrationNumber = "REG-123",
        fullName = "Jane Doe",
        photoUrl = null,
        profession = "Pharmacist",
        licenseStatus = "Active",
        expiryDate = "2027-01-01",
        subtitle = null
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
