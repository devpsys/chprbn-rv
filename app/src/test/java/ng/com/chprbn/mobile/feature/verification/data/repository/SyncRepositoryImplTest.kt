package ng.com.chprbn.mobile.feature.verification.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult as CoreSyncBatchResult
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncBatchRunner
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import ng.com.chprbn.mobile.core.sync.SyncJobEntity
import ng.com.chprbn.mobile.feature.verification.data.local.VerifiedLicenseDao
import ng.com.chprbn.mobile.feature.verification.data.local.VerifiedLicenseEntity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Verifies the refactored repo's contract: backfill any verified rows that
 * are Pending/Failed but missing from the outbox, then delegate to the
 * canonical [SyncBatchRunner]. The legacy direct-POST loop is gone.
 */
class SyncRepositoryImplTest {

    private val now = 1_700_000_000_000L
    private val clock = Clock { now }
    private lateinit var verifiedLicenseDao: VerifiedLicenseDao
    private lateinit var syncJobDao: SyncJobDao
    private lateinit var batchRunner: SyncBatchRunner
    private lateinit var repository: SyncRepositoryImpl

    @Before
    fun setUp() {
        verifiedLicenseDao = mockk(relaxed = true)
        syncJobDao = mockk(relaxed = true)
        batchRunner = mockk(relaxed = true)
        coEvery { batchRunner.runBatch(any()) } returns CoreSyncBatchResult.Empty
        repository = SyncRepositoryImpl(verifiedLicenseDao, syncJobDao, batchRunner, clock)
    }

    @Test
    fun `syncAllPendingAndFailed backfills each pending or failed row into the queue`() = runTest {
        coEvery { verifiedLicenseDao.getPendingOrFailed() } returns listOf(
            verified("MED-1", status = "Pending"),
            verified("MED-2", status = "Failed"),
        )
        val captured = mutableListOf<SyncJobEntity>()
        coEvery { syncJobDao.enqueue(capture(captured)) } returns 1L

        repository.syncAllPendingAndFailed()

        assertEquals(2, captured.size)
        assertEquals(listOf("MED-1", "MED-2"), captured.map { it.entityKey })
        assertEquals(
            listOf(SyncEntityType.VerifiedLicense.name, SyncEntityType.VerifiedLicense.name),
            captured.map { it.entityType },
        )
        coVerify(exactly = 1) { batchRunner.runBatch(any()) }
    }

    @Test
    fun `retryFailed backfills only failed rows`() = runTest {
        coEvery { verifiedLicenseDao.getFailed() } returns listOf(verified("MED-9", status = "Failed"))
        val jobSlot = slot<SyncJobEntity>()
        coEvery { syncJobDao.enqueue(capture(jobSlot)) } returns 1L

        repository.retryFailed()

        assertEquals("MED-9", jobSlot.captured.entityKey)
        coVerify(exactly = 1) { verifiedLicenseDao.getFailed() }
        coVerify(exactly = 0) { verifiedLicenseDao.getPendingOrFailed() }
        coVerify(exactly = 1) { batchRunner.runBatch(any()) }
    }

    @Test
    fun `empty pending list still calls the batch runner`() = runTest {
        coEvery { verifiedLicenseDao.getPendingOrFailed() } returns emptyList()

        repository.syncAllPendingAndFailed()

        coVerify(exactly = 0) { syncJobDao.enqueue(any()) }
        coVerify(exactly = 1) { batchRunner.runBatch(any()) }
    }

    @Test
    fun `core batch result is forwarded as the feature-side result`() = runTest {
        coEvery { verifiedLicenseDao.getPendingOrFailed() } returns emptyList()
        coEvery { batchRunner.runBatch(any()) } returns CoreSyncBatchResult(
            attempted = 3,
            succeeded = 2,
            failed = 1,
            errors = listOf("MED-3: offline"),
        )

        val result = repository.syncAllPendingAndFailed()

        assertEquals(3, result.attempted)
        assertEquals(2, result.succeeded)
        assertEquals(1, result.failed)
        assertEquals(listOf("MED-3: offline"), result.errors)
    }

    @Test
    fun `getSyncRecords maps DAO rows to domain SyncRecords`() = runTest {
        coEvery { verifiedLicenseDao.getAll() } returns listOf(verified("MED-1"))

        val rows = repository.getSyncRecords()

        assertEquals(1, rows.size)
        assertEquals("MED-1", rows.single().registrationNumber)
    }

    private fun verified(registration: String, status: String = "Pending") = VerifiedLicenseEntity(
        registrationNumber = registration,
        fullName = "Sarah Jenkins",
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
        institutionAttendedName = null,
        verificationLocation = "Lagos",
        practitionerPresent = true,
        remark = "Active",
        verifiedAt = 1_700_000_000_000L,
        syncStatus = status,
        lastSyncAttempt = null,
        syncError = null,
    )
}
