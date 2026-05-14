package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the `rowsForSchedule` aggregation query — the heaviest piece of
 * SQL in the assessment feature. Covers:
 *
 * - the SUM-based [aggregateScore] across practical + project rows,
 * - the [scoredQuestions] / [totalQuestions] derivation from `section_questions`,
 * - the `CASE WHEN` syncStatus priority `Failed > Pending > Synced`,
 * - the free-text filter parameter.
 *
 * Stays in androidTest because it uses a real Room database (SQLite).
 */
@RunWith(AndroidJUnit4::class)
class AssessmentCandidateDaoTest {

    private lateinit var db: AssessmentDatabase
    private lateinit var candidateDao: AssessmentCandidateDao
    private lateinit var sectionDao: PracticalSectionDao
    private lateinit var questionDao: SectionQuestionDao
    private lateinit var practicalDao: PracticalScoreDao
    private lateinit var projectDao: ProjectScoreDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AssessmentDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        candidateDao = db.candidateDao()
        sectionDao = db.sectionDao()
        questionDao = db.questionDao()
        practicalDao = db.practicalScoreDao()
        projectDao = db.projectScoreDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun aggregateScoreSumsPracticalAndProject() = runTest {
        seedScheduleS1()
        candidateDao.upsertAll(listOf(candidate("c1", "EX-1", "Jane Doe")))
        candidateDao.upsertAssignments(listOf(assignment("s1", "c1")))
        practicalDao.upsert(practical("c1", "q1", score = 5))
        practicalDao.upsert(practical("c1", "q2", score = 3))
        projectDao.upsert(project("c1", score = 7.6, status = SyncStatus.Pending))

        val rows = candidateDao.rowsForSchedule("s1", "")

        assertEquals(1, rows.size)
        // 5 + 3 + ROUND(7.6) = 5 + 3 + 8 = 16
        assertEquals(16, rows.single().aggregateScore)
        assertEquals(2, rows.single().scoredQuestions)
        assertEquals(2, rows.single().totalQuestions)
    }

    @Test
    fun syncStatusFollowsFailedOverPendingOverSynced() = runTest {
        seedScheduleS1()
        candidateDao.upsertAll(
            listOf(
                candidate("c1", "EX-1", "Failing Candidate"),
                candidate("c2", "EX-2", "Pending Candidate"),
                candidate("c3", "EX-3", "Synced Candidate"),
            ),
        )
        candidateDao.upsertAssignments(
            listOf(
                assignment("s1", "c1"),
                assignment("s1", "c2"),
                assignment("s1", "c3"),
            ),
        )
        // c1: one Synced + one Failed → Failed
        practicalDao.upsert(practical("c1", "q1", score = 5, status = SyncStatus.Synced))
        practicalDao.upsert(practical("c1", "q2", score = 3, status = SyncStatus.Failed))
        // c2: one Synced + one Pending → Pending
        practicalDao.upsert(practical("c2", "q1", score = 5, status = SyncStatus.Synced))
        practicalDao.upsert(practical("c2", "q2", score = 3, status = SyncStatus.Pending))
        // c3: both Synced → Synced
        practicalDao.upsert(practical("c3", "q1", score = 5, status = SyncStatus.Synced))
        practicalDao.upsert(practical("c3", "q2", score = 3, status = SyncStatus.Synced))

        val rows = candidateDao.rowsForSchedule("s1", "")
            .associateBy { it.candidateId }

        assertEquals(SyncStatus.Failed.name, rows.getValue("c1").syncStatus)
        assertEquals(SyncStatus.Pending.name, rows.getValue("c2").syncStatus)
        assertEquals(SyncStatus.Synced.name, rows.getValue("c3").syncStatus)
    }

    @Test
    fun queryFiltersByExamNumberOrFullName() = runTest {
        seedScheduleS1()
        candidateDao.upsertAll(
            listOf(
                candidate("c1", "EX-001", "Alice Smith"),
                candidate("c2", "EX-002", "Bob Jones"),
                candidate("c3", "EX-003", "Alice Anderson"),
            ),
        )
        candidateDao.upsertAssignments(
            listOf(assignment("s1", "c1"), assignment("s1", "c2"), assignment("s1", "c3")),
        )

        // Empty filter returns all
        assertEquals(3, candidateDao.rowsForSchedule("s1", "").size)
        // Name filter
        val alices = candidateDao.rowsForSchedule("s1", "%Alice%")
        assertEquals(2, alices.size)
        assertTrue(alices.all { it.fullName.contains("Alice") })
        // Exam number filter
        val bob = candidateDao.rowsForSchedule("s1", "%EX-002%")
        assertEquals(1, bob.size)
        assertEquals("Bob Jones", bob.single().fullName)
    }

    @Test
    fun unscoredCandidateHasZeroAggregateAndSyncedStatus() = runTest {
        seedScheduleS1()
        candidateDao.upsertAll(listOf(candidate("c1", "EX-1", "Untouched")))
        candidateDao.upsertAssignments(listOf(assignment("s1", "c1")))

        val row = candidateDao.rowsForSchedule("s1", "").single()

        assertEquals(0, row.aggregateScore)
        assertEquals(0, row.scoredQuestions)
        // Vacuous "no failed, no pending" → Synced; the screen suppresses the
        // pill anyway via scoredQuestions == 0.
        assertEquals(SyncStatus.Synced.name, row.syncStatus)
    }

    // Seeds the schedule, one section, and two questions so the
    // `totalQuestions` aggregation has rows to count.
    private suspend fun seedScheduleS1() {
        sectionDao.upsertAll(
            listOf(
                PracticalSectionEntity(
                    id = "sec1",
                    scheduleId = "s1",
                    title = "A",
                    subtitle = "Vitals",
                    ordering = 1,
                ),
            ),
        )
        questionDao.upsertAll(
            listOf(
                SectionQuestionEntity("q1", "sec1", 1, "Take BP", null, 10),
                SectionQuestionEntity("q2", "sec1", 2, "Take pulse", null, 10),
            ),
        )
    }

    private fun candidate(id: String, examNumber: String, fullName: String) =
        AssessmentCandidateEntity(id, examNumber, fullName, photoUrl = null)

    private fun assignment(scheduleId: String, candidateId: String) =
        ScheduleCandidateAssignmentEntity(scheduleId, candidateId)

    private fun practical(
        candidateId: String,
        questionId: String,
        score: Int,
        status: SyncStatus = SyncStatus.Pending,
    ) = PracticalScoreEntity(
        scheduleId = "s1",
        candidateId = candidateId,
        questionId = questionId,
        score = score,
        scoredAt = 0L,
        syncStatus = status.name,
    )

    private fun project(
        candidateId: String,
        score: Double,
        status: SyncStatus = SyncStatus.Synced,
    ) = ProjectScoreEntity(
        scheduleId = "s1",
        candidateId = candidateId,
        score = score,
        maxScore = 10,
        scoredAt = 0L,
        syncStatus = status.name,
    )
}
