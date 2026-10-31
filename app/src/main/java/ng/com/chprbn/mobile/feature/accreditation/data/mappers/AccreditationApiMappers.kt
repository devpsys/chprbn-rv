package ng.com.chprbn.mobile.feature.accreditation.data.mappers

import com.google.gson.Gson
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationAssignedInstitutionDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationAssignmentsDataDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationAttachmentMetaDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationCriteriaDraftDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationCurrentAssessmentDataDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationDecisionDataDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationDraftDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationDraftSyncRequestDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationFeatureGridDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationHistoryItemDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationInstitutionDataDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationOfficerDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationScoreDraftDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationSectionDraftDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationSectionSyncItemDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationUpcomingInstitutionDto
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationDraftSyncPayload
import ng.com.chprbn.mobile.feature.accreditation.data.source.AssessmentSeedBundle
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AccreditationHubData
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssessmentDecision
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssessmentDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssessmentHistoryItem
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssignedInstitution
import ng.com.chprbn.mobile.feature.accreditation.domain.model.CriteriaDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.model.FeatureGridInfo
import ng.com.chprbn.mobile.feature.accreditation.domain.model.InstitutionAssignmentSyncStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.model.InstitutionProfile
import ng.com.chprbn.mobile.feature.accreditation.domain.model.OfficerBanner
import ng.com.chprbn.mobile.feature.accreditation.domain.model.ScoreDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionKey
import ng.com.chprbn.mobile.feature.accreditation.domain.model.UpcomingInstitution

private val gson = Gson()

fun AccreditationAssignmentsDataDto.toDomain(): AccreditationHubData =
    AccreditationHubData(
        officer = officer.toDomain(),
        featureGrid = featureGrid.toDomain(),
        assignedInstitutions = assignedInstitutions.map { it.toDomain() },
        upcomingInstitutions = upcomingInstitutions.map { it.toDomain() },
    )

private fun AccreditationOfficerDto?.toDomain(): OfficerBanner =
    OfficerBanner(
        initials = this?.initials.orEmpty().ifBlank { "—" },
        displayName = this?.displayName.orEmpty().ifBlank { "Accreditation Officer" },
        registrationLabel = this?.registrationLabel.orEmpty(),
        isVerified = this?.isVerified ?: false,
    )

private fun AccreditationFeatureGridDto?.toDomain(): FeatureGridInfo =
    FeatureGridInfo(
        startSubtitle = this?.startSubtitle.orEmpty(),
        continueSubtitle = this?.continueSubtitle.orEmpty(),
        draftsSubtitle = this?.draftsSubtitle.orEmpty(),
        syncSubtitle = this?.syncSubtitle.orEmpty(),
        isOnline = this?.isOnline ?: true,
    )

private fun AccreditationAssignedInstitutionDto.toDomain(): AssignedInstitution =
    AssignedInstitution(
        id = id,
        name = name,
        locationLabel = locationLabel.orEmpty(),
        protocolLabel = protocolLabel.orEmpty(),
        lastSyncedLabel = lastSyncedLabel.orEmpty(),
        syncStatus = syncStatus.toInstitutionSyncStatus(),
        isActive = isActive ?: true,
    )

private fun AccreditationUpcomingInstitutionDto.toDomain(): UpcomingInstitution =
    UpcomingInstitution(
        id = id,
        name = name,
        scheduleLabel = scheduleLabel.orEmpty(),
    )

fun AccreditationInstitutionDataDto.toDomain(): InstitutionProfile =
    InstitutionProfile(
        id = id,
        name = name,
        ownershipSubtitle = ownershipSubtitle.orEmpty(),
        photoUrl = photoUrl,
        syncStatus = syncStatus.toInstitutionSyncStatus(),
        lastSyncedLabel = lastSyncedLabel.orEmpty(),
        facilityCode = facilityCode.orEmpty(),
        lastVisitLabel = lastVisitLabel.orEmpty(),
        ownershipType = ownershipType.orEmpty(),
        state = state.orEmpty(),
        zone = zone.orEmpty(),
        campusAddress = campusAddress.orEmpty(),
        programmes = programmes,
        programmesAccreditedCount = programmesAccreditedCount ?: programmes.size,
        academicStaffCount = academicStaffCount ?: 0,
        technicalStaffCount = technicalStaffCount ?: 0,
        adminStaffCount = adminStaffCount ?: 0,
        techToAcademicRatio = techToAcademicRatio.orEmpty(),
        capitalBaseLabel = capitalBaseLabel.orEmpty(),
        history = history.map { it.toDomain() },
    )

private fun AccreditationHistoryItemDto.toDomain(): AssessmentHistoryItem =
    AssessmentHistoryItem(
        id = id,
        cycleLabel = cycleLabel.orEmpty(),
        dateLabel = dateLabel.orEmpty(),
        scoreLabel = scoreLabel.orEmpty(),
        gradeLabel = gradeLabel.orEmpty(),
        decisionLabel = decisionLabel.orEmpty(),
    )

fun AccreditationCurrentAssessmentDataDto.toSeedBundle(): AssessmentSeedBundle {
    val draftRecord = draft.toDomain()
    val sectionRecords = sections.map { it.toDomain(draftRecord.id) }
    val criteriaRecord = criteria.toDomain(draftRecord.id)
    val scoresRecord = scores.toDomain(draftRecord.id)
    return AssessmentSeedBundle(draftRecord, sectionRecords, criteriaRecord, scoresRecord)
}

private fun AccreditationDraftDto.toDomain(): AssessmentDraftRecord =
    AssessmentDraftRecord(
        id = id,
        institutionId = institutionId,
        institutionName = institutionName.orEmpty(),
        cycleLabel = cycleLabel.orEmpty(),
        facilityCode = facilityCode.orEmpty(),
        locationLabel = locationLabel.orEmpty(),
        status = status.orEmpty().toDraftPersistenceStatus(),
        updatedAt = updatedAt ?: 0L,
    )

private fun AccreditationSectionDraftDto.toDomain(assessmentId: String): SsqSectionDraftRecord {
    val key = SsqSectionKey.fromKey(sectionKey)
    return SsqSectionDraftRecord(
        assessmentId = assessmentId,
        sectionKey = key,
        codeLabel = codeLabel ?: "Section ${key.key}",
        rangeLabel = rangeLabel.orEmpty(),
        title = title.orEmpty(),
        subtitle = subtitle.orEmpty(),
        status = status.orEmpty().toDraftPersistenceStatus(),
        payloadJson = payloadJson.orEmpty(),
        itemsCompleted = itemsCompleted ?: 0,
        totalItems = totalItems ?: 0,
    )
}

private fun AccreditationCriteriaDraftDto?.toDomain(assessmentId: String): CriteriaDraftRecord =
    CriteriaDraftRecord(
        assessmentId = assessmentId,
        payloadJson = this?.payloadJson.orEmpty(),
        evaluatedCount = this?.evaluatedCount ?: 0,
        totalCriteria = this?.totalCriteria ?: 0,
        status = this?.status.orEmpty().toDraftPersistenceStatus(),
    )

private fun AccreditationScoreDraftDto?.toDomain(assessmentId: String): ScoreDraftRecord =
    ScoreDraftRecord(
        assessmentId = assessmentId,
        payloadJson = this?.payloadJson.orEmpty(),
        completedCount = this?.completedCount ?: 0,
        totalGroups = this?.totalGroups ?: 0,
        status = this?.status.orEmpty().toDraftPersistenceStatus(),
    )

fun AccreditationDecisionDataDto.toDomain(): AssessmentDecision {
    val rowsJson = rawScoreRowsJson
        ?: rawScoreRows?.let { gson.toJson(it) }
        ?: "[]"
    val condJson = conditionsJson
        ?: conditions?.let { gson.toJson(it) }
        ?: "[]"
    return AssessmentDecision(
        decisionId = decisionId,
        institutionId = institutionId,
        institutionName = institutionName.orEmpty(),
        locationLabel = locationLabel.orEmpty(),
        facilityCode = facilityCode.orEmpty(),
        assessmentDateLabel = assessmentDateLabel.orEmpty(),
        gradeLabel = gradeLabel.orEmpty(),
        decisionLabel = decisionLabel.orEmpty(),
        accreditationPeriod = accreditationPeriod.orEmpty(),
        overallScorePercentLabel = overallScorePercentLabel.orEmpty(),
        studentQuotaLabel = studentQuotaLabel.orEmpty(),
        receiptRef = receiptRef.orEmpty(),
        rawScoreRowsJson = rowsJson,
        conditionsJson = condJson,
    )
}

fun AccreditationDraftSyncPayload.toRequestDto(): AccreditationDraftSyncRequestDto =
    AccreditationDraftSyncRequestDto(
        institutionId = institutionId,
        cycleLabel = cycleLabel,
        facilityCode = facilityCode,
        locationLabel = locationLabel,
        sections = sections.map { section ->
            AccreditationSectionSyncItemDto(
                sectionKey = section.sectionKey,
                status = section.status,
                payloadJson = section.payloadJson,
                itemsCompleted = section.itemsCompleted,
                totalItems = section.totalItems,
                codeLabel = section.codeLabel,
                rangeLabel = section.rangeLabel,
                title = section.title,
                subtitle = section.subtitle,
            )
        },
        criteria = criteriaPayloadJson?.let {
            AccreditationCriteriaDraftDto(
                payloadJson = it,
                evaluatedCount = criteriaEvaluatedCount,
                totalCriteria = criteriaTotal,
                status = criteriaStatus,
            )
        },
        scores = scoresPayloadJson?.let {
            AccreditationScoreDraftDto(
                payloadJson = it,
                completedCount = scoresCompletedCount,
                totalGroups = scoresTotalGroups,
                status = scoresStatus,
            )
        },
        attachments = attachments.map { meta ->
            AccreditationAttachmentMetaDto(
                id = meta.id,
                parentKey = meta.parentKey,
                title = meta.title,
                isPhoto = meta.isPhoto,
                sizeLabel = meta.sizeLabel,
                locationLabel = meta.locationLabel,
                uploadState = meta.uploadState,
            )
        },
        updatedAt = updatedAt,
    )

private fun String?.toInstitutionSyncStatus(): InstitutionAssignmentSyncStatus =
    when (this?.trim()?.lowercase()) {
        "synced" -> InstitutionAssignmentSyncStatus.Synced
        "draft_pending_sync", "draftpendingsync", "pending" ->
            InstitutionAssignmentSyncStatus.DraftPendingSync
        "sync_failed", "syncfailed", "failed" ->
            InstitutionAssignmentSyncStatus.SyncFailed
        "never_synced", "neversynced" -> InstitutionAssignmentSyncStatus.NeverSynced
        else -> InstitutionAssignmentSyncStatus.NeverSynced
    }
