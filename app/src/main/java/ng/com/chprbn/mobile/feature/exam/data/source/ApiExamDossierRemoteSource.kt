package ng.com.chprbn.mobile.feature.exam.data.source

import android.util.Log
import ng.com.chprbn.mobile.feature.exam.data.api.ExamDossierApiService
import ng.com.chprbn.mobile.feature.exam.data.dto.ScheduleDto
import ng.com.chprbn.mobile.feature.exam.data.mappers.toDomain
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject

/**
 * Retrofit-backed dossier source. Bound directly (no Fake fallback) by
 * `ExamDataModule.provideExamDossierRemoteSource` in every build type.
 *
 * Contract:
 *
 * - **Transport / envelope failure** (network throws, HTTP non-2xx,
 *   envelope `success:false`, unmappable center): throws
 *   [IllegalStateException] with a short message, surfaced to the officer
 *   as a real error rather than silently synthetic data.
 * - **Successful-but-empty** (HTTP 200 with a missing / null `data.center`):
 *   returns `null`, logged via [Log.w] since this used to be able to
 *   trigger a silent [CompositeExamDossierRemoteSource] fallback to
 *   [FakeExamDossierRemoteSource] before that path was disabled.
 * - **Successful with a valid centre**: returns the mapped bundle.
 *
 * Candidates and paper↔candidate assignments live nested per
 * [ng.com.chprbn.mobile.feature.exam.data.dto.ScheduleDto] on the wire,
 * not as flat top-level arrays — this flattens every schedule's
 * `candidates`/`paper_candidates` into the bundle's flat lists, deduping
 * candidates by id (the same person can appear in more than one
 * schedule) and deriving each paper's [Paper.totalCandidates] from the
 * assignment list, since the wire never provides that count directly.
 * [Paper.startAt]/[Paper.endAt] are similarly derived — from whichever
 * schedule's `paper_candidates` first assigns a candidate to that paper,
 * via [parsedTimeWindow].
 *
 * Unmappable rows are dropped — logged via [Log.w] with before/after
 * counts (better to persist the partial dossier than to fail the whole
 * download over a single malformed row, but silently is no longer
 * acceptable — this is exactly the class of bug that produced a "3
 * papers and 3 candidates" success message for a 150-candidate
 * download).
 *
 * `data.sections` collapses to `Center.hasSections` (non-emptiness only —
 * element shape is unconfirmed, always empty in every response seen so
 * far) so the dashboard can gate the Practical Assessment card on it.
 *
 * `data.year` is copied onto [Center.year] — confirmed live per
 * `docs/mobile-api-guide.html` §4, required verbatim on every
 * `attendance/push-record` row (§5).
 */
class ApiExamDossierRemoteSource @Inject constructor(
    private val api: ExamDossierApiService,
) : ExamDossierRemoteSource {

    override suspend fun fetchDossier(): ExamDossierBundle? {
        val response = try {
            api.fetchDossier()
        } catch (e: IOException) {
            error("Network error fetching exam dossier: ${e.message ?: e.javaClass.simpleName}")
        }
        if (!response.isSuccessful) {
            error("Exam dossier request failed: HTTP ${response.code()} ${response.message()}")
        }
        val envelope = response.body()
            ?: error("Exam dossier response had an empty body.")
        if (!envelope.success) {
            error(envelope.message ?: "Exam dossier request was rejected.")
        }
        val data = envelope.data
        if (data == null) {
            Log.w(TAG, "Dossier envelope had success=true but a null data block — returning null.")
            return null
        }
        val center = data.center?.toDomain()?.copy(
            hasSections = !data.sections.isNullOrEmpty(),
            year = data.year,
        )
        if (center == null) {
            Log.w(
                TAG,
                "Dossier response had no usable center (raw payload carried " +
                    "${data.papers?.size ?: 0} paper(s), ${data.schedules?.size ?: 0} " +
                    "schedule(s)) — returning null instead of persisting them.",
            )
            return null
        }

        val schedules = data.schedules.orEmpty()

        val rawAssignmentCount = schedules.sumOf { it.paperCandidates?.size ?: 0 }
        val assignments = schedules.flatMap { schedule ->
            schedule.paperCandidates.orEmpty().mapNotNull { it.toDomain(schedule.id.orEmpty()) }
        }
        logDroppedRows("assignments", rawAssignmentCount, assignments.size)
        val assignmentCountByPaperId = assignments.groupingBy { it.paperId }.eachCount()

        val timeWindowByPaperId = mutableMapOf<String, Pair<Long, Long>>()
        schedules.forEach { schedule ->
            val window = schedule.parsedTimeWindow() ?: return@forEach
            schedule.paperCandidates.orEmpty().forEach { assignment ->
                val paperId = assignment.paperId ?: return@forEach
                timeWindowByPaperId.putIfAbsent(paperId, window)
            }
        }

        val rawPapers = data.papers.orEmpty()
        val papers = rawPapers.mapNotNull {
            it.toDomain(centerId = center.id)
        }.map { paper ->
            val window = timeWindowByPaperId[paper.id]
            paper.copy(
                totalCandidates = assignmentCountByPaperId[paper.id] ?: 0,
                startAt = window?.first ?: paper.startAt,
                endAt = window?.second ?: paper.endAt,
            )
        }
        logDroppedRows("papers", rawPapers.size, papers.size)

        val rawCandidates = schedules.flatMap { it.candidates.orEmpty() }
        val mappedCandidates = rawCandidates.mapNotNull { it.toDomain() }
        logDroppedRows("candidates", rawCandidates.size, mappedCandidates.size)
        val candidates = mappedCandidates.distinctBy { it.id }
        if (candidates.size < mappedCandidates.size) {
            Log.w(
                TAG,
                "Collapsed ${mappedCandidates.size - candidates.size} duplicate candidate " +
                    "row(s) appearing in more than one schedule.",
            )
        }

        return ExamDossierBundle(
            center = center,
            papers = papers,
            candidates = candidates,
            assignments = assignments,
        )
    }

    /**
     * Combines [ScheduleDto.testDate] with [ScheduleDto.startTime]/
     * [ScheduleDto.endTime] (confirmed `HH:mm`, 24h, e.g. `"08:00"`) into
     * an epoch-millis start/end pair. Returns `null` (and logs a warning)
     * rather than a guessed value on anything unparseable, or when any of
     * the three fields is absent.
     */
    private fun ScheduleDto.parsedTimeWindow(): Pair<Long, Long>? {
        val date = testDate ?: return null
        val start = startTime ?: return null
        val end = endTime ?: return null
        val zone = ZoneId.systemDefault()
        return try {
            val day = parseTestDate(date)
            val startAt = LocalDateTime.of(day, LocalTime.parse(start))
                .atZone(zone).toInstant().toEpochMilli()
            val endAt = LocalDateTime.of(day, LocalTime.parse(end))
                .atZone(zone).toInstant().toEpochMilli()
            startAt to endAt
        } catch (e: DateTimeParseException) {
            Log.w(
                TAG,
                "Unable to parse schedule '$id' time window " +
                    "(test_date=$date, start_time=$start, end_time=$end): ${e.message}",
            )
            null
        }
    }

    /**
     * `test_date` is confirmed against a real device response (2026-08-14)
     * to be a human-readable string with an ordinal day suffix, e.g.
     * `"Friday, 14th Aug 2026"` — not ISO `yyyy-MM-dd`. [ORDINAL_SUFFIX]
     * strips the `st`/`nd`/`rd`/`th` before parsing since [DateTimeFormatter]
     * has no built-in ordinal-day pattern letter.
     */
    private fun parseTestDate(raw: String): LocalDate {
        val normalized = ORDINAL_SUFFIX.replace(raw) { it.groupValues[1] }
        return LocalDate.parse(normalized, TEST_DATE_FORMATTER)
    }

    private fun logDroppedRows(label: String, rawCount: Int, mappedCount: Int) {
        if (mappedCount < rawCount) {
            Log.w(
                TAG,
                "Dropped ${rawCount - mappedCount} of $rawCount $label row(s) from the " +
                    "dossier response — each was missing a required id field on the wire.",
            )
        }
    }

    private companion object {
        const val TAG = "ExamDossier"
        val ORDINAL_SUFFIX = Regex("(\\d+)(st|nd|rd|th)", RegexOption.IGNORE_CASE)
        val TEST_DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale.ENGLISH)
    }
}
