package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemarkDaoTest {

    private lateinit var db: ExamDatabase
    private lateinit var dao: RemarkDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, ExamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.remarkDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertReplacesByPrimaryKey() = runTest {
        dao.upsert(remark(id = "r1", body = "first"))
        dao.upsert(remark(id = "r1", body = "edited"))

        assertEquals("edited", dao.getById("r1")?.body)
    }

    @Test
    fun getForCandidateReturnsRowsInReverseChronologicalOrder() = runTest {
        dao.upsert(remark(id = "r1", candidateId = "c1", createdAt = 100L))
        dao.upsert(remark(id = "r2", candidateId = "c1", createdAt = 300L))
        dao.upsert(remark(id = "r3", candidateId = "c1", createdAt = 200L))
        dao.upsert(remark(id = "r4", candidateId = "c2", createdAt = 500L))

        val rows = dao.getForCandidate("c1")

        assertEquals(listOf("r2", "r3", "r1"), rows.map { it.id })
    }

    @Test
    fun pendingAndFailedFiltersBySyncStatus() = runTest {
        dao.upsert(remark(id = "r1", syncStatus = SyncStatus.Pending))
        dao.upsert(remark(id = "r2", syncStatus = SyncStatus.Synced))
        dao.upsert(remark(id = "r3", syncStatus = SyncStatus.Failed))

        val pending = dao.pendingAndFailed()

        assertEquals(2, pending.size)
        assertEquals(setOf("r1", "r3"), pending.map { it.id }.toSet())
    }

    @Test
    fun updateSyncMetadataFlipsStatusAndKeepsBody() = runTest {
        dao.upsert(remark(id = "r1", body = "Late", syncStatus = SyncStatus.Pending))

        val rows = dao.updateSyncMetadata(
            id = "r1",
            syncStatus = SyncStatus.Synced.name,
            syncError = null,
            lastSyncAttemptAt = 9L,
        )

        assertEquals(1, rows)
        val row = dao.getById("r1")!!
        assertEquals(SyncStatus.Synced.name, row.syncStatus)
        assertEquals("Late", row.body)
        assertEquals(9L, row.lastSyncAttemptAt)
        assertNull(row.syncError)
    }

    @Test
    fun clearAllWipesEverything() = runTest {
        dao.upsert(remark(id = "r1"))
        dao.upsert(remark(id = "r2"))

        val deleted = dao.clearAll()

        assertEquals(2, deleted)
        assertNull(dao.getById("r1"))
    }

    @Test
    fun getByIdReturnsNullWhenAbsent() = runTest {
        assertNull(dao.getById("missing"))
    }

    @Test
    fun upsertPersistsOptionalPaperId() = runTest {
        dao.upsert(remark(id = "r1", paperId = null))

        assertNotNull(dao.getById("r1"))
        assertNull(dao.getById("r1")?.paperId)
    }

    private fun remark(
        id: String = "r1",
        candidateId: String = "c1",
        paperId: String? = "p1",
        body: String = "Late arrival",
        severity: RemarkSeverity = RemarkSeverity.Info,
        createdAt: Long = 1_700_000_000_000L,
        syncStatus: SyncStatus = SyncStatus.Pending,
    ) = RemarkEntity(
        id = id,
        candidateId = candidateId,
        paperId = paperId,
        body = body,
        severity = severity.name,
        createdAt = createdAt,
        syncStatus = syncStatus.name,
    )
}
