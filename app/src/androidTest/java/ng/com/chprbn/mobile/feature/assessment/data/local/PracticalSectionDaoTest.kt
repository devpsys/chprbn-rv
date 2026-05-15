package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PracticalSectionDaoTest {

    private lateinit var db: AssessmentDatabase
    private lateinit var dao: PracticalSectionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AssessmentDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.sectionDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getByScheduleIdReturnsRowsOrderedByOrderingAscending() = runTest {
        dao.upsertAll(
            listOf(
                section(id = "C", scheduleId = "s1", ordering = 2),
                section(id = "A", scheduleId = "s1", ordering = 0),
                section(id = "B", scheduleId = "s1", ordering = 1),
            ),
        )

        assertEquals(listOf("A", "B", "C"), dao.getByScheduleId("s1").map { it.id })
    }

    @Test
    fun getByScheduleIdIsolatesRowsForOtherSchedules() = runTest {
        dao.upsertAll(
            listOf(
                section(id = "A", scheduleId = "s1"),
                section(id = "B", scheduleId = "s2"),
            ),
        )

        val rows = dao.getByScheduleId("s1")

        assertEquals(1, rows.size)
        assertEquals("A", rows.single().id)
    }

    @Test
    fun deleteByScheduleIdLeavesOtherSchedulesIntact() = runTest {
        dao.upsertAll(
            listOf(
                section(id = "A", scheduleId = "s1"),
                section(id = "B", scheduleId = "s1"),
                section(id = "C", scheduleId = "s2"),
            ),
        )

        val deleted = dao.deleteByScheduleId("s1")

        assertEquals(2, deleted)
        assertTrue(dao.getByScheduleId("s1").isEmpty())
        assertEquals(1, dao.getByScheduleId("s2").size)
    }

    @Test
    fun clearAllWipesEveryRow() = runTest {
        dao.upsertAll(
            listOf(section(id = "A", scheduleId = "s1"), section(id = "B", scheduleId = "s2")),
        )

        val deleted = dao.clearAll()

        assertEquals(2, deleted)
        assertTrue(dao.getByScheduleId("s1").isEmpty())
        assertTrue(dao.getByScheduleId("s2").isEmpty())
    }

    private fun section(
        id: String,
        scheduleId: String,
        ordering: Int = 0,
    ) = PracticalSectionEntity(
        id = id,
        scheduleId = scheduleId,
        title = "Section $id",
        subtitle = "Sub $id",
        ordering = ordering,
    )
}
