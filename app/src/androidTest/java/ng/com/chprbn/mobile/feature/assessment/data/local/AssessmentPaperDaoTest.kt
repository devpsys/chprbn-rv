package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssessmentPaperDaoTest {

    private lateinit var db: AssessmentDatabase
    private lateinit var dao: AssessmentPaperDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AssessmentDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.paperDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertReplacesOnScheduleIdConflict() = runTest {
        dao.upsert(paper(scheduleId = "s1", title = "Original"))
        dao.upsert(paper(scheduleId = "s1", title = "Renamed"))

        assertEquals("Renamed", dao.getByScheduleId("s1")?.title)
    }

    @Test
    fun getByScheduleIdReturnsNullWhenAbsent() = runTest {
        assertNull(dao.getByScheduleId("missing"))
    }

    @Test
    fun deleteByScheduleIdRemovesOnlyTargetSchedule() = runTest {
        dao.upsert(paper(scheduleId = "s1"))
        dao.upsert(paper(scheduleId = "s2"))

        val deleted = dao.deleteByScheduleId("s1")

        assertEquals(1, deleted)
        assertNull(dao.getByScheduleId("s1"))
        assertNotNull(dao.getByScheduleId("s2"))
    }

    @Test
    fun clearAllWipesEveryRow() = runTest {
        dao.upsert(paper(scheduleId = "s1"))
        dao.upsert(paper(scheduleId = "s2"))

        val deleted = dao.clearAll()

        assertEquals(2, deleted)
        assertNull(dao.getByScheduleId("s1"))
        assertNull(dao.getByScheduleId("s2"))
    }

    private fun paper(
        scheduleId: String,
        title: String = "Pharmacology",
    ) = AssessmentPaperEntity(
        scheduleId = scheduleId,
        title = title,
        statusLabel = "Active",
        facilityName = "Lagos UTH",
        facilityAddress = "10 Marina",
        hallName = "Hall B",
        hallAddress = "Room 12",
        heroImageUrl = null,
    )
}
