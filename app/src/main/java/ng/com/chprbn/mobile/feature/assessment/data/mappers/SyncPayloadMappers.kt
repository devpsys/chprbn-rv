package ng.com.chprbn.mobile.feature.assessment.data.mappers

import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalScoreSyncRequestDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ProjectScoreSyncRequestDto
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore

/**
 * Domain → request DTO for the sync engine. Matches verification's
 * `SyncPayloadMappers.kt` layout: one extension per outbound payload, all
 * in one file because the file is small and the related functions are
 * easier to find together.
 *
 * No `syncStatus` / `syncError` cross the wire — those are mobile-only
 * bookkeeping written to local rows after a server response arrives.
 */
internal fun PracticalScore.toSyncRequestDto(): PracticalScoreSyncRequestDto =
    PracticalScoreSyncRequestDto(
        scheduleId = scheduleId,
        candidateId = candidateId,
        questionId = questionId,
        score = score,
        scoredAt = scoredAt,
    )

internal fun ProjectScore.toSyncRequestDto(): ProjectScoreSyncRequestDto =
    ProjectScoreSyncRequestDto(
        scheduleId = scheduleId,
        candidateId = candidateId,
        score = score,
        maxScore = maxScore,
        scoredAt = scoredAt,
    )
