package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaperDaoTest {

    private lateinit var db: ExamDatabase
    private lateinit var dao: PaperDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, ExamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.paperDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertAllReplacesByPrimaryKey() = runTest {
        dao.upsertAll(listOf(paper(id = "p1", title = "First")))
        dao.upsertAll(listOf(paper(id = "p1", title = "First (Renamed)")))

        assertEquals("First (Renamed)", dao.getById("p1")?.title)
    }

    @Test
    fun getAllOrdersByStartAt() = runTest {
        dao.upsertAll(
            listOf(
                paper(id = "p3", startAt = 30L),
                paper(id = "p1", startAt = 10L),
                paper(id = "p2", startAt = 20L),
            ),
        )

        val rows = dao.getAll()

        assertEquals(listOf("p1", "p2", "p3"), rows.map { it.id })
    }

    @Test
    fun getForCenterReturnsOnlyMatchingRows() = runTest {
        dao.upsertAll(
            listOf(
                paper(id = "p1", centerId = "c1"),
                paper(id = "p2", centerId = "c2"),
                paper(id = "p3", centerId = "c1"),
            ),
        )

        val rows = dao.getForCenter("c1")

        assertEquals(2, rows.size)
        assertEquals(setOf("p1", "p3"), rows.map { it.id }.toSet())
    }

    @Test
    fun clearAllWipesEverything() = runTest {
        dao.upsertAll(listOf(paper(id = "p1"), paper(id = "p2")))

        val deleted = dao.clearAll()

        assertEquals(2, deleted)
        assertNull(dao.getById("p1"))
    }

    private fun paper(
        id: String = "p1",
        centerId: String = "c1",
        title: String = "Paper",
        startAt: Long = 0L,
    ) = PaperEntity(
        id = id,
        centerId = centerId,
        title = title,
        subtitle = "Practical",
        paperKind = PaperKind.Practical.name,
        startAt = startAt,
        endAt = startAt + 3_600_000L,
        hall = "Hall A",
        totalCandidates = 30,
    )
}
