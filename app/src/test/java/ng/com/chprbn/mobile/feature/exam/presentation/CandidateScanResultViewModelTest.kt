package ng.com.chprbn.mobile.feature.exam.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.exam.domain.usecase.LookupCandidateByExamNumberUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CandidateScanResultViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var context: Context
    private val lookupCandidate = mockk<LookupCandidateByExamNumberUseCase>().also {
        coEvery { it(any()) } returns null
    }

    @Before
    fun setUp() {
        // android.net.Uri is a framework class and unmocked by default in JVM
        // unit tests — stub the static decode() so we can exercise the
        // ViewModel without pulling in Robolectric.
        mockkStatic(Uri::class)
        every { Uri.decode(any<String>()) } answers { firstArg<String>() }

        // CandidateScanResultUiState.fromScannedPayload() now reads every
        // user-facing label from strings.xml via Context.getString(); stub
        // each call so the assertions below see the same copy the production
        // factory would produce.
        context = mockk {
            every { getString(R.string.candidate_scan_default_name) } returns "Johnathan Doe"
            // getString(@StringRes Int, vararg Any?) — match the trimmed exam-number
            // arg explicitly per test case so the format substitution is exercised.
            every {
                getString(R.string.candidate_scan_exam_number_format, "REG-123")
            } returns "Exam Number: REG-123"
            every {
                getString(R.string.candidate_scan_exam_number_format, "ABC-12345-XY")
            } returns "Exam Number: ABC-12345-XY"
            every { getString(R.string.candidate_scan_default_exam_number) } returns "ABC-12345-XY"
            every { getString(R.string.candidate_scan_verification_section) } returns "Identity Verification"
            every { getString(R.string.candidate_scan_identity_verified_headline) } returns "Identity Verified"
            every { getString(R.string.candidate_scan_match_label) } returns "MATCH 98%"
            every { getString(R.string.candidate_scan_exam_date_caption) } returns "Exam Date"
            every { getString(R.string.candidate_scan_default_exam_date) } returns "Oct 24, 2023"
            every { getString(R.string.candidate_scan_testing_center_caption) } returns "Testing Center"
            every { getString(R.string.candidate_scan_default_testing_center) } returns "Hall B - Room 12"
        }
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    @Test
    fun `state derives exam number from the scanned payload nav arg`() {
        val savedStateHandle = SavedStateHandle(mapOf("scannedPayload" to "REG-123"))

        val viewModel = CandidateScanResultViewModel(savedStateHandle, context, lookupCandidate)

        assertEquals("Exam Number: REG-123", viewModel.uiState.value.examNumberLine)
    }

    @Test
    fun `state url-decodes the scanned payload`() {
        every { Uri.decode("REG%2D123") } returns "REG-123"
        val savedStateHandle = SavedStateHandle(mapOf("scannedPayload" to "REG%2D123"))

        val viewModel = CandidateScanResultViewModel(savedStateHandle, context, lookupCandidate)

        assertEquals("Exam Number: REG-123", viewModel.uiState.value.examNumberLine)
    }

    @Test
    fun `state trims whitespace around the scanned payload`() {
        val savedStateHandle = SavedStateHandle(mapOf("scannedPayload" to "  REG-123  "))

        val viewModel = CandidateScanResultViewModel(savedStateHandle, context, lookupCandidate)

        assertEquals("Exam Number: REG-123", viewModel.uiState.value.examNumberLine)
    }

    @Test
    fun `state falls back to placeholder exam number when nav arg is missing`() {
        val viewModel = CandidateScanResultViewModel(SavedStateHandle(), context, lookupCandidate)

        assertEquals("Exam Number: ABC-12345-XY", viewModel.uiState.value.examNumberLine)
    }

    @Test
    fun `state falls back to placeholder exam number when nav arg is blank`() {
        val savedStateHandle = SavedStateHandle(mapOf("scannedPayload" to "   "))

        val viewModel = CandidateScanResultViewModel(savedStateHandle, context, lookupCandidate)

        assertEquals("Exam Number: ABC-12345-XY", viewModel.uiState.value.examNumberLine)
    }

    @Test
    fun `state always reports the verified-identity headline and 98 percent match`() {
        val viewModel = CandidateScanResultViewModel(
            SavedStateHandle(mapOf("scannedPayload" to "REG-123")),
            context,
            lookupCandidate,
        )

        val state = viewModel.uiState.value
        assertEquals("Identity Verified", state.identityVerifiedHeadline)
        assertTrue("Match label should be a percentage", state.matchLabel.endsWith("%"))
        assertEquals("MATCH 98%", state.matchLabel)
    }
}
