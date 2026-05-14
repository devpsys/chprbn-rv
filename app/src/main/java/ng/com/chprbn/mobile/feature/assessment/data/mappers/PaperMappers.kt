package ng.com.chprbn.mobile.feature.assessment.data.mappers

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
