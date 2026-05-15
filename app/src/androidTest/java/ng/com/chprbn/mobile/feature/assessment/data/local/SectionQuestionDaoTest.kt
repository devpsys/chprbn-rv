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
class SectionQuestionDaoTest {

    private lateinit var db: AssessmentDatabase
    private lateinit var sectionDao: PracticalSectionDao
    private lateinit var dao: SectionQuestionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AssessmentDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sectionDao = db.sectionDao()
        dao = db.questionDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getBySectionIdReturnsRowsOrderedByNumberAscending() = runTest {
        sectionDao.upsertAll(listOf(section(id = "A", scheduleId = "s1")))
        dao.upsertAll(
            listOf(
                question(id = "q3", sectionId = "A", number = 3),
                question(id = "q1", sectionId = "A", number = 1),
                question(id = "q2", sectionId = "A", number = 2),
            ),
        )

        assertEquals(listOf("q1", "q2", "q3"), dao.getBySectionId("A").map { it.id })
    }

    @Test
    fun getByScheduleIdJoinsThroughPracticalSections() = runTest {
        sectionDao.upsertAll(
            listOf(
                section(id = "A", scheduleId = "s1"),
                section(id = "B", scheduleId = "s2"),
            ),
        )
        dao.upsertAll(
            listOf(
                question(id = "q1", sectionId = "A", number = 1),
                question(id = "q2", sectionId = "A", number = 2),
                question(id = "q3", sectionId = "B", number = 1),
            ),
        )

        val rows = dao.getByScheduleId("s1")

        assertEquals(2, rows.size)
        assertTrue(rows.all { it.sectionId == "A" })
    }

    @Test
    fun deleteByScheduleIdRemovesOnlyQuestionsForThatSchedule() = runTest {
        sectionDao.upsertAll(
            listOf(
                section(id = "A", scheduleId = "s1"),
                section(id = "B", scheduleId = "s2"),
            ),
        )
        dao.upsertAll(
            listOf(
                question(id = "q1", sectionId = "A"),
                question(id = "q2", sectionId = "A"),
                question(id = "q3", sectionId = "B"),
            ),
        )

        val deleted = dao.deleteByScheduleId("s1")

        assertEquals(2, deleted)
        assertTrue(dao.getBySectionId("A").isEmpty())
        assertEquals(1, dao.getBySectionId("B").size)
    }

    @Test
    fun clearAllWipesEveryRow() = runTest {
        sectionDao.upsertAll(listOf(section(id = "A", scheduleId = "s1")))
        dao.upsertAll(listOf(question(id = "q1", sectionId = "A")))

        val deleted = dao.clearAll()

        assertEquals(1, deleted)
        assertTrue(dao.getBySectionId("A").isEmpty())
    }

    private fun section(id: String, scheduleId: String) = PracticalSectionEntity(
        id = id,
        scheduleId = scheduleId,
        title = "Section $id",
        subtitle = "Sub $id",
        ordering = 0,
    )

    private fun question(
        id: String,
        sectionId: String,
        number: Int = 1,
    ) = SectionQuestionEntity(
        id = id,
        sectionId = sectionId,
        number = number,
        prompt = "Prompt $id",
        imageUrl = null,
        maxScore = 5,
    )
}
