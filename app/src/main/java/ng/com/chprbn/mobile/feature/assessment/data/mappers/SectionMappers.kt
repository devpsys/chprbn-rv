package ng.com.chprbn.mobile.feature.assessment.data.mappers

import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalSectionEntity
import ng.com.chprbn.mobile.feature.assessment.data.local.SectionQuestionEntity
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSection
import ng.com.chprbn.mobile.feature.assessment.domain.model.SectionQuestion

internal fun PracticalSectionEntity.toDomain(): PracticalSection = PracticalSection(
    id = id,
    scheduleId = scheduleId,
    title = title,
    subtitle = subtitle,
    ordering = ordering,
)

internal fun PracticalSection.toEntity(): PracticalSectionEntity = PracticalSectionEntity(
    id = id,
    scheduleId = scheduleId,
    title = title,
    subtitle = subtitle,
    ordering = ordering,
)

internal fun SectionQuestionEntity.toDomain(): SectionQuestion = SectionQuestion(
    id = id,
    sectionId = sectionId,
    number = number,
    prompt = prompt,
    imageUrl = imageUrl,
    maxScore = maxScore,
)

internal fun SectionQuestion.toEntity(): SectionQuestionEntity = SectionQuestionEntity(
    id = id,
    sectionId = sectionId,
    number = number,
    prompt = prompt,
    imageUrl = imageUrl,
    maxScore = maxScore,
)
