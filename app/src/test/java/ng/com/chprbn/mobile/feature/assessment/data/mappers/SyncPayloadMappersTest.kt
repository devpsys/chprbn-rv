package ng.com.chprbn.mobile.feature.assessment.data.mappers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SyncPayloadMappersTest {

    @Test
    fun `practical score clientId is composite scheduleId+candidateId+questionId`() {
        assertEquals("PE-2024:c1:q1", practicalScoreClientId("PE-2024", "c1", "q1"))
    }

    @Test
    fun `project score clientId is composite scheduleId+candidateId`() {
        assertEquals("PE-2024:c1", projectScoreClientId("PE-2024", "c1"))
    }

    @Test
    fun `wireQuestionId extracts the trailing qN from a namespaced local id`() {
        assertEquals(11L, wireQuestionId("8-sec-1-q11"))
        assertEquals(1L, wireQuestionId("PA-9-sec-2-q1"))
    }

    @Test
    fun `wireQuestionId falls back to parsing the whole string`() {
        assertEquals(11L, wireQuestionId("11"))
        assertNull(wireQuestionId("q-x"))
    }
}
