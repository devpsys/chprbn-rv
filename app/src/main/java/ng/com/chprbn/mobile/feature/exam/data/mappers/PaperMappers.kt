package ng.com.chprbn.mobile.feature.exam.data.mappers

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

/** Returns `null` when the wire payload omits `id`. */
internal fun PaperDto.toDomain(): Paper? {
    val safeId = id?.takeIf { it.isNotBlank() } ?: return null
    return Paper(
        id = safeId,
        centerId = centerId.orEmpty(),
        title = title.orEmpty(),
        subtitle = subtitle.orEmpty(),
        paperKind = paperKind.orEmpty().toPaperKind(),
        startAt = startAt ?: 0L,
        endAt = endAt ?: 0L,
        hall = hall.orEmpty(),
        totalCandidates = totalCandidates ?: 0,
    )
}
