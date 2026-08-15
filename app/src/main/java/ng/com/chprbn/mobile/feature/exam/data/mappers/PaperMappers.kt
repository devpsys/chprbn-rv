package ng.com.chprbn.mobile.feature.exam.data.mappers

import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.feature.exam.data.dto.PaperDto
import ng.com.chprbn.mobile.feature.exam.data.local.PaperEntity
import ng.com.chprbn.mobile.feature.exam.domain.model.Paper

internal fun PaperEntity.toDomain(): Paper = Paper(
    id = id,
    centerId = centerId,
    title = title,
    subtitle = subtitle,
    paperKind = paperKind.toPaperKind(),
    startAt = startAt,
    endAt = endAt,
    hall = hall,
    totalCandidates = totalCandidates,
)

internal fun Paper.toEntity(): PaperEntity = PaperEntity(
    id = id,
    centerId = centerId,
    title = title,
    subtitle = subtitle,
    paperKind = paperKind.toDbValue(),
    startAt = startAt,
    endAt = endAt,
    hall = hall,
    totalCandidates = totalCandidates,
)

/**
 * Returns `null` when the wire payload omits `id`. [centerId] and
 * [totalCandidates] aren't on the wire for a paper — the caller supplies
 * [centerId] from the dossier's own centre and [totalCandidates] from
 * counting that paper's rows in the assignment list (see
 * `ApiExamDossierRemoteSource`). `startAt`/`endAt`/`hall` have no wire
 * signal on the dossier today and fall back to `0` / `""`.
 *
 * `paperKind` is derived from the paper's [PaperDto.code] via
 * [PaperKind.fromWireCode] — `"PE"` → `Practical` and `"PA"` → `Project`
 * feed the assessment feature's schedules list; everything else is
 * `Theory` and shows on the exam papers screen (see [PaperDao.getExamPapers]
 * / [PaperDao.getAssessmentPapers] for the SQL-side filter).
 */
internal fun PaperDto.toDomain(centerId: String, totalCandidates: Int = 0): Paper? {
    val safeId = id?.takeIf { it.isNotBlank() } ?: return null
    return Paper(
        id = safeId,
        centerId = centerId,
        title = name.orEmpty(),
        subtitle = code.orEmpty(),
        paperKind = PaperKind.fromWireCode(code),
        startAt = 0L,
        endAt = 0L,
        hall = "",
        totalCandidates = totalCandidates,
    )
}
