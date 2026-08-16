package ng.com.chprbn.mobile.feature.assessment.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentDatabase
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreEntity
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamPaperRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The download-package flow is gone: sections + questions arrive on the
 * exam dossier and are persisted by `ExamSyncRepositoryImpl`, so this
 * repo only owns schedules-from-dossier, paper-detail read, and cache
 * wipes. The remaining tests cover [clearCache] — the only path that
 * still writes into `assessment.db` from here.
 */
@RunWith(AndroidJUnit4::class)
class AssessmentScheduleRepositoryImplTest {

    private lateinit var db: AssessmentDatabase
    private lateinit var repository: AssessmentScheduleRepositoryImpl
    private lateinit var examPaperRepository: ExamPaperRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AssessmentDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        // getSchedules/getPaperDetail exercise this via examPaperRepository;
        // clearCache tests don't touch it. Strict mock is fine — the two
        // clearCache tests never call any exam-repo method.
        examPaperRepository = mockk()
        repository = AssessmentScheduleRepositoryImpl(
            db = db,
            paperDao = db.paperDao(),
            sectionDao = db.sectionDao(),
            questionDao = db.questionDao(),
            candidateDao = db.candidateDao(),
            practicalScoreDao = db.practicalScoreDao(),
            projectScoreDao = db.projectScoreDao(),
            examPaperRepository = examPaperRepository,
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `per-schedule clearCache wipes only that schedule's score rows`() = runTest {
        db.practicalScoreDao().upsert(
            PracticalScoreEntity(
                scheduleId = "PE-2024", candidateId = "c1", questionId = "q1",
                score = 8, scoredAt = 0L, syncStatus = SyncStatus.Pending.name,
            ),
        )
        db.practicalScoreDao().upsert(
            PracticalScoreEntity(
                scheduleId = "MD-801", candidateId = "c1", questionId = "q1",
                score = 5, scoredAt = 0L, syncStatus = SyncStatus.Pending.name,
            ),
        )

        val result = repository.clearCache("PE-2024")

        assertTrue(result is SaveResult.Success)
        assertNull(db.practicalScoreDao().getOne("PE-2024", "c1", "q1"))
        assertNotNull(db.practicalScoreDao().getOne("MD-801", "c1", "q1"))
    }

    @Test
    fun `global clearCache wipes everything including scores`() = runTest {
        db.practicalScoreDao().upsert(
            PracticalScoreEntity(
                scheduleId = "PE-2024", candidateId = "c1", questionId = "q1",
                score = 8, scoredAt = 0L, syncStatus = SyncStatus.Pending.name,
            ),
        )

        val result = repository.clearCache(null)

        assertTrue(result is SaveResult.Success)
        assertNull(db.practicalScoreDao().getOne("PE-2024", "c1", "q1"))
        assertEquals(0, db.practicalScoreDao().countByStatusForSchedule("PE-2024", SyncStatus.Pending.name))
    }
}
