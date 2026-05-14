package ng.com.chprbn.mobile.feature.exam.data.mappers

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
