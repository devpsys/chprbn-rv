package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssessmentScheduleDaoTest {

    private lateinit var db: AssessmentDatabase
    private lateinit var dao: AssessmentScheduleDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AssessmentDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.scheduleDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getAllReturnsRowsOrderedByDateAscending() = runTest {
        dao.upsertAll(
            listOf(
                schedule(id = "s2", date = 200L),
                schedule(id = "s1", date = 100L),
                schedule(id = "s3", date = 300L),
            ),
        )

        assertEquals(listOf("s1", "s2", "s3"), dao.getAll().map { it.id })
    }

    @Test
    fun upsertReplacesExistingRowOnIdConflict() = runTest {
        dao.upsert(schedule(id = "s1", title = "Original"))
        dao.upsert(schedule(id = "s1", title = "Renamed"))

        assertEquals("Renamed", dao.getById("s1")?.title)
    }

    @Test
    fun getByIdReturnsNullWhenAbsent() = runTest {
        assertNull(dao.getById("missing"))
    }

    @Test
    fun updateSyncStatusFlipsTheCachedDerivedValue() = runTest {
        dao.upsert(schedule(id = "s1", syncStatus = SyncStatus.Pending.name))

        val updated = dao.updateSyncStatus("s1", SyncStatus.Synced.name)

        assertEquals(1, updated)
        assertEquals(SyncStatus.Synced.name, dao.getById("s1")?.syncStatus)
    }

    @Test
    fun updateSyncStatusOnMissingRowReturnsZero() = runTest {
        val updated = dao.updateSyncStatus("missing", SyncStatus.Synced.name)

        assertEquals(0, updated)
    }

    @Test
    fun clearAllWipesEveryRow() = runTest {
        dao.upsertAll(listOf(schedule("s1"), schedule("s2")))

        val deleted = dao.clearAll()

        assertEquals(2, deleted)
        assertEquals(emptyList<AssessmentScheduleEntity>(), dao.getAll())
    }

    private fun schedule(
        id: String,
        title: String = "PE-$id",
        date: Long = 1_700_000_000_000L,
        syncStatus: String = SyncStatus.Pending.name,
    ) = AssessmentScheduleEntity(
        id = id,
        title = title,
        date = date,
        paperKind = PaperKind.Practical.name,
        centerId = "C-1",
        syncStatus = syncStatus,
    )
}
