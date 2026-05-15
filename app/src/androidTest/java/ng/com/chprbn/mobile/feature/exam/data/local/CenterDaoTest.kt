package ng.com.chprbn.mobile.feature.exam.data.local

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
class CenterDaoTest {

    private lateinit var db: ExamDatabase
    private lateinit var dao: CenterDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, ExamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.centerDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertInsertsThenReplaces() = runTest {
        dao.upsert(center(name = "Lagos Centre"))
        dao.upsert(center(name = "Lagos Centre (Renamed)"))

        val row = dao.getById("c1")

        assertNotNull(row)
        assertEquals("Lagos Centre (Renamed)", row!!.name)
    }

    @Test
    fun upsertAllPersistsEveryRow() = runTest {
        dao.upsertAll(
            listOf(
                center(id = "c1", name = "Lagos"),
                center(id = "c2", name = "Abuja"),
                center(id = "c3", name = "Kano"),
            ),
        )

        assertEquals("Lagos", dao.getById("c1")?.name)
        assertEquals("Abuja", dao.getById("c2")?.name)
        assertEquals("Kano", dao.getById("c3")?.name)
    }

    @Test
    fun getByIdReturnsNullWhenAbsent() = runTest {
        assertNull(dao.getById("missing"))
    }

    @Test
    fun clearAllWipesAllRows() = runTest {
        dao.upsertAll(listOf(center("c1"), center("c2")))

        val deleted = dao.clearAll()

        assertEquals(2, deleted)
        assertNull(dao.getById("c1"))
        assertNull(dao.getById("c2"))
    }

    private fun center(id: String = "c1", name: String = "Lagos Centre") = CenterEntity(
        id = id,
        name = name,
        code = "LAG",
        location = "Lagos",
    )
}
