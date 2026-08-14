package ng.com.chprbn.mobile.feature.exam.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.exam.domain.model.Attendance
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.MarkAttendanceResult
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetAttendanceStatusUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.LookupCandidateByExamNumberUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.MarkAttendanceUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    private val getAttendanceStatus = mockk<GetAttendanceStatusUseCase>().also {
        coEvery { it(any(), any()) } returns null
    }
    private val markAttendance = mockk<MarkAttendanceUseCase>()

    private fun viewModel(savedStateHandle: SavedStateHandle) = CandidateScanResultViewModel(
        savedStateHandle,
        context,
        lookupCandidate,
        getAttendanceStatus,
        markAttendance,
    )

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
            every {
                getString(R.string.candidate_scan_mark_attendance_error_no_candidate)
            } returns "This candidate isn't in the local roster for this paper. Sync the dossier and try again."
        }
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    @Test
    fun `state derives exam number from the scanned payload nav arg`() {
        val savedStateHandle = SavedStateHandle(mapOf("scannedPayload" to "REG-123"))

        val viewModel = viewModel(savedStateHandle)

        assertEquals("Exam Number: REG-123", viewModel.uiState.value.examNumberLine)
    }

    @Test
    fun `state url-decodes the scanned payload`() {
        every { Uri.decode("REG%2D123") } returns "REG-123"
        val savedStateHandle = SavedStateHandle(mapOf("scannedPayload" to "REG%2D123"))

        val viewModel = viewModel(savedStateHandle)

        assertEquals("Exam Number: REG-123", viewModel.uiState.value.examNumberLine)
    }

    @Test
    fun `state trims whitespace around the scanned payload`() {
        val savedStateHandle = SavedStateHandle(mapOf("scannedPayload" to "  REG-123  "))

        val viewModel = viewModel(savedStateHandle)

        assertEquals("Exam Number: REG-123", viewModel.uiState.value.examNumberLine)
    }

    @Test
    fun `state falls back to placeholder exam number when nav arg is missing`() {
        val viewModel = viewModel(SavedStateHandle())

        assertEquals("Exam Number: ABC-12345-XY", viewModel.uiState.value.examNumberLine)
    }

    @Test
    fun `state falls back to placeholder exam number when nav arg is blank`() {
        val savedStateHandle = SavedStateHandle(mapOf("scannedPayload" to "   "))

        val viewModel = viewModel(savedStateHandle)

        assertEquals("Exam Number: ABC-12345-XY", viewModel.uiState.value.examNumberLine)
    }

    @Test
    fun `state always reports the verified-identity headline and 98 percent match`() {
        val viewModel = viewModel(SavedStateHandle(mapOf("scannedPayload" to "REG-123")))

        val state = viewModel.uiState.value
        assertEquals("Identity Verified", state.identityVerifiedHeadline)
        assertTrue("Match label should be a percentage", state.matchLabel.endsWith("%"))
        assertEquals("MATCH 98%", state.matchLabel)
    }

    @Test
    fun `state carries the resolved candidate's photoUrl once lookup resolves`() = runTest {
        coEvery { lookupCandidate("REG-123") } returns Candidate(
            id = "can_1",
            examNumber = "REG-123",
            fullName = "Johnathan Doe",
            photoUrl = "data:image/jpeg;base64,AAAA",
        )
        val savedStateHandle = SavedStateHandle(mapOf("scannedPayload" to "REG-123"))

        val viewModel = viewModel(savedStateHandle)

        assertEquals("data:image/jpeg;base64,AAAA", viewModel.uiState.value.photoUrl)
    }

    @Test
    fun `state has no photoUrl when lookup finds no candidate`() {
        val savedStateHandle = SavedStateHandle(mapOf("scannedPayload" to "REG-123"))

        val viewModel = viewModel(savedStateHandle)

        assertNull(viewModel.uiState.value.photoUrl)
    }

    @Test
    fun `onMarkAttendance without a resolved candidate surfaces an error and never calls the use case`() =
        runTest {
            val savedStateHandle = SavedStateHandle(mapOf("scannedPayload" to "REG-123"))
            val viewModel = viewModel(savedStateHandle)

            viewModel.onMarkAttendance()

            val state = viewModel.markAttendanceState.value
            assertTrue(state is MarkAttendanceUiState.Error)
            coVerify(exactly = 0) { markAttendance(any(), any(), any()) }
        }

    @Test
    fun `onMarkAttendance calls the use case with paperId and resolved candidateId, then emits attendanceMarked`() =
        runTest {
            coEvery { lookupCandidate("REG-123") } returns Candidate(
                id = "can_1",
                examNumber = "REG-123",
                fullName = "Johnathan Doe",
            )
            coEvery { markAttendance("p1", "can_1", AttendanceStatus.SignedIn) } returns
                MarkAttendanceResult.Success(
                    Attendance(
                        paperId = "p1",
                        candidateId = "can_1",
                        status = AttendanceStatus.SignedIn,
                        markedAt = 0L,
                        syncStatus = SyncStatus.Pending,
                        syncError = null,
                    ),
                )
            val savedStateHandle =
                SavedStateHandle(mapOf("paperId" to "p1", "scannedPayload" to "REG-123"))
            val viewModel = viewModel(savedStateHandle)

            var emitted = false
            val job = launch { viewModel.attendanceMarked.collect { emitted = true } }
            // attendanceMarked is a replay=0 SharedFlow, and this collector runs on
            // runTest's own dispatcher (separate from the Unconfined Main dispatcher
            // the ViewModel's coroutines run on) — advance so `collect` actually
            // subscribes before onMarkAttendance() emits, or the emission is missed.
            advanceUntilIdle()

            viewModel.onMarkAttendance()
            advanceUntilIdle()

            assertTrue(emitted)
            assertEquals(MarkAttendanceUiState.Idle, viewModel.markAttendanceState.value)
            job.cancel()
        }

    @Test
    fun `onMarkAttendance surfaces the use case's error message on failure`() = runTest {
        coEvery { lookupCandidate("REG-123") } returns Candidate(
            id = "can_1",
            examNumber = "REG-123",
            fullName = "Johnathan Doe",
        )
        coEvery { markAttendance(any(), any(), any()) } returns
            MarkAttendanceResult.Error("Paper and candidate are required.")
        val savedStateHandle =
            SavedStateHandle(mapOf("paperId" to "p1", "scannedPayload" to "REG-123"))
        val viewModel = viewModel(savedStateHandle)

        viewModel.onMarkAttendance()

        val state = viewModel.markAttendanceState.value
        assertTrue(state is MarkAttendanceUiState.Error)
        assertEquals(
            "Paper and candidate are required.",
            (state as MarkAttendanceUiState.Error).message,
        )
    }

    @Test
    fun `state reports isSignedIn once lookup resolves an already-signed-in candidate`() = runTest {
        coEvery { lookupCandidate("REG-123") } returns Candidate(
            id = "can_1",
            examNumber = "REG-123",
            fullName = "Johnathan Doe",
        )
        coEvery { getAttendanceStatus("p1", "can_1") } returns AttendanceStatus.SignedIn
        val savedStateHandle =
            SavedStateHandle(mapOf("paperId" to "p1", "scannedPayload" to "REG-123"))

        val viewModel = viewModel(savedStateHandle)

        assertTrue(viewModel.uiState.value.isSignedIn)
    }

    @Test
    fun `onMarkAttendance signs out a candidate who is currently signed in`() = runTest {
        coEvery { lookupCandidate("REG-123") } returns Candidate(
            id = "can_1",
            examNumber = "REG-123",
            fullName = "Johnathan Doe",
        )
        coEvery { getAttendanceStatus("p1", "can_1") } returns AttendanceStatus.SignedIn
        coEvery { markAttendance("p1", "can_1", AttendanceStatus.SignedOut) } returns
            MarkAttendanceResult.Success(
                Attendance(
                    paperId = "p1",
                    candidateId = "can_1",
                    status = AttendanceStatus.SignedOut,
                    markedAt = 0L,
                    syncStatus = SyncStatus.Pending,
                    syncError = null,
                ),
            )
        val savedStateHandle =
            SavedStateHandle(mapOf("paperId" to "p1", "scannedPayload" to "REG-123"))
        val viewModel = viewModel(savedStateHandle)

        viewModel.onMarkAttendance()

        coVerify(exactly = 1) { markAttendance("p1", "can_1", AttendanceStatus.SignedOut) }
        assertTrue(!viewModel.uiState.value.isSignedIn)
    }

    @Test
    fun `onMarkAttendance signs in a candidate who is not currently signed in`() = runTest {
        coEvery { lookupCandidate("REG-123") } returns Candidate(
            id = "can_1",
            examNumber = "REG-123",
            fullName = "Johnathan Doe",
        )
        coEvery { getAttendanceStatus("p1", "can_1") } returns AttendanceStatus.SignedOut
        coEvery { markAttendance("p1", "can_1", AttendanceStatus.SignedIn) } returns
            MarkAttendanceResult.Success(
                Attendance(
                    paperId = "p1",
                    candidateId = "can_1",
                    status = AttendanceStatus.SignedIn,
                    markedAt = 0L,
                    syncStatus = SyncStatus.Pending,
                    syncError = null,
                ),
            )
        val savedStateHandle =
            SavedStateHandle(mapOf("paperId" to "p1", "scannedPayload" to "REG-123"))
        val viewModel = viewModel(savedStateHandle)

        viewModel.onMarkAttendance()

        coVerify(exactly = 1) { markAttendance("p1", "can_1", AttendanceStatus.SignedIn) }
        assertTrue(viewModel.uiState.value.isSignedIn)
    }

    @Test
    fun `dismissMarkAttendanceError resets state to Idle`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("scannedPayload" to "REG-123"))
        val viewModel = viewModel(savedStateHandle)
        viewModel.onMarkAttendance()

        viewModel.dismissMarkAttendanceError()

        assertEquals(MarkAttendanceUiState.Idle, viewModel.markAttendanceState.value)
    }
}
