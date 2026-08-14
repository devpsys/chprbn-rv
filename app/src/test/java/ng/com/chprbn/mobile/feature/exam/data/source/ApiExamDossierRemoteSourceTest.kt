package ng.com.chprbn.mobile.feature.exam.data.source

import android.util.Log
import com.google.gson.JsonPrimitive
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.feature.exam.data.api.ExamDossierApiService
import ng.com.chprbn.mobile.feature.exam.data.dto.CandidateDto
import ng.com.chprbn.mobile.feature.exam.data.dto.CenterDto
import ng.com.chprbn.mobile.feature.exam.data.dto.ExamDossierDataDto
import ng.com.chprbn.mobile.feature.exam.data.dto.ExamDossierEnvelopeDto
import ng.com.chprbn.mobile.feature.exam.data.dto.PaperCandidateAssignmentDto
import ng.com.chprbn.mobile.feature.exam.data.dto.PaperDto
import ng.com.chprbn.mobile.feature.exam.data.dto.ScheduleDto
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * Regression coverage for flattening the live wire shape — candidates
 * and paper↔candidate assignments live nested per [ScheduleDto], not as
 * flat top-level arrays (confirmed against a real device response,
 * 2026-08-14) — plus the row-drop and null-center paths that made "3
 * papers and 3 candidates" indistinguishable from a real download vs. a
 * silent [FakeExamDossierRemoteSource] fallback — see
 * [CompositeExamDossierRemoteSource]'s doc comment.
 */
class ApiExamDossierRemoteSourceTest {

    private val api = mockk<ExamDossierApiService>()
    private val source = ApiExamDossierRemoteSource(api)

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `flattens schedule-nested candidates and assignments, deriving each paper's candidate count`() = runTest {
        coEvery { api.fetchDossier() } returns Response.success(
            envelope(
                center = CenterDto(id = "ctr_1", name = "Centre", location = "Here"),
                papers = listOf(PaperDto(id = "pap_1", code = "P1", name = "PAPER 1")),
                schedules = listOf(
                    ScheduleDto(
                        id = "sch_1",
                        testCode = "CHEW",
                        paperCandidates = listOf(
                            PaperCandidateAssignmentDto(paperId = "pap_1", candidateId = "can_1"),
                        ),
                        candidates = listOf(
                            CandidateDto(id = "can_1", examNumber = "B/1", fullName = "A"),
                            // in the roster but not assigned to any paper in this sample
                            CandidateDto(id = "can_2", examNumber = "B/2", fullName = "B"),
                        ),
                    ),
                ),
            ),
        )

        val bundle = source.fetchDossier()

        assertEquals(1, bundle?.papers?.size)
        assertEquals(1, bundle?.papers?.single()?.totalCandidates)
        assertEquals(2, bundle?.candidates?.size)
        assertEquals(1, bundle?.assignments?.size)
    }

    @Test
    fun `combines candidates and assignments across multiple schedules`() = runTest {
        coEvery { api.fetchDossier() } returns Response.success(
            envelope(
                center = CenterDto(id = "ctr_1"),
                papers = listOf(PaperDto(id = "pap_1"), PaperDto(id = "pap_2")),
                schedules = listOf(
                    ScheduleDto(
                        id = "sch_1",
                        paperCandidates = listOf(
                            PaperCandidateAssignmentDto(paperId = "pap_1", candidateId = "can_1"),
                        ),
                        candidates = listOf(CandidateDto(id = "can_1")),
                    ),
                    ScheduleDto(
                        id = "sch_2",
                        paperCandidates = listOf(
                            PaperCandidateAssignmentDto(paperId = "pap_2", candidateId = "can_2"),
                            PaperCandidateAssignmentDto(paperId = "pap_2", candidateId = "can_3"),
                        ),
                        candidates = listOf(CandidateDto(id = "can_2"), CandidateDto(id = "can_3")),
                    ),
                ),
            ),
        )

        val bundle = source.fetchDossier()

        assertEquals(3, bundle?.candidates?.size)
        assertEquals(3, bundle?.assignments?.size)
        val pap1 = bundle?.papers?.single { it.id == "pap_1" }
        val pap2 = bundle?.papers?.single { it.id == "pap_2" }
        assertEquals(1, pap1?.totalCandidates)
        assertEquals(2, pap2?.totalCandidates)
    }

    @Test
    fun `collapses the same candidate appearing in more than one schedule`() = runTest {
        coEvery { api.fetchDossier() } returns Response.success(
            envelope(
                center = CenterDto(id = "ctr_1"),
                schedules = listOf(
                    ScheduleDto(id = "sch_1", candidates = listOf(CandidateDto(id = "can_1", fullName = "First"))),
                    ScheduleDto(id = "sch_2", candidates = listOf(CandidateDto(id = "can_1", fullName = "First"))),
                ),
            ),
        )

        val bundle = source.fetchDossier()

        assertEquals(1, bundle?.candidates?.size)
    }

    @Test
    fun `drops candidate rows missing an id instead of failing the whole download`() = runTest {
        coEvery { api.fetchDossier() } returns Response.success(
            envelope(
                center = CenterDto(id = "ctr_1"),
                schedules = listOf(
                    ScheduleDto(
                        id = "sch_1",
                        candidates = listOf(
                            CandidateDto(id = "can_1"),
                            CandidateDto(id = null),
                            CandidateDto(id = "   "),
                        ),
                    ),
                ),
            ),
        )

        val bundle = source.fetchDossier()

        assertEquals(1, bundle?.candidates?.size)
    }

    @Test
    fun `returns null instead of a bundle when the center is missing`() = runTest {
        coEvery { api.fetchDossier() } returns Response.success(
            envelope(
                center = null,
                schedules = listOf(ScheduleDto(id = "sch_1", candidates = listOf(CandidateDto(id = "can_1")))),
            ),
        )

        assertNull(source.fetchDossier())
    }

    @Test
    fun `returns null when data is missing entirely`() = runTest {
        coEvery { api.fetchDossier() } returns Response.success(
            ExamDossierEnvelopeDto(success = true, data = null),
        )

        assertNull(source.fetchDossier())
    }

    @Test
    fun `sets center hasSections true when the sections array is non-empty`() = runTest {
        coEvery { api.fetchDossier() } returns Response.success(
            envelope(
                center = CenterDto(id = "ctr_1"),
                sections = listOf(JsonPrimitive("placeholder")),
            ),
        )

        assertEquals(true, source.fetchDossier()?.center?.hasSections)
    }

    @Test
    fun `sets center hasSections false when the sections array is empty or absent`() = runTest {
        coEvery { api.fetchDossier() } returns Response.success(
            envelope(center = CenterDto(id = "ctr_1"), sections = emptyList()),
        )

        assertEquals(false, source.fetchDossier()?.center?.hasSections)
    }

    private fun envelope(
        center: CenterDto? = CenterDto(id = "ctr_1"),
        papers: List<PaperDto> = emptyList(),
        schedules: List<ScheduleDto> = emptyList(),
        sections: List<com.google.gson.JsonElement>? = null,
    ) = ExamDossierEnvelopeDto(
        success = true,
        data = ExamDossierDataDto(
            center = center,
            papers = papers,
            schedules = schedules,
            sections = sections,
        ),
    )
}
