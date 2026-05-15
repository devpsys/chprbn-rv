package ng.com.chprbn.mobile.feature.verification.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VerifiedLicenseDaoTest {

    private lateinit var db: VerificationDatabase
    private lateinit var dao: VerifiedLicenseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, VerificationDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.verifiedLicenseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getAllReturnsRowsOrderedByVerifiedAtDescending() = runTest {
        dao.insertOrUpdate(verified("MED-1", verifiedAt = 100L))
        dao.insertOrUpdate(verified("MED-2", verifiedAt = 300L))
        dao.insertOrUpdate(verified("MED-3", verifiedAt = 200L))

        val rows = dao.getAll().map { it.registrationNumber }

        assertEquals(listOf("MED-2", "MED-3", "MED-1"), rows)
    }

    @Test
    fun insertOrUpdateReplacesOnRegistrationConflict() = runTest {
        dao.insertOrUpdate(verified("MED-1", fullName = "Original"))
        dao.insertOrUpdate(verified("MED-1", fullName = "Renamed"))

        assertEquals("Renamed", dao.getAll().single().fullName)
    }

    @Test
    fun getByLicenseStatusFiltersToTheGivenStatus() = runTest {
        dao.insertOrUpdate(verified("MED-1", licenseStatus = "Active"))
        dao.insertOrUpdate(verified("MED-2", licenseStatus = "Expired"))
        dao.insertOrUpdate(verified("MED-3", licenseStatus = "Active"))

        assertEquals(2, dao.getByLicenseStatus("Active").size)
        assertEquals(1, dao.getByLicenseStatus("Expired").size)
    }

    @Test
    fun getBySyncStatusFiltersToTheGivenStatus() = runTest {
        dao.insertOrUpdate(verified("MED-1", syncStatus = "Synced"))
        dao.insertOrUpdate(verified("MED-2", syncStatus = "Pending"))
        dao.insertOrUpdate(verified("MED-3", syncStatus = "Failed"))

        assertEquals(1, dao.getBySyncStatus("Synced").size)
        assertEquals(1, dao.getBySyncStatus("Pending").size)
        assertEquals(1, dao.getBySyncStatus("Failed").size)
    }

    @Test
    fun getPendingOrFailedUnionsBothStatuses() = runTest {
        dao.insertOrUpdate(verified("MED-1", syncStatus = "Synced"))
        dao.insertOrUpdate(verified("MED-2", syncStatus = "Pending"))
        dao.insertOrUpdate(verified("MED-3", syncStatus = "Failed"))

        val rows = dao.getPendingOrFailed()

        assertEquals(2, rows.size)
        assertTrue(rows.all { it.syncStatus != "Synced" })
    }

    @Test
    fun getFailedReturnsOnlyFailedRows() = runTest {
        dao.insertOrUpdate(verified("MED-1", syncStatus = "Synced"))
        dao.insertOrUpdate(verified("MED-2", syncStatus = "Failed"))
        dao.insertOrUpdate(verified("MED-3", syncStatus = "Pending"))

        val rows = dao.getFailed()

        assertEquals(1, rows.size)
        assertEquals("MED-2", rows.single().registrationNumber)
    }

    @Test
    fun updateSyncMetadataFlipsStatusAttemptAndError() = runTest {
        dao.insertOrUpdate(verified("MED-1", syncStatus = "Pending"))

        val updated = dao.updateSyncMetadata(
            registrationNumber = "MED-1",
            syncStatus = "Failed",
            lastSyncAttempt = 1_700_000_000_000L,
            syncError = "network down",
        )

        assertEquals(1, updated)
        val row = dao.getAll().single()
        assertEquals("Failed", row.syncStatus)
        assertEquals(1_700_000_000_000L, row.lastSyncAttempt)
        assertEquals("network down", row.syncError)
    }

    @Test
    fun updateSyncMetadataOnMissingRegistrationReturnsZero() = runTest {
        val updated = dao.updateSyncMetadata("missing", "Synced", null, null)

        assertEquals(0, updated)
    }

    private fun verified(
        registration: String,
        fullName: String = "Sarah Jenkins",
        licenseStatus: String = "Active",
        syncStatus: String = "Pending",
        verifiedAt: Long = 1_700_000_000_000L,
    ) = VerifiedLicenseEntity(
        registrationNumber = registration,
        fullName = fullName,
        photoUrl = null,
        profession = "Physician",
        certificateNo = "CERT-001",
        email = "sarah@example.com",
        phone = "08012345678",
        licenseStatus = licenseStatus,
        expiryDate = "Dec 2026",
        subtitle = "Senior Practitioner",
        issueDate = "Jan 2024",
        gender = "Female",
        graduationDate = "Jun 2010",
        institutionAttendedName = null,
        verificationLocation = "Lagos",
        practitionerPresent = true,
        remark = "Documents verified",
        verifiedAt = verifiedAt,
        syncStatus = syncStatus,
        lastSyncAttempt = null,
        syncError = null,
    )
}
