package ng.com.chprbn.mobile.feature.accreditation.presentation

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AccreditationHubData
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssessmentDecision
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssessmentOverview
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.model.InstitutionAssignmentSyncStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.model.InstitutionProfile
import ng.com.chprbn.mobile.feature.accreditation.domain.model.LocalDraftListItem
import ng.com.chprbn.mobile.feature.accreditation.domain.model.LocalDraftSyncState as DomainLocalDraftSyncState
import ng.com.chprbn.mobile.feature.accreditation.domain.model.OverallSyncStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionKey as DomainSsqSectionKey
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SyncStatusSnapshot

internal val AccreditationGson: Gson = Gson()

fun DraftPersistenceStatus.toSectionDraftStatus(): SectionDraftStatus = when (this) {
    DraftPersistenceStatus.NotStarted -> SectionDraftStatus.NotStarted
    DraftPersistenceStatus.InProgress -> SectionDraftStatus.InProgress
    DraftPersistenceStatus.SavedLocally -> SectionDraftStatus.SavedLocally
    DraftPersistenceStatus.PendingSync -> SectionDraftStatus.PendingSync
    DraftPersistenceStatus.Synced -> SectionDraftStatus.Synced
    DraftPersistenceStatus.SyncFailed -> SectionDraftStatus.SyncFailed
}

fun SectionDraftStatus.toDomain(): DraftPersistenceStatus = when (this) {
    SectionDraftStatus.NotStarted -> DraftPersistenceStatus.NotStarted
    SectionDraftStatus.InProgress -> DraftPersistenceStatus.InProgress
    SectionDraftStatus.SavedLocally -> DraftPersistenceStatus.SavedLocally
    SectionDraftStatus.PendingSync -> DraftPersistenceStatus.PendingSync
    SectionDraftStatus.Synced -> DraftPersistenceStatus.Synced
    SectionDraftStatus.SyncFailed -> DraftPersistenceStatus.SyncFailed
}

fun DomainSsqSectionKey.toPresentation(): SsqSectionKey = when (this) {
    DomainSsqSectionKey.A -> SsqSectionKey.A
    DomainSsqSectionKey.B -> SsqSectionKey.B
    DomainSsqSectionKey.C -> SsqSectionKey.C
    DomainSsqSectionKey.D -> SsqSectionKey.D
    DomainSsqSectionKey.E -> SsqSectionKey.E
    DomainSsqSectionKey.F -> SsqSectionKey.F
}

fun SsqSectionKey.toDomain(): DomainSsqSectionKey = when (this) {
    SsqSectionKey.A -> DomainSsqSectionKey.A
    SsqSectionKey.B -> DomainSsqSectionKey.B
    SsqSectionKey.C -> DomainSsqSectionKey.C
    SsqSectionKey.D -> DomainSsqSectionKey.D
    SsqSectionKey.E -> DomainSsqSectionKey.E
    SsqSectionKey.F -> DomainSsqSectionKey.F
}

fun InstitutionAssignmentSyncStatus.toPresentation(): InstitutionSyncStatus = when (this) {
    InstitutionAssignmentSyncStatus.Synced -> InstitutionSyncStatus.Synced
    InstitutionAssignmentSyncStatus.DraftPendingSync -> InstitutionSyncStatus.DraftPendingSync
    InstitutionAssignmentSyncStatus.SyncFailed -> InstitutionSyncStatus.SyncFailed
    InstitutionAssignmentSyncStatus.NeverSynced -> InstitutionSyncStatus.NeverSynced
}

fun AccreditationHubData.toUiState(): AccreditationHubUiState = AccreditationHubUiState(
    isLoading = false,
    officer = OfficerBannerUiState(
        initials = officer.initials,
        displayName = officer.displayName,
        registrationLabel = officer.registrationLabel,
        isVerified = officer.isVerified,
    ),
    featureGrid = FeatureGridUiState(
        startSubtitle = featureGrid.startSubtitle,
        continueSubtitle = featureGrid.continueSubtitle,
        draftsSubtitle = featureGrid.draftsSubtitle,
        syncSubtitle = featureGrid.syncSubtitle,
        isOnline = featureGrid.isOnline,
    ),
    assignedInstitutions = assignedInstitutions.map {
        AssignedInstitutionUiState(
            id = it.id,
            name = it.name,
            locationLabel = it.locationLabel,
            protocolLabel = it.protocolLabel,
            lastSyncedLabel = it.lastSyncedLabel,
            syncStatus = it.syncStatus.toPresentation(),
            isActive = it.isActive,
        )
    },
    upcomingInstitutions = upcomingInstitutions.map {
        UpcomingInstitutionUiState(
            id = it.id,
            name = it.name,
            scheduleLabel = it.scheduleLabel,
        )
    },
)

fun InstitutionProfile.toUiData(): InstitutionProfileData = InstitutionProfileData(
    id = id,
    name = name,
    ownershipSubtitle = ownershipSubtitle,
    photoUrl = photoUrl,
    syncStatus = syncStatus.toPresentation(),
    lastSyncedLabel = lastSyncedLabel,
    facilityCode = facilityCode,
    lastVisitLabel = lastVisitLabel,
    ownershipType = ownershipType,
    state = state,
    zone = zone,
    campusAddress = campusAddress,
    programmes = programmes,
    programmesAccreditedCount = programmesAccreditedCount,
    academicStaffCount = academicStaffCount,
    technicalStaffCount = technicalStaffCount,
    adminStaffCount = adminStaffCount,
    techToAcademicRatio = techToAcademicRatio,
    capitalBaseLabel = capitalBaseLabel,
    history = history.map {
        AccreditationHistoryItemUiState(
            id = it.id,
            cycleLabel = it.cycleLabel,
            dateLabel = it.dateLabel,
            scoreLabel = it.scoreLabel,
            gradeLabel = it.gradeLabel,
            decisionLabel = it.decisionLabel,
        )
    },
)

fun AssessmentOverview.toUiData(): AssessmentOverviewData = AssessmentOverviewData(
    institutionId = institutionId,
    institutionName = institutionName,
    locationLabel = locationLabel,
    facilityCode = facilityCode,
    cycleLabel = cycleLabel,
    sections = sections.map {
        SsqSectionSummaryUiState(
            key = it.key.toPresentation(),
            codeLabel = it.codeLabel,
            rangeLabel = it.rangeLabel,
            title = it.title,
            subtitle = it.subtitle,
            status = it.status.toSectionDraftStatus(),
            itemsCompleted = it.itemsCompleted,
            totalItems = it.totalItems,
        )
    },
)

fun LocalDraftListItem.toUi(): LocalDraftItemUi = LocalDraftItemUi(
    id = id,
    institutionId = institutionId,
    institutionName = institutionName,
    assignmentCode = assignmentCode,
    sectionsCompleted = sectionsCompleted,
    totalSections = totalSections,
    attachmentsCount = attachmentsCount,
    lastModifiedLabel = lastModifiedLabel,
    syncState = when (syncState) {
        DomainLocalDraftSyncState.PendingSync -> LocalDraftSyncState.PendingSync
        DomainLocalDraftSyncState.Syncing -> LocalDraftSyncState.Syncing
        DomainLocalDraftSyncState.Synced -> LocalDraftSyncState.Synced
        DomainLocalDraftSyncState.SyncFailed -> LocalDraftSyncState.SyncFailed
    },
    attemptCount = attemptCount,
    lastRawError = lastRawError,
)

fun SyncStatusSnapshot.toUiState(): SyncStatusUiState = SyncStatusUiState(
    isLoading = false,
    overallStatus = when (overallStatus) {
        OverallSyncStatus.Synced -> OverallSyncStatusUi.Synced
        OverallSyncStatus.Syncing -> OverallSyncStatusUi.Syncing
        OverallSyncStatus.SyncFailed -> OverallSyncStatusUi.SyncFailed
        OverallSyncStatus.Offline -> OverallSyncStatusUi.Offline
    },
    progressFraction = progressFraction,
    lastSuccessfulSyncLabel = lastSuccessfulSyncLabel,
    failedDrafts = failedDrafts.map { it.draft.toUi() },
)

fun AssessmentDecision.toUiData(): AssessmentResultData {
    val rows: List<AssessmentResultScoreRow> = runCatching {
        val type = object : TypeToken<List<AssessmentResultScoreRow>>() {}.type
        AccreditationGson.fromJson<List<AssessmentResultScoreRow>>(rawScoreRowsJson, type)
    }.getOrDefault(emptyList())
    val conditions: List<String> = runCatching {
        val type = object : TypeToken<List<String>>() {}.type
        AccreditationGson.fromJson<List<String>>(conditionsJson, type)
    }.getOrDefault(emptyList())
    return AssessmentResultData(
        institutionId = institutionId,
        decisionId = decisionId,
        institutionName = institutionName,
        locationLabel = locationLabel,
        facilityCode = facilityCode,
        assessmentDateLabel = assessmentDateLabel,
        grade = when (gradeLabel) {
            "Satisfactory" -> AccreditationServerGrade.Satisfactory
            "ReVisit" -> AccreditationServerGrade.ReVisit
            "Denial" -> AccreditationServerGrade.Denial
            else -> AccreditationServerGrade.Excellent
        },
        decision = when (decisionLabel) {
            "ProvisionalConditional" -> AccreditationServerDecision.ProvisionalConditional
            "Denial" -> AccreditationServerDecision.Denial
            "Revocation" -> AccreditationServerDecision.Revocation
            "Deferral" -> AccreditationServerDecision.Deferral
            else -> AccreditationServerDecision.FullAccreditation
        },
        accreditationPeriod = accreditationPeriod,
        overallScorePercentLabel = overallScorePercentLabel,
        studentQuotaLabel = studentQuotaLabel,
        receiptRef = receiptRef,
        rawScoreRows = rows,
        conditions = conditions,
    )
}

fun SsqSectionFormData.toPayloadJson(): String = AccreditationGson.toJson(this)

fun parseSsqSectionFormData(json: String?): SsqSectionFormData? {
    if (json.isNullOrBlank()) return null
    return runCatching { AccreditationGson.fromJson(json, SsqSectionFormData::class.java) }.getOrNull()
}

fun CriteriaChecklistData.toPayloadJson(): String = AccreditationGson.toJson(this)

fun parseCriteriaChecklistData(json: String?): CriteriaChecklistData? {
    if (json.isNullOrBlank()) return null
    return runCatching { AccreditationGson.fromJson(json, CriteriaChecklistData::class.java) }.getOrNull()
}

fun ScoreEntryData.toPayloadJson(): String = AccreditationGson.toJson(this)

fun parseScoreEntryData(json: String?): ScoreEntryData? {
    if (json.isNullOrBlank()) return null
    return runCatching { AccreditationGson.fromJson(json, ScoreEntryData::class.java) }.getOrNull()
}
