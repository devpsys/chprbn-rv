package ng.com.chprbn.mobile.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class StripHostsAndUrlsTest {

    @Test
    fun strips_url_hostname_and_ipv4() {
        val raw =
            "failed to connect to https://api.chprbn.gov.ng/216.219.94.166 (port 443) after 15000ms"
        val sanitised = raw.stripHostsAndUrls()

        assertFalse(sanitised.contains("api.chprbn.gov.ng"))
        assertFalse(sanitised.contains("216.219.94.166"))
        assertFalse(sanitised.contains("https://"))
        assertFalse(sanitised.contains("(port 443)"))
    }

    @Test
    fun leaves_plain_message_unchanged() {
        val raw = "Could not sync this assessment. Please try again."
        assertEquals(raw, raw.stripHostsAndUrls())
    }
}
