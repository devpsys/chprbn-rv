package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises [CandidateDao.rowsForPaper] — the heavy join driving the
 * Exam Candidates screen. Covers:
 *
 * - LEFT JOIN behaviour: candidates without attendance rows still appear
 *   when the filter is `All`.
 * - The `attendanceFilter` parameter narrowing by explicit status.
 * - The free-text filter over fullName / examNumber.
 * - The remark-count subquery.
 */
@RunWith(AndroidJUnit4::class)
class CandidateDaoTest {

    private lateinit var db: ExamDatabase
    private lateinit var candidateDao: CandidateDao
    private lateinit var attendanceDao: AttendanceDao
    private lateinit var remarkDao: RemarkDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, ExamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        candidateDao = db.candidateDao()
        attendanceDao = db.attendanceDao()
        remarkDao = db.remarkDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun rowsForPaperIncludesCandidatesWithoutAttendance() = runTest {
        seedAssignments()

        val rows = candidateDao.rowsForPaper("p1", "All", "")

        assertEquals(3, rows.size)
        // All candidates are assigned but none have attendance rows, so
        // attendanceStatus is null for every row.
        assertTrue(rows.all { it.attendanceStatus == null })
    }

    @Test
    fun rowsForPaperFiltersByAttendanceStatus() = runTest {
        seedAssignments()
        attendanceDao.upsert(attendanceFor("c1", AttendanceStatus.SignedIn))
        attendanceDao.upsert(attendanceFor("c2", AttendanceStatus.Flagged))

        val signedIn = candidateDao.rowsForPaper("p1", AttendanceStatus.SignedIn.name, "")
        val flagged = candidateDao.rowsForPaper("p1", AttendanceStatus.Flagged.name, "")
        val all = candidateDao.rowsForPaper("p1", "All", "")

        assertEquals(1, signedIn.size)
        assertEquals("c1", signedIn.single().candidateId)
        assertEquals(1, flagged.size)
        assertEquals("c2", flagged.single().candidateId)
        assertEquals(3, all.size)
    }

    @Test
    fun rowsForPaperFiltersByFreeTextQuery() = runTest {
        seedAssignments()

        val alice = candidateDao.rowsForPaper("p1", "All", "%Alice%")
        val bob = candidateDao.rowsForPaper("p1", "All", "%Bob%")
        val examNumberHit = candidateDao.rowsForPaper("p1", "All", "%EX-002%")

        assertEquals(2, alice.size)
        assertTrue(alice.all { it.fullName.contains("Alice") })
        assertEquals(1, bob.size)
        assertEquals("Bob Jones", bob.single().fullName)
        assertEquals(1, examNumberHit.size)
        assertEquals("c2", examNumberHit.single().candidateId)
    }

    @Test
    fun rowsForPaperReportsRemarkCount() = runTest {
        seedAssignments()
        remarkDao.upsert(remarkFor("r1", "c1"))
        remarkDao.upsert(remarkFor("r2", "c1"))
        remarkDao.upsert(remarkFor("r3", "c2"))

        val rows = candidateDao.rowsForPaper("p1", "All", "").associateBy { it.candidateId }

        assertEquals(2, rows.getValue("c1").remarkCount)
        assertEquals(1, rows.getValue("c2").remarkCount)
        assertEquals(0, rows.getValue("c3").remarkCount)
    }

    @Test
    fun rowsForPaperReturnsNullAttendanceFieldsViaLeftJoin() = runTest {
        seedAssignments()

        val rows = candidateDao.rowsForPaper("p1", "All", "")
        val sample = rows.first()
        assertNull(sample.attendanceStatus)
        assertNull(sample.attendanceMarkedAt)
        assertNull(sample.attendanceSyncStatus)
    }

    private suspend fun seedAssignments() {
        candidateDao.upsertAll(
            listOf(
                CandidateEntity("c1", "EX-001", "Alice Smith"),
                CandidateEntity("c2", "EX-002", "Bob Jones"),
                CandidateEntity("c3", "EX-003", "Alice Anderson"),
            ),
        )
        candidateDao.upsertAssignments(
            listOf(
                PaperCandidateAssignmentEntity("p1", "c1"),
                PaperCandidateAssignmentEntity("p1", "c2"),
                PaperCandidateAssignmentEntity("p1", "c3"),
            ),
        )
    }

    private fun attendanceFor(candidateId: String, status: AttendanceStatus) = AttendanceEntity(
        paperId = "p1",
        candidateId = candidateId,
        status = status.name,
        markedAt = 1_700_000_000_000L,
        syncStatus = SyncStatus.Pending.name,
    )

    private fun remarkFor(id: String, candidateId: String) = RemarkEntity(
        id = id,
        candidateId = candidateId,
        paperId = "p1",
        body = "x",
        severity = "Info",
        createdAt = 1_700_000_000_000L,
        syncStatus = SyncStatus.Pending.name,
    )
}
