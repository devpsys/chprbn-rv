package ng.com.chprbn.mobile.feature.assessment.data.mappers

import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalSectionDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.SectionQuestionDto
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

internal fun PracticalSectionDto.toEntity(): PracticalSectionEntity? {
    val safeId = id?.takeIf { it.isNotBlank() } ?: return null
    val safeScheduleId = scheduleId?.takeIf { it.isNotBlank() } ?: return null
    return PracticalSectionEntity(
        id = safeId,
        scheduleId = safeScheduleId,
        title = title.orEmpty(),
        subtitle = subtitle.orEmpty(),
        ordering = ordering ?: 0,
    )
}

internal fun SectionQuestionDto.toEntity(): SectionQuestionEntity? {
    val safeId = id?.takeIf { it.isNotBlank() } ?: return null
    val safeSectionId = sectionId?.takeIf { it.isNotBlank() } ?: return null
    return SectionQuestionEntity(
        id = safeId,
        sectionId = safeSectionId,
        number = number ?: 0,
        prompt = prompt.orEmpty(),
        imageUrl = imageUrl,
        maxScore = maxScore ?: 0,
    )
}
