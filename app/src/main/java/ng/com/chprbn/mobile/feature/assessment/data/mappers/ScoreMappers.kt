package ng.com.chprbn.mobile.feature.assessment.data.mappers

import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreEntity
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreEntity
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore

internal fun PracticalScoreEntity.toDomain(): PracticalScore = PracticalScore(
    scheduleId = scheduleId,
    candidateId = candidateId,
    questionId = questionId,
    score = score,
    scoredAt = scoredAt,
    syncStatus = syncStatus.toSyncStatus(),
    syncError = syncError,
)

internal fun PracticalScore.toEntity(): PracticalScoreEntity = PracticalScoreEntity(
    scheduleId = scheduleId,
    candidateId = candidateId,
    questionId = questionId,
    score = score,
    scoredAt = scoredAt,
    syncStatus = syncStatus.toDbValue(),
    syncError = syncError,
)

internal fun ProjectScoreEntity.toDomain(): ProjectScore = ProjectScore(
    scheduleId = scheduleId,
    candidateId = candidateId,
    score = score,
    maxScore = maxScore,
    scoredAt = scoredAt,
    syncStatus = syncStatus.toSyncStatus(),
    syncError = syncError,
)

internal fun ProjectScore.toEntity(): ProjectScoreEntity = ProjectScoreEntity(
    scheduleId = scheduleId,
    candidateId = candidateId,
    score = score,
    maxScore = maxScore,
    scoredAt = scoredAt,
    syncStatus = syncStatus.toDbValue(),
    syncError = syncError,
)
