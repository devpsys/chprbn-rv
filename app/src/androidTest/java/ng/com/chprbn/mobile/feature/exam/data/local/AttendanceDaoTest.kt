package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AttendanceDaoTest {

    private lateinit var db: ExamDatabase
    private lateinit var dao: AttendanceDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, ExamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.attendanceDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertReplacesExistingRow() = runTest {
        dao.upsert(attendance(status = AttendanceStatus.SignedIn))
        dao.upsert(attendance(status = AttendanceStatus.Flagged))

        val row = dao.getOne("p1", "c1")

        assertNotNull(row)
        assertEquals("second upsert must REPLACE", AttendanceStatus.Flagged.name, row!!.status)
    }

    @Test
    fun updateSyncMetadataFlipsStatusAndKeepsAttendance() = runTest {
        dao.upsert(attendance(status = AttendanceStatus.SignedIn))

        val rows = dao.updateSyncMetadata(
            paperId = "p1",
            candidateId = "c1",
            syncStatus = SyncStatus.Synced.name,
            syncError = null,
            lastSyncAttemptAt = 9L,
        )

        assertEquals(1, rows)
        val row = dao.getOne("p1", "c1")!!
        assertEquals(SyncStatus.Synced.name, row.syncStatus)
        assertEquals(AttendanceStatus.SignedIn.name, row.status)
        assertEquals(9L, row.lastSyncAttemptAt)
        assertNull(row.syncError)
    }

    @Test
    fun pendingAndFailedFiltersBySyncStatus() = runTest {
        dao.upsert(attendance(candidateId = "c1", syncStatus = SyncStatus.Pending))
        dao.upsert(attendance(candidateId = "c2", syncStatus = SyncStatus.Synced))
        dao.upsert(attendance(candidateId = "c3", syncStatus = SyncStatus.Failed))

        val pending = dao.pendingAndFailed()

        assertEquals(2, pending.size)
        assertEquals(setOf("c1", "c3"), pending.map { it.candidateId }.toSet())
    }

    @Test
    fun countByStatusForPaperMatches() = runTest {
        dao.upsert(attendance(candidateId = "c1", status = AttendanceStatus.SignedIn))
        dao.upsert(attendance(candidateId = "c2", status = AttendanceStatus.SignedIn))
        dao.upsert(attendance(candidateId = "c3", status = AttendanceStatus.Flagged))
        dao.upsert(attendance(paperId = "p2", candidateId = "c1", status = AttendanceStatus.SignedIn))

        assertEquals(2, dao.countByStatusForPaper("p1", AttendanceStatus.SignedIn.name))
        assertEquals(1, dao.countByStatusForPaper("p1", AttendanceStatus.Flagged.name))
        assertEquals(1, dao.countByStatusForPaper("p2", AttendanceStatus.SignedIn.name))
    }

    @Test
    fun mostRecentMarkedAtReturnsMax() = runTest {
        dao.upsert(attendance(candidateId = "c1", markedAt = 100L))
        dao.upsert(attendance(candidateId = "c2", markedAt = 300L))
        dao.upsert(attendance(candidateId = "c3", markedAt = 200L))

        assertEquals(300L, dao.mostRecentMarkedAt())
    }

    @Test
    fun mostRecentMarkedAtReturnsNullWhenEmpty() = runTest {
        assertNull(dao.mostRecentMarkedAt())
    }

    private fun attendance(
        paperId: String = "p1",
        candidateId: String = "c1",
        status: AttendanceStatus = AttendanceStatus.SignedIn,
        markedAt: Long = 1_700_000_000_000L,
        syncStatus: SyncStatus = SyncStatus.Pending,
    ) = AttendanceEntity(
        paperId = paperId,
        candidateId = candidateId,
        status = status.name,
        markedAt = markedAt,
        syncStatus = syncStatus.name,
    )
}
