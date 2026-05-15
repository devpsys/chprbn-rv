package ng.com.chprbn.mobile.feature.verification.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import ng.com.chprbn.mobile.core.sync.SyncJobEntity
import ng.com.chprbn.mobile.feature.verification.data.local.VerifiedLicenseDao
import ng.com.chprbn.mobile.feature.verification.data.local.VerifiedLicenseEntity
import ng.com.chprbn.mobile.feature.verification.domain.model.LicenseRecord
import ng.com.chprbn.mobile.feature.verification.domain.model.SaveVerifiedLicenseResult
import ng.com.chprbn.mobile.feature.verification.domain.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VerifiedRepositoryImplTest {

    private lateinit var verifiedLicenseDao: VerifiedLicenseDao
    private lateinit var syncJobDao: SyncJobDao
    private val now = 1_700_000_000_000L
    private val clock = Clock { now }
    private lateinit var repository: VerifiedRepositoryImpl

    @Before
    fun setUp() {
        verifiedLicenseDao = mockk(relaxed = true)
        syncJobDao = mockk(relaxed = true)
        repository = VerifiedRepositoryImpl(verifiedLicenseDao, syncJobDao, clock)
    }

    @Test
    fun `saveVerifiedLicense persists entity with Pending sync status and returns Success`() = runTest {
        val entitySlot = slot<VerifiedLicenseEntity>()
        coEvery { verifiedLicenseDao.insertOrUpdate(capture(entitySlot)) } returns 1L

        val result = repository.saveVerifiedLicense(
            licenseRecord = sampleRecord(),
            remark = "Identity confirmed",
            verifiedAt = 1_700_000_000_000L
        )

        assertEquals(SaveVerifiedLicenseResult.Success, result)
        with(entitySlot.captured) {
            assertEquals("REG-123", registrationNumber)
            assertEquals("Identity confirmed", remark)
            assertEquals(1_700_000_000_000L, verifiedAt)
            assertEquals(true, practitionerPresent)
            assertEquals(SyncStatus.Pending.name, syncStatus)
        }
    }

    @Test
    fun `saveVerifiedLicense enqueues a sync job keyed by registration number`() = runTest {
        coEvery { verifiedLicenseDao.insertOrUpdate(any()) } returns 1L
        val jobSlot = slot<SyncJobEntity>()
        coEvery { syncJobDao.enqueue(capture(jobSlot)) } returns 1L

        repository.saveVerifiedLicense(
            licenseRecord = sampleRecord(),
            remark = "Identity confirmed",
            verifiedAt = 1L,
        )

        coVerify(exactly = 1) { syncJobDao.enqueue(any()) }
        with(jobSlot.captured) {
            assertEquals(SyncEntityType.VerifiedLicense.name, entityType)
            assertEquals("REG-123", entityKey)
            assertEquals(now, enqueuedAt)
            assertEquals("Pending", status)
        }
    }

    @Test
    fun `saveVerifiedLicense returns Error when DAO throws and does not enqueue`() = runTest {
        coEvery {
            verifiedLicenseDao.insertOrUpdate(any())
        } throws RuntimeException("DB locked")

        val result = repository.saveVerifiedLicense(
            licenseRecord = sampleRecord(),
            remark = "Identity confirmed",
            verifiedAt = 1L
        )

        assertTrue(result is SaveVerifiedLicenseResult.Error)
        assertEquals("DB locked", (result as SaveVerifiedLicenseResult.Error).message)
        coVerify(exactly = 0) { syncJobDao.enqueue(any()) }
    }

    @Test
    fun `getVerifiedLicenses maps DAO entities to domain models`() = runTest {
        coEvery { verifiedLicenseDao.getAll() } returns listOf(
            sampleEntity(registrationNumber = "REG-1"),
            sampleEntity(registrationNumber = "REG-2"),
        )

        val result = repository.getVerifiedLicenses()

        assertEquals(2, result.size)
        assertEquals("REG-1", result[0].registrationNumber)
        assertEquals("REG-2", result[1].registrationNumber)
        coVerify(exactly = 1) { verifiedLicenseDao.getAll() }
    }

    @Test
    fun `getVerifiedLicenses returns empty list when DAO is empty`() = runTest {
        coEvery { verifiedLicenseDao.getAll() } returns emptyList()

        val result = repository.getVerifiedLicenses()

        assertTrue(result.isEmpty())
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

    private fun sampleEntity(registrationNumber: String) = VerifiedLicenseEntity(
        registrationNumber = registrationNumber,
        fullName = "Jane Doe",
        photoUrl = null,
        profession = "Pharmacist",
        licenseStatus = "Active",
        expiryDate = "2027-01-01",
        subtitle = null,
        verificationLocation = "",
        practitionerPresent = true,
        remark = "Identity confirmed",
        verifiedAt = 1_700_000_000_000L,
        syncStatus = SyncStatus.Pending.name
    )
}
