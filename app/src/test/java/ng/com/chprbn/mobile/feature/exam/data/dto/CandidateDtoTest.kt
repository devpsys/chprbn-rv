package ng.com.chprbn.mobile.feature.exam.data.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regression coverage pinned against a real candidate record captured
 * from a live device (2026-08-14). The live wire keys (`indexing`,
 * `fullname`, `photo`) diverge from what `full-api-documentation.md`
 * §10.1 originally speculated (`exam_number`, `full_name`, `photo_url`)
 * — this is what made "150 candidates downloaded" render as "0 cached"
 * once the candidates array itself was reachable.
 */
class CandidateDtoTest {

    private val gson = Gson()

    @Test
    fun `parses the live candidate record's indexing, fullname, and photo keys`() {
        val json = """
            {
              "id": 22710,
              "indexing": "B/213/104/14",
              "fullname": "MUHAMMAD ALIYU ",
              "photo": "base64-jpeg-bytes"
            }
        """.trimIndent()

        val dto = gson.fromJson(json, CandidateDto::class.java)

        assertEquals("22710", dto.id)
        assertEquals("B/213/104/14", dto.examNumber)
        assertEquals("MUHAMMAD ALIYU ", dto.fullName)
        assertEquals("base64-jpeg-bytes", dto.photoUrl)
    }

    @Test
    fun `still accepts the originally documented exam_number, full_name, photo_url keys`() {
        val json = """
            {
              "id": "can_1",
              "exam_number": "EX-2026-00001",
              "full_name": "John Adebayo",
              "photo_url": "data:image/jpeg;base64,xyz"
            }
        """.trimIndent()

        val dto = gson.fromJson(json, CandidateDto::class.java)

        assertEquals("can_1", dto.id)
        assertEquals("EX-2026-00001", dto.examNumber)
        assertEquals("John Adebayo", dto.fullName)
        assertEquals("data:image/jpeg;base64,xyz", dto.photoUrl)
    }
}
