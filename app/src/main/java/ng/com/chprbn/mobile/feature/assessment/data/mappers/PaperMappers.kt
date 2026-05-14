package ng.com.chprbn.mobile.feature.assessment.data.mappers

import ng.com.chprbn.mobile.feature.assessment.data.dto.AssessmentPaperDto
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentPaperEntity
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaper
import ng.com.chprbn.mobile.feature.assessment.domain.model.Facility
import ng.com.chprbn.mobile.feature.assessment.domain.model.Hall

internal fun AssessmentPaperEntity.toDomain(): AssessmentPaper = AssessmentPaper(
    scheduleId = scheduleId,
    title = title,
    statusLabel = statusLabel,
    facility = Facility(name = facilityName, address = facilityAddress),
    hall = Hall(name = hallName, address = hallAddress),
    heroImageUrl = heroImageUrl,
)

internal fun AssessmentPaper.toEntity(): AssessmentPaperEntity = AssessmentPaperEntity(
    scheduleId = scheduleId,
    title = title,
    statusLabel = statusLabel,
    facilityName = facility.name,
    facilityAddress = facility.address,
    hallName = hall.name,
    hallAddress = hall.address,
    heroImageUrl = heroImageUrl,
)

/** Returns `null` when the wire payload omits `schedule_id`. */
internal fun AssessmentPaperDto.toEntity(): AssessmentPaperEntity? {
    val safeScheduleId = scheduleId?.takeIf { it.isNotBlank() } ?: return null
    return AssessmentPaperEntity(
        scheduleId = safeScheduleId,
        title = title.orEmpty(),
        statusLabel = statusLabel.orEmpty(),
        facilityName = facilityName.orEmpty(),
        facilityAddress = facilityAddress.orEmpty(),
        hallName = hallName.orEmpty(),
        hallAddress = hallAddress.orEmpty(),
        heroImageUrl = heroImageUrl,
    )
}
