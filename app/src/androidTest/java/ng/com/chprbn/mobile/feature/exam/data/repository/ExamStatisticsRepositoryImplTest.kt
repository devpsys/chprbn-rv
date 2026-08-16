package ng.com.chprbn.mobile.feature.exam.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentDatabase
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreEntity
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreEntity
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceEntity
import ng.com.chprbn.mobile.feature.exam.data.local.CandidateEntity
import ng.com.chprbn.mobile.feature.exam.data.local.CenterEntity
import ng.com.chprbn.mobile.feature.exam.data.local.ExamDatabase
import ng.com.chprbn.mobile.feature.exam.data.local.PaperCandidateAssignmentEntity
import ng.com.chprbn.mobile.feature.exam.data.local.PaperEntity
import ng.com.chprbn.mobile.feature.exam.data.local.RemarkEntity
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity
import ng.com.chprbn.mobile.feature.exam.domain.model.SaveResult
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Real Room-backed test of [ExamStatisticsRepositoryImpl]. Uses an
 * in-memory database because `clearLocalCache` runs through
 * `db.withTransaction { … }`, which is awkward to mock.
 */
@RunWith(AndroidJUnit4::class)
class ExamStatisticsRepositoryImplTest {

    private lateinit var db: ExamDatabase
    private lateinit var assessmentDb: AssessmentDatabase
    private lateinit var repository: ExamStatisticsRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ExamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        assessmentDb = Room.inMemoryDatabaseBuilder(context, AssessmentDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ExamStatisticsRepositoryImpl(
            db = db,
            assessmentDb = assessmentDb,
            centerDao = db.centerDao(),
            paperDao = db.paperDao(),
            candidateDao = db.candidateDao(),
            attendanceDao = db.attendanceDao(),
            remarkDao = db.remarkDao(),
            practicalScoreDao = assessmentDb.practicalScoreDao(),
            projectScoreDao = assessmentDb.projectScoreDao(),
        )
    }

    @After
    fun tearDown() {
        db.close()
        assessmentDb.close()
    }

    @Test
    fun getStatisticsAggregatesAcrossSyncBuckets() = runTest {
        db.attendanceDao().upsert(attendance("c1", SyncStatus.Synced, markedAt = 100L))
        db.attendanceDao().upsert(attendance("c2", SyncStatus.Pending, markedAt = 300L))
        db.attendanceDao().upsert(attendance("c3", SyncStatus.Failed, markedAt = 200L))

        val stats = repository.getStatistics()

        assertEquals(3, stats.attendanceCaptured)
        assertEquals(0, stats.practicalCaptured)
        assertEquals(0, stats.projectCaptured)
        assertEquals(1, stats.syncedCount)
        assertEquals(3, stats.cachedCount)
        assertEquals(1, stats.pendingCount)
        assertEquals(1, stats.failedCount)
        assertEquals(300L, stats.lastUpdatedAt)
    }

    @Test
    fun getStatisticsFoldsPracticalAndProjectIntoSyncBuckets() = runTest {
        db.attendanceDao().upsert(attendance("c1", SyncStatus.Synced, markedAt = 100L))
        assessmentDb.practicalScoreDao().upsert(
            practical(candidateId = "c1", questionId = "q1", status = SyncStatus.Pending, scoredAt = 400L),
        )
        assessmentDb.practicalScoreDao().upsert(
            practical(candidateId = "c1", questionId = "q2", status = SyncStatus.Synced, scoredAt = 150L),
        )
        assessmentDb.projectScoreDao().upsert(
            project(candidateId = "c2", status = SyncStatus.Failed, scoredAt = 250L),
        )

        val stats = repository.getStatistics()

        assertEquals(1, stats.attendanceCaptured)
        assertEquals(1, stats.practicalCaptured)
        assertEquals(1, stats.projectCaptured)
        assertEquals(2, stats.syncedCount)
        assertEquals(1, stats.pendingCount)
        assertEquals(1, stats.failedCount)
        assertEquals(4, stats.cachedCount)
        assertEquals(400L, stats.lastUpdatedAt)
    }

    @Test
    fun practicalCapturedCountsDistinctCandidatesNotQuestionRows() = runTest {
        assessmentDb.practicalScoreDao().upsert(
            practical(candidateId = "c1", questionId = "q1", status = SyncStatus.Pending, scoredAt = 1L),
        )
        assessmentDb.practicalScoreDao().upsert(
            practical(candidateId = "c1", questionId = "q2", status = SyncStatus.Synced, scoredAt = 2L),
        )
        assessmentDb.practicalScoreDao().upsert(
            practical(candidateId = "c2", questionId = "q1", status = SyncStatus.Pending, scoredAt = 3L),
        )

        val stats = repository.getStatistics()

        assertEquals(2, stats.practicalCaptured)
        assertEquals(3, stats.cachedCount)
    }

    @Test
    fun getStatisticsReturnsZeroesWhenTableEmpty() = runTest {
        val stats = repository.getStatistics()

        assertEquals(0, stats.recordsDownloaded)
        assertEquals(0, stats.attendanceCaptured)
        assertEquals(0, stats.practicalCaptured)
        assertEquals(0, stats.projectCaptured)
        assertEquals(0, stats.syncedCount)
        assertEquals(0, stats.pendingCount)
        assertEquals(0, stats.failedCount)
        assertNull(stats.lastUpdatedAt)
    }

    @Test
    fun recordsDownloadedReflectsAssignmentCountNotAttendanceCount() = runTest {
        // Seed 4 assignments (= 4 records downloaded) but only 1 attendance row.
        // The proxy used to conflate these; the real DAO query keeps them
        // independent so the dashboard "X of N downloaded records have been
        // checked in" math stays meaningful before the officer starts marking.
        db.candidateDao().upsertAll(
            listOf(
                CandidateEntity("c1", "EX1", "Ada"),
                CandidateEntity("c2", "EX2", "Bola"),
                CandidateEntity("c3", "EX3", "Chika"),
                CandidateEntity("c4", "EX4", "Dele"),
            ),
        )
        db.candidateDao().upsertAssignments(
            listOf(
                PaperCandidateAssignmentEntity("p1", "c1"),
                PaperCandidateAssignmentEntity("p1", "c2"),
                PaperCandidateAssignmentEntity("p1", "c3"),
                PaperCandidateAssignmentEntity("p1", "c4"),
            ),
        )
        db.attendanceDao().upsert(attendance("c1", SyncStatus.Pending))

        val stats = repository.getStatistics()

        assertEquals(4, stats.recordsDownloaded)
        assertEquals(1, stats.attendanceCaptured)
    }

    @Test
    fun clearLocalCacheWipesEveryTable() = runTest {
        db.centerDao().upsert(center())
        db.paperDao().upsertAll(listOf(paper()))
        db.candidateDao().upsertAll(listOf(candidate()))
        db.candidateDao().upsertAssignments(
            listOf(PaperCandidateAssignmentEntity(paperId = "p1", candidateId = "c1")),
        )
        db.attendanceDao().upsert(attendance("c1"))
        db.remarkDao().upsert(remark())
        assessmentDb.practicalScoreDao().upsert(
            practical(candidateId = "c1", questionId = "q1", status = SyncStatus.Pending, scoredAt = 1L),
        )
        assessmentDb.projectScoreDao().upsert(
            project(candidateId = "c1", status = SyncStatus.Pending, scoredAt = 1L),
        )

        val result = repository.clearLocalCache()

        assertTrue(result is SaveResult.Success)
        assertEquals(0, db.attendanceDao().totalCount())
        assertNull(db.paperDao().getById("p1"))
        assertNull(db.centerDao().getById("c1"))
        assertEquals(0, assessmentDb.practicalScoreDao().totalCount())
        assertEquals(0, assessmentDb.projectScoreDao().totalCount())
    }

    private fun center() = CenterEntity(
        id = "c1",
        name = "Lagos Centre",
        code = "LAG",
        location = "Lagos",
    )

    private fun paper() = PaperEntity(
        id = "p1",
        centerId = "c1",
        title = "Paper 1",
        subtitle = "Practical",
        paperKind = PaperKind.Practical.name,
        startAt = 0L,
        endAt = 1L,
        hall = "Hall A",
        totalCandidates = 30,
    )

    private fun candidate() = CandidateEntity(
        id = "c1",
        examNumber = "EX1",
        fullName = "Ada",
    )

    private fun attendance(
        candidateId: String,
        syncStatus: SyncStatus = SyncStatus.Pending,
        markedAt: Long = 1_700_000_000_000L,
    ) = AttendanceEntity(
        paperId = "p1",
        candidateId = candidateId,
        status = AttendanceStatus.SignedIn.name,
        markedAt = markedAt,
        syncStatus = syncStatus.name,
    )

    private fun practical(
        candidateId: String,
        questionId: String,
        status: SyncStatus,
        scoredAt: Long,
    ) = PracticalScoreEntity(
        scheduleId = "p1",
        candidateId = candidateId,
        questionId = questionId,
        score = 5,
        scoredAt = scoredAt,
        syncStatus = status.name,
    )

    private fun project(
        candidateId: String,
        status: SyncStatus,
        scoredAt: Long,
    ) = ProjectScoreEntity(
        scheduleId = "p1",
        candidateId = candidateId,
        score = 8.0,
        maxScore = 20,
        scoredAt = scoredAt,
        syncStatus = status.name,
    )

    private fun remark() = RemarkEntity(
        id = "r1",
        candidateId = "c1",
        paperId = "p1",
        body = "Late arrival",
        severity = RemarkSeverity.Info.name,
        createdAt = 0L,
        syncStatus = SyncStatus.Pending.name,
    )
}
