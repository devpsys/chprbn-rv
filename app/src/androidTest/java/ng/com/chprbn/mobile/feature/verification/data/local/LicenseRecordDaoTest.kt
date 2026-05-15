package ng.com.chprbn.mobile.feature.verification.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LicenseRecordDaoTest {

    private lateinit var db: VerificationDatabase
    private lateinit var dao: LicenseRecordDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, VerificationDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.licenseRecordDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertOrUpdateThenReadByRegistrationNumber() {
        dao.insertOrUpdate(record(registration = "MED-12345", fullName = "Sarah Jenkins"))

        val row = dao.getByRegistrationNumber("MED-12345")

        assertNotNull(row)
        assertEquals("Sarah Jenkins", row!!.fullName)
    }

    @Test
    fun insertOrUpdateReplacesOnRegistrationConflict() {
        dao.insertOrUpdate(record(registration = "MED-12345", fullName = "Original"))
        dao.insertOrUpdate(record(registration = "MED-12345", fullName = "Renamed"))

        assertEquals("Renamed", dao.getByRegistrationNumber("MED-12345")?.fullName)
    }

    @Test
    fun getByRegistrationNumberReturnsNullWhenAbsent() {
        assertNull(dao.getByRegistrationNumber("missing"))
    }

    @Test
    fun multipleRecordsCoexistKeyedByRegistration() {
        dao.insertOrUpdate(record(registration = "MED-1", fullName = "One"))
        dao.insertOrUpdate(record(registration = "MED-2", fullName = "Two"))

        assertEquals("One", dao.getByRegistrationNumber("MED-1")?.fullName)
        assertEquals("Two", dao.getByRegistrationNumber("MED-2")?.fullName)
    }

    private fun record(
        registration: String,
        fullName: String = "Sarah Jenkins",
    ) = LicenseRecordEntity(
        registrationNumber = registration,
        fullName = fullName,
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
    )
}
