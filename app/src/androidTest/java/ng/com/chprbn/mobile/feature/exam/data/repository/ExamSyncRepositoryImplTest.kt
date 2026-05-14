package ng.com.chprbn.mobile.feature.exam.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.SyncBatchRunner
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceEntity
import ng.com.chprbn.mobile.feature.exam.data.local.ExamDatabase
import ng.com.chprbn.mobile.feature.exam.data.local.RemarkEntity
import ng.com.chprbn.mobile.feature.exam.data.source.ExamDossierBundle
import ng.com.chprbn.mobile.feature.exam.data.source.ExamDossierRemoteSource
import ng.com.chprbn.mobile.feature.exam.data.source.ExamPaperAssignment
import ng.com.chprbn.mobile.feature.exam.domain.model.Center
import ng.com.chprbn.mobile.feature.exam.domain.model.DownloadDossierResult
import ng.com.chprbn.mobile.feature.exam.domain.model.Paper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Real Room-backed test of [ExamSyncRepositoryImpl.downloadDossier].
 * The key invariant: attendance and remarks survive a re-download —
 * that's the UX contract behind the download-warning prompt.
 */
@RunWith(AndroidJUnit4::class)
class ExamSyncRepositoryImplTest {

    private lateinit var db: ExamDatabase
    private lateinit var repository: ExamSyncRepositoryImpl
    private lateinit var remoteSource: ExamDossierRemoteSource
    private lateinit var runner: SyncBatchRunner

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ExamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        remoteSource = mockk()
        runner = mockk {
            coEvery { runBatch(any()) } returns SyncBatchResult.Empty
        }
        repository = ExamSyncRepositoryImpl(
            db = db,
            centerDao = db.centerDao(),
            paperDao = db.paperDao(),
            candidateDao = db.candidateDao(),
            remoteSource = remoteSource,
            runner = runner,
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun downloadDossierPersistsCenterPapersCandidatesAssignments() = runTest {
        coEvery { remoteSource.fetchDossier() } returns sampleBundle()

        val result = repository.downloadDossier()

        assertTrue("expected Success, was $result", result is DownloadDossierResult.Success)
        val success = result as DownloadDossierResult.Success
        assertEquals(2, success.papersCount)
        assertEquals(2, success.candidatesCount)

        assertNotNull(db.centerDao().getById("C-1"))
        assertEquals(2, db.paperDao().getAll().size)
        assertNotNull(db.candidateDao().getById("c1"))
        assertNotNull(db.candidateDao().getById("c2"))
        // 2 candidates × 2 papers = 4 assignments visible through any per-paper query.
        assertEquals(2, db.candidateDao().rowsForPaper("p1", "All", "").size)
    }

    @Test
    fun downloadDossierPreservesAttendanceAndRemarks() = runTest {
        // Seed local writes BEFORE the download.
        db.attendanceDao().upsert(
            AttendanceEntity(
                paperId = "p1",
                candidateId = "c1",
                status = "SignedIn",
                markedAt = 1L,
                syncStatus = SyncStatus.Pending.name,
            ),
        )
        db.remarkDao().upsert(
            RemarkEntity(
                id = "r1",
                candidateId = "c1",
                paperId = "p1",
                body = "x",
                severity = "Info",
                createdAt = 1L,
                syncStatus = SyncStatus.Pending.name,
            ),
        )

        coEvery { remoteSource.fetchDossier() } returns sampleBundle()

        repository.downloadDossier()

        assertNotNull(
            "attendance must survive re-download",
            db.attendanceDao().getOne("p1", "c1"),
        )
        assertNotNull(
            "remark must survive re-download",
            db.remarkDao().getById("r1"),
        )
    }

    @Test
    fun downloadDossierReportsErrorWhenRemoteReturnsNull() = runTest {
        coEvery { remoteSource.fetchDossier() } returns null

        val result = repository.downloadDossier()

        assertTrue(result is DownloadDossierResult.Error)
    }

    @Test
    fun replaceTwicePersistsLatestReferenceData() = runTest {
        coEvery { remoteSource.fetchDossier() } returns sampleBundle()
        repository.downloadDossier()

        // Second download with a different candidate set — old assignment
        // rows for c1/c2 must be cleared, new c3 must appear.
        coEvery { remoteSource.fetchDossier() } returns sampleBundle(
            candidates = listOf(Candidate("c3", "EX-3", "New Cand")),
            assignments = listOf(
                ExamPaperAssignment("p1", "c3"),
                ExamPaperAssignment("p2", "c3"),
            ),
        )
        repository.downloadDossier()

        val rows = db.candidateDao().rowsForPaper("p1", "All", "")
        assertEquals(1, rows.size)
        assertEquals("c3", rows.single().candidateId)
    }

    private fun sampleBundle(
        candidates: List<Candidate> = listOf(
            Candidate("c1", "EX-1", "Jane Doe"),
            Candidate("c2", "EX-2", "John Smith"),
        ),
        assignments: List<ExamPaperAssignment> = listOf(
            ExamPaperAssignment("p1", "c1"),
            ExamPaperAssignment("p1", "c2"),
            ExamPaperAssignment("p2", "c1"),
            ExamPaperAssignment("p2", "c2"),
        ),
    ) = ExamDossierBundle(
        center = Center("C-1", "Lagos Centre", "LAG-001", "Marina", null),
        papers = listOf(
            Paper("p1", "C-1", "Paper I", "English", PaperKind.Theory, 0L, 0L, "Hall A", 42),
            Paper("p2", "C-1", "Paper II", "Maths", PaperKind.Theory, 0L, 0L, "Hall A", 42),
        ),
        candidates = candidates,
        assignments = assignments,
    )
}
