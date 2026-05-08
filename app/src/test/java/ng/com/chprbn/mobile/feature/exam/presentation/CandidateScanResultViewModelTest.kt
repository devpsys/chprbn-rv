package ng.com.chprbn.mobile.feature.exam.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CandidateScanResultViewModelTest {

    @Before
    fun setUp() {
        // android.net.Uri is a framework class and unmocked by default in JVM
        // unit tests — stub the static decode() so we can exercise the
        // ViewModel without pulling in Robolectric.
        mockkStatic(Uri::class)
        every { Uri.decode(any<String>()) } answers { firstArg<String>() }
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    @Test
    fun `state derives exam number from the scanned payload nav arg`() {
        val savedStateHandle = SavedStateHandle(mapOf("scannedPayload" to "REG-123"))

        val viewModel = CandidateScanResultViewModel(savedStateHandle)

        assertEquals("Exam Number: REG-123", viewModel.uiState.value.examNumberLine)
    }

    @Test
    fun `state url-decodes the scanned payload`() {
        every { Uri.decode("REG%2D123") } returns "REG-123"
        val savedStateHandle = SavedStateHandle(mapOf("scannedPayload" to "REG%2D123"))

        val viewModel = CandidateScanResultViewModel(savedStateHandle)

        assertEquals("Exam Number: REG-123", viewModel.uiState.value.examNumberLine)
    }

    @Test
    fun `state trims whitespace around the scanned payload`() {
        val savedStateHandle = SavedStateHandle(mapOf("scannedPayload" to "  REG-123  "))

        val viewModel = CandidateScanResultViewModel(savedStateHandle)

        assertEquals("Exam Number: REG-123", viewModel.uiState.value.examNumberLine)
    }

    @Test
    fun `state falls back to placeholder exam number when nav arg is missing`() {
        val viewModel = CandidateScanResultViewModel(SavedStateHandle())

        assertEquals("Exam Number: ABC-12345-XY", viewModel.uiState.value.examNumberLine)
    }

    @Test
    fun `state falls back to placeholder exam number when nav arg is blank`() {
        val savedStateHandle = SavedStateHandle(mapOf("scannedPayload" to "   "))

        val viewModel = CandidateScanResultViewModel(savedStateHandle)

        assertEquals("Exam Number: ABC-12345-XY", viewModel.uiState.value.examNumberLine)
    }

    @Test
    fun `state always reports the verified-identity headline and 98 percent match`() {
        val viewModel = CandidateScanResultViewModel(
            SavedStateHandle(mapOf("scannedPayload" to "REG-123"))
        )

        val state = viewModel.uiState.value
        assertEquals("Identity Verified", state.identityVerifiedHeadline)
        assertTrue("Match label should be a percentage", state.matchLabel.endsWith("%"))
        assertEquals("MATCH 98%", state.matchLabel)
    }
}
