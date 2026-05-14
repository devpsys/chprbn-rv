package ng.com.chprbn.mobile.feature.assessment.data.mappers

import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.data.dto.AssessmentScheduleDto
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentScheduleEntity
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSchedule

internal fun AssessmentScheduleEntity.toDomain(): AssessmentSchedule = AssessmentSchedule(
    id = id,
    title = title,
    date = date,
    paperKind = paperKind.toPaperKind(),
    centerId = centerId,
    syncStatus = syncStatus.toSyncStatus(),
)

internal fun AssessmentSchedule.toEntity(): AssessmentScheduleEntity = AssessmentScheduleEntity(
    id = id,
    title = title,
    date = date,
    paperKind = paperKind.toDbValue(),
    centerId = centerId,
    syncStatus = syncStatus.toDbValue(),
)

/**
 * Materialises a server-side schedule row into the local entity. The
 * server doesn't know about local sync state, so `syncStatus` starts at
 * `Synced` — score writes against this schedule later flip it via
 * `AssessmentScheduleDao.updateSyncStatus`.
 *
 * Returns `null` when [id] is missing; the caller (the repository) should
 * drop unmappable rows rather than persisting placeholders.
 */
internal fun AssessmentScheduleDto.toEntity(): AssessmentScheduleEntity? {
    val safeId = id?.takeIf { it.isNotBlank() } ?: return null
    return AssessmentScheduleEntity(
        id = safeId,
        title = title.orEmpty(),
        date = date ?: 0L,
        paperKind = paperKind.orEmpty().toPaperKind().toDbValue(),
        centerId = centerId.orEmpty(),
        syncStatus = SyncStatus.Synced.toDbValue(),
    )
}
