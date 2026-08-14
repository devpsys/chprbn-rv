package ng.com.chprbn.mobile.feature.verification.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QrRegistrationExtractorTest {

    @Test
    fun `slash-delimited candidate registration number is accepted`() {
        assertEquals("B/213/105/24", extractRegistrationFromQrPayload("B/213/105/24"))
    }

    @Test
    fun `hash-line practitioner payload extracts the token after the hash`() {
        val payload = """
            #: B2320135
            Cadre: CHEW
            Expiry: 2025-10-24
        """.trimIndent()

        assertEquals("B2320135", extractRegistrationFromQrPayload(payload))
    }

    @Test
    fun `plain alphanumeric id is accepted`() {
        assertEquals("B2320135", extractRegistrationFromQrPayload("B2320135"))
    }

    @Test
    fun `blank payload is rejected`() {
        assertNull(extractRegistrationFromQrPayload("   "))
    }

    @Test
    fun `payload with spaces and no hash line is rejected`() {
        assertNull(extractRegistrationFromQrPayload("not a registration number"))
    }
}
