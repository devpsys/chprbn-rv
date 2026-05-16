package ng.com.chprbn.mobile.feature.verification.data.sync

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.verification.data.local.VerifiedLicenseDao
import ng.com.chprbn.mobile.feature.verification.data.local.VerifiedLicenseEntity
import ng.com.chprbn.mobile.feature.verification.data.source.VerifiedSyncRemoteSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class VerifiedLicenseSyncHandlerTest {

    private val now = 1_700_000_000_000L
    private val clock = Clock { now }
    private val dao = mockk<VerifiedLicenseDao>(relaxUnitFun = true) {
        coEvery { updateSyncMetadata(any(), any(), any(), any()) } returns 1
    }
    private val remote = mockk<VerifiedSyncRemoteSource>()
    private val handler = VerifiedLicenseSyncHandler(dao, remote, clock)

    @Test
    fun `missing local row produces per-key Failure without touching remote`() = runTest {
        coEvery { dao.getByRegistrationNumber("MED-1") } returns null

        val outcomes = handler.uploadBatch(listOf("MED-1"))

        assertTrue(outcomes["MED-1"] is SyncOutcome.Failure)
        coVerify(exactly = 0) { remote.uploadVerifiedBatch(any()) }
        coVerify(exactly = 0) { dao.updateSyncMetadata(any(), any(), any(), any()) }
    }

    @Test
    fun `successful upload flips row to Synced with current clock time`() = runTest {
        coEvery { dao.getByRegistrationNumber("MED-1") } returns verified("MED-1")
        coEvery { remote.uploadVerifiedBatch(any()) } returns mapOf(
            "MED-1" to Result.success(Unit),
        )

        val outcomes = handler.uploadBatch(listOf("MED-1"))

        assertEquals(SyncOutcome.Success, outcomes["MED-1"])
        coVerify(exactly = 1) {
            dao.updateSyncMetadata(
                registrationNumber = "MED-1",
                syncStatus = "Synced",
                lastSyncAttempt = now,
                syncError = null,
            )
        }
    }

    @Test
    fun `failed upload flips row to Failed with error message`() = runTest {
        coEvery { dao.getByRegistrationNumber("MED-1") } returns verified("MED-1")
        coEvery { remote.uploadVerifiedBatch(any()) } returns mapOf(
            "MED-1" to Result.failure(IOException("offline")),
        )

        val outcomes = handler.uploadBatch(listOf("MED-1"))

        assertTrue(outcomes["MED-1"] is SyncOutcome.Failure)
        assertEquals("offline", (outcomes["MED-1"] as SyncOutcome.Failure).message)
        coVerify(exactly = 1) {
            dao.updateSyncMetadata(
                registrationNumber = "MED-1",
                syncStatus = "Failed",
                lastSyncAttempt = now,
                syncError = "offline",
            )
        }
    }

    @Test
    fun `blank failure message degrades to 'Sync failed'`() = runTest {
        coEvery { dao.getByRegistrationNumber("MED-1") } returns verified("MED-1")
        coEvery { remote.uploadVerifiedBatch(any()) } returns mapOf(
            "MED-1" to Result.failure(IOException("")),
        )

        val outcomes = handler.uploadBatch(listOf("MED-1"))

        assertEquals("Sync failed", (outcomes["MED-1"] as SyncOutcome.Failure).message)
        coVerify(exactly = 1) {
            dao.updateSyncMetadata(
                registrationNumber = "MED-1",
                syncStatus = "Failed",
                lastSyncAttempt = now,
                syncError = "Sync failed",
            )
        }
    }

    @Test
    fun `batch with two rows sends a single uploadVerifiedBatch call`() = runTest {
        coEvery { dao.getByRegistrationNumber("MED-1") } returns verified("MED-1")
        coEvery { dao.getByRegistrationNumber("MED-2") } returns verified("MED-2")
        coEvery { remote.uploadVerifiedBatch(any()) } returns mapOf(
            "MED-1" to Result.success(Unit),
            "MED-2" to Result.success(Unit),
        )

        handler.uploadBatch(listOf("MED-1", "MED-2"))

        coVerify(exactly = 1) { remote.uploadVerifiedBatch(any()) }
    }

    private fun verified(registration: String) = VerifiedLicenseEntity(
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
        remark = "Documents verified",
        verifiedAt = 1_700_000_000_000L,
        syncStatus = "Pending",
        lastSyncAttempt = null,
        syncError = null,
    )
}
