package ng.com.chprbn.mobile.feature.assessment.data.mappers

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
