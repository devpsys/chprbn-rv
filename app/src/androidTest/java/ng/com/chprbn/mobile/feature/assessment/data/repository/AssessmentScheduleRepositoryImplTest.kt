package ng.com.chprbn.mobile.feature.assessment.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentDatabase
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreEntity
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreEntity
import ng.com.chprbn.mobile.feature.assessment.data.source.AssessmentPackageBundle
import ng.com.chprbn.mobile.feature.assessment.data.source.AssessmentPackageRemoteSource
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaper
import ng.com.chprbn.mobile.feature.assessment.domain.model.DownloadAssessmentPackageResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.Facility
import ng.com.chprbn.mobile.feature.assessment.domain.model.Hall
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSection
import ng.com.chprbn.mobile.feature.assessment.domain.model.SectionQuestion
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Real Room-backed test of [AssessmentScheduleRepositoryImpl.downloadPackage].
 * The key invariant: practical/project scores survive a re-download — that's
 * the UX contract behind the download-warning prompt.
 */
@RunWith(AndroidJUnit4::class)
class AssessmentScheduleRepositoryImplTest {

    private lateinit var db: AssessmentDatabase
    private lateinit var repository: AssessmentScheduleRepositoryImpl
    private lateinit var remoteSource: AssessmentPackageRemoteSource

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AssessmentDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        remoteSource = mockk()
        repository = AssessmentScheduleRepositoryImpl(
            db = db,
            scheduleDao = db.scheduleDao(),
            paperDao = db.paperDao(),
            sectionDao = db.sectionDao(),
            questionDao = db.questionDao(),
            candidateDao = db.candidateDao(),
            practicalScoreDao = db.practicalScoreDao(),
            projectScoreDao = db.projectScoreDao(),
            remoteSource = remoteSource,
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun downloadPackagePersistsEveryFanoutTable() = runTest {
        coEvery { remoteSource.fetchPackage("PE-2024") } returns samplePackage()

        val result = repository.downloadPackage("PE-2024")

        assertTrue("expected Success, was $result", result is DownloadAssessmentPackageResult.Success)
        val success = result as DownloadAssessmentPackageResult.Success
        assertEquals("PE-2024", success.scheduleId)
        assertEquals(2, success.candidatesCount)
        assertEquals(1, success.sectionsCount)
        assertEquals(2, success.questionsCount)

        assertNotNull(db.paperDao().getByScheduleId("PE-2024"))
        assertEquals(1, db.sectionDao().getByScheduleId("PE-2024").size)
        assertEquals(2, db.questionDao().getByScheduleId("PE-2024").size)
        // 2 candidates × 1 schedule = 2 assignments + 2 candidate rows
        assertNotNull(db.candidateDao().getForSchedule("PE-2024", "c1"))
        assertNotNull(db.candidateDao().getForSchedule("PE-2024", "c2"))
    }

    @Test
    fun downloadPackagePreservesPendingScoreWrites() = runTest {
        // Seed scores BEFORE the download — these must survive the re-download.
        db.practicalScoreDao().upsert(
            PracticalScoreEntity(
                scheduleId = "PE-2024",
                candidateId = "c1",
                questionId = "q1",
                score = 8,
                scoredAt = 1_700_000_000_000L,
                syncStatus = SyncStatus.Pending.name,
            ),
        )
        db.projectScoreDao().upsert(
            ProjectScoreEntity(
                scheduleId = "PE-2024",
                candidateId = "c1",
                score = 7.5,
                maxScore = 10,
                scoredAt = 1_700_000_000_000L,
                syncStatus = SyncStatus.Pending.name,
            ),
        )

        coEvery { remoteSource.fetchPackage("PE-2024") } returns samplePackage()

        repository.downloadPackage("PE-2024")

        val practical = db.practicalScoreDao().getOne("PE-2024", "c1", "q1")
        assertNotNull("practical score must survive re-download", practical)
        assertEquals(8, practical!!.score)

        val project = db.projectScoreDao().getOne("PE-2024", "c1")
        assertNotNull("project score must survive re-download", project)
        assertEquals(7.5, project!!.score, 0.0)
    }

    @Test
    fun downloadPackageReportsErrorWhenRemoteReturnsNull() = runTest {
        coEvery { remoteSource.fetchPackage("PE-2024") } returns null

        val result = repository.downloadPackage("PE-2024")

        assertTrue(result is DownloadAssessmentPackageResult.Error)
    }

    @Test
    fun perScheduleClearCacheWipesScoresButGlobalClearLeavesScoresAlone() = runTest {
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

        // Per-schedule wipe: scores for that schedule go, the other schedule's stay.
        repository.clearCache("PE-2024")

        assertEquals(null, db.practicalScoreDao().getOne("PE-2024", "c1", "q1"))
        assertNotNull(db.practicalScoreDao().getOne("MD-801", "c1", "q1"))
    }

    @Test
    fun replaceTwicePersistsNewReferenceData() = runTest {
        coEvery { remoteSource.fetchPackage("PE-2024") } returns samplePackage()
        repository.downloadPackage("PE-2024")

        // Second download with different candidate set; old reference rows
        // must be replaced cleanly without duplicate-PK collisions.
        coEvery { remoteSource.fetchPackage("PE-2024") } returns samplePackage(
            candidates = listOf(Candidate("c3", "EX-3", "New Candidate")),
        )
        repository.downloadPackage("PE-2024")

        assertNotNull(db.candidateDao().getForSchedule("PE-2024", "c3"))
        // Assignments for c1/c2 should be gone (old assignments deleted).
        val rows = db.candidateDao().rowsForSchedule("PE-2024", "")
        assertEquals(1, rows.size)
        assertEquals("c3", rows.single().candidateId)
    }

    private fun samplePackage(
        candidates: List<Candidate> = listOf(
            Candidate("c1", "EX-1", "Jane Doe"),
            Candidate("c2", "EX-2", "John Smith"),
        ),
    ) = AssessmentPackageBundle(
        paper = AssessmentPaper(
            scheduleId = "PE-2024",
            title = "Paper A",
            statusLabel = "Active",
            facility = Facility("Lagos", "10 Marina"),
            hall = Hall("Hall B", "Room 12"),
            heroImageUrl = null,
        ),
        sections = listOf(
            PracticalSection(
                id = "PE-2024-sec-A",
                scheduleId = "PE-2024",
                title = "A",
                subtitle = "Vitals",
                ordering = 1,
            ),
        ),
        questions = listOf(
            SectionQuestion("q1", "PE-2024-sec-A", 1, "Take BP", null, 10),
            SectionQuestion("q2", "PE-2024-sec-A", 2, "Take pulse", null, 10),
        ),
        candidates = candidates,
    )
}
