package ng.com.chprbn.mobile.feature.accreditation.data.source

import kotlinx.coroutines.delay
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
import ng.com.chprbn.mobile.feature.accreditation.domain.model.LocalDraftListItem
import ng.com.chprbn.mobile.feature.accreditation.domain.model.LocalDraftSyncState
import ng.com.chprbn.mobile.feature.accreditation.domain.model.OfficerBanner
import ng.com.chprbn.mobile.feature.accreditation.domain.model.ScoreDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionKey
import ng.com.chprbn.mobile.feature.accreditation.domain.model.UpcomingInstitution
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory remotes matching presentation sample data (ABUTH, etc.).
 * Used as Composite fallback while accreditation HTTP endpoints are TO BE IMPLEMENTED.
 */
@Singleton
class FakeAccreditationRemoteSource @Inject constructor() :
    AccreditationAssignmentRemoteSource,
    InstitutionRemoteSource,
    AccreditationDraftRemoteSource,
    AccreditationSyncRemoteSource,
    AssessmentSubmissionRemoteSource {

    override suspend fun getHub(): AccreditationHubData {
        delay(80L)
        return AccreditationHubData(
            officer = OfficerBanner(
                initials = "DB",
                displayName = "Insp. Officer: D. Bello",
                registrationLabel = "CHPRBN REG #OFF-9942",
                isVerified = true,
            ),
            featureGrid = FeatureGridInfo(
                startSubtitle = "Protocol 2024/25",
                continueSubtitle = "Resume section 4 of 9",
                draftsSubtitle = "Ready for sync pipeline",
                syncSubtitle = "Last synced 5m ago",
                isOnline = true,
            ),
            assignedInstitutions = listOf(
                AssignedInstitution(
                    id = "inst-abuth",
                    name = "Ahmadu Bello University Teaching Hospital",
                    locationLabel = "Zaria, Kaduna • North West",
                    protocolLabel = "Accreditation Cycle 2024/2025",
                    lastSyncedLabel = "Today, 09:30 AM",
                    syncStatus = InstitutionAssignmentSyncStatus.Synced,
                    isActive = true,
                ),
            ),
            upcomingInstitutions = listOf(
                UpcomingInstitution(
                    id = "inst-niht",
                    name = "National Institute of Health Technology",
                    scheduleLabel = "Scheduled dispatch • Inspection window starts tomorrow",
                ),
            ),
        )
    }

    override suspend fun getProfile(institutionId: String): InstitutionProfile? {
        delay(60L)
        val id = institutionId.ifBlank { "inst-abuth" }
        if (id != "inst-abuth" && !KNOWN_INSTITUTIONS.containsKey(id)) {
            // Still return ABUTH-shaped sample for unknown ids so Continue path works.
        }
        val meta = KNOWN_INSTITUTIONS[id] ?: KNOWN_INSTITUTIONS.getValue("inst-abuth")
        return InstitutionProfile(
            id = id,
            name = meta.name,
            ownershipSubtitle = "Federal Government • Tertiary Healthcare Institution",
            photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC1qI9Ud3fZEDO0iyzmhjCCZVNGnU8kNBgZt9WLsOmq-dc9HdUIODS3YA1blshavLdSvZw1W4_CEVBr5XHZ5-F6mVargiw2VcD97vgnJaynUsU0xOOfiKi1CtV5pMoD4ssLwZ4U1NHfbqHWl-SGbo_P-Z892UwsVEAEXl2ecf5Lps6xClIrHg3zscWYoW27gyYBFn-jpveuZtmFAQFAKZs9V5-Ya5qKD9deyNtAtmp5",
            syncStatus = InstitutionAssignmentSyncStatus.Synced,
            lastSyncedLabel = "09:30 AM",
            facilityCode = meta.facilityCode,
            lastVisitLabel = "14 Nov 2021",
            ownershipType = "Federal Government",
            state = meta.state,
            zone = meta.zone,
            campusAddress = meta.campusAddress,
            programmes = listOf(
                "BCHS / CHO",
                "CHEW",
                "HND Health Tech",
                "JCHEW",
                "ND Health Info Management",
            ),
            programmesAccreditedCount = 5,
            academicStaffCount = 128,
            technicalStaffCount = 42,
            adminStaffCount = 85,
            techToAcademicRatio = "1:3.0",
            capitalBaseLabel = "₦450,000,000",
            history = listOf(
                AssessmentHistoryItem(
                    id = "hist-2021",
                    cycleLabel = "Cycle 2021 / 2022",
                    dateLabel = "14 Nov 2021",
                    scoreLabel = "84.5%",
                    gradeLabel = "Grade A",
                    decisionLabel = "Full Accreditation (5 Years)",
                ),
                AssessmentHistoryItem(
                    id = "hist-2016",
                    cycleLabel = "Cycle 2016 / 2017",
                    dateLabel = "20 Oct 2016",
                    scoreLabel = "78.0%",
                    gradeLabel = "Grade B",
                    decisionLabel = "Full Accreditation (5 Years)",
                ),
            ),
        )
    }

    override suspend fun getSeedBundle(institutionId: String): AssessmentSeedBundle? {
        val id = institutionId.ifBlank { "inst-abuth" }
        val meta = KNOWN_INSTITUTIONS[id] ?: KNOWN_INSTITUTIONS.getValue("inst-abuth")
        val assessmentId = "assess-$id"
        val now = System.currentTimeMillis()
        val draft = AssessmentDraftRecord(
            id = assessmentId,
            institutionId = id,
            institutionName = meta.name,
            cycleLabel = "Cycle 2024/2025",
            facilityCode = meta.facilityCodeShort,
            locationLabel = meta.locationShort,
            status = DraftPersistenceStatus.SavedLocally,
            updatedAt = now,
        )
        val sections = SECTION_META.map { metaRow ->
            SsqSectionDraftRecord(
                assessmentId = assessmentId,
                sectionKey = metaRow.key,
                codeLabel = metaRow.codeLabel,
                rangeLabel = metaRow.rangeLabel,
                title = metaRow.title,
                subtitle = metaRow.subtitle,
                status = DraftPersistenceStatus.SavedLocally,
                payloadJson = "",
                itemsCompleted = metaRow.totalItems,
                totalItems = metaRow.totalItems,
            )
        }
        val criteria = CriteriaDraftRecord(
            assessmentId = assessmentId,
            payloadJson = "",
            evaluatedCount = 8,
            totalCriteria = 8,
            status = DraftPersistenceStatus.NotStarted,
        )
        val scores = ScoreDraftRecord(
            assessmentId = assessmentId,
            payloadJson = "",
            completedCount = 6,
            totalGroups = 6,
            status = DraftPersistenceStatus.NotStarted,
        )
        return AssessmentSeedBundle(draft, sections, criteria, scores)
    }

    override suspend fun sampleLocalDrafts(): List<LocalDraftListItem> = listOf(
        LocalDraftListItem(
            id = "assess-inst-abuth",
            institutionId = "inst-abuth",
            institutionName = "Ahmadu Bello University Teaching Hospital",
            assignmentCode = "CHPRBN-NW-2024-08",
            sectionsCompleted = 5,
            totalSections = 6,
            attachmentsCount = 14,
            lastModifiedLabel = "Today, 10:15 AM",
            syncState = LocalDraftSyncState.PendingSync,
        ),
        LocalDraftListItem(
            id = "assess-inst-minna",
            institutionId = "inst-minna",
            institutionName = "School of Health Technology, Minna",
            assignmentCode = "CHPRBN-NC-2024-12",
            sectionsCompleted = 3,
            totalSections = 6,
            attachmentsCount = 8,
            lastModifiedLabel = "Yesterday, 04:40 PM",
            syncState = LocalDraftSyncState.SyncFailed,
            attemptCount = 2,
            lastRawError =
                "Failed to reach https://registry.chprbn.gov.ng:8443/api/v1/sync : " +
                    "SSLHandshakeException at 192.168.1.104",
        ),
        LocalDraftListItem(
            id = "assess-inst-lagos",
            institutionId = "inst-lagos",
            institutionName = "Lagos State College of Health Technology",
            assignmentCode = "CHPRBN-SW-2024-04",
            sectionsCompleted = 6,
            totalSections = 6,
            attachmentsCount = 19,
            lastModifiedLabel = "22 Oct 2024",
            syncState = LocalDraftSyncState.Synced,
        ),
    )

    override suspend fun uploadDraft(payload: AccreditationDraftSyncPayload): Boolean {
        delay(200L)
        return payload.assessmentId.isNotBlank()
    }

    override suspend fun submit(assessmentId: String): String {
        delay(400L)
        val institutionId = assessmentId.removePrefix("assess-").ifBlank { "inst-abuth" }
        return "dec-$institutionId-2024"
    }

    override suspend fun getDecision(decisionId: String): AssessmentDecision? {
        delay(80L)
        if (decisionId.isBlank()) return null
        val institutionId = decisionId
            .removePrefix("dec-")
            .removeSuffix("-2024")
            .ifBlank { "inst-abuth" }
        val meta = KNOWN_INSTITUTIONS[institutionId] ?: KNOWN_INSTITUTIONS.getValue("inst-abuth")
        return AssessmentDecision(
            decisionId = decisionId,
            institutionId = institutionId,
            institutionName = meta.name,
            locationLabel = meta.locationShort,
            facilityCode = meta.facilityCode,
            assessmentDateLabel = "24 Oct 2024",
            gradeLabel = "Excellent",
            decisionLabel = "FullAccreditation",
            accreditationPeriod = "5 Years Full Accreditation",
            overallScorePercentLabel = "88.0%",
            studentQuotaLabel = "120",
            receiptRef = "REC-2024-ABU-8829-SYNC",
            rawScoreRowsJson = DEFAULT_SCORE_ROWS_JSON,
            conditionsJson = DEFAULT_CONDITIONS_JSON,
        )
    }

    private data class InstitutionMeta(
        val name: String,
        val facilityCode: String,
        val facilityCodeShort: String,
        val locationShort: String,
        val state: String,
        val zone: String,
        val campusAddress: String,
    )

    private data class SectionMeta(
        val key: SsqSectionKey,
        val codeLabel: String,
        val rangeLabel: String,
        val title: String,
        val subtitle: String,
        val totalItems: Int,
    )

    companion object {
        private val KNOWN_INSTITUTIONS = mapOf(
            "inst-abuth" to InstitutionMeta(
                name = "Ahmadu Bello University Teaching Hospital",
                facilityCode = "CHPRBN-NW-2024-08",
                facilityCodeShort = "ABUTH-ZR-049",
                locationShort = "Zaria, Kaduna State",
                state = "Kaduna State",
                zone = "North West",
                campusAddress = "Main Campus, Shika, Zaria",
            ),
            "inst-minna" to InstitutionMeta(
                name = "School of Health Technology, Minna",
                facilityCode = "CHPRBN-NC-2024-12",
                facilityCodeShort = "SHT-MN-012",
                locationShort = "Minna, Niger State",
                state = "Niger State",
                zone = "North Central",
                campusAddress = "Tunga, Minna",
            ),
            "inst-lagos" to InstitutionMeta(
                name = "Lagos State College of Health Technology",
                facilityCode = "CHPRBN-SW-2024-04",
                facilityCodeShort = "LSCHT-LG-004",
                locationShort = "Yaba, Lagos State",
                state = "Lagos State",
                zone = "South West",
                campusAddress = "Yaba Campus, Lagos",
            ),
            "inst-sabon" to InstitutionMeta(
                name = "College of Health Sciences & Tech, Sabon Gari",
                facilityCode = "CHPRBN-NW-2024-19",
                facilityCodeShort = "CHST-SG-019",
                locationShort = "Sabon Gari, Kaduna State",
                state = "Kaduna State",
                zone = "North West",
                campusAddress = "Sabon Gari",
            ),
            "inst-niht" to InstitutionMeta(
                name = "National Institute of Health Technology",
                facilityCode = "CHPRBN-FC-2024-01",
                facilityCodeShort = "NIHT-FC-001",
                locationShort = "Abuja, FCT",
                state = "FCT",
                zone = "North Central",
                campusAddress = "Gwagwalada, Abuja",
            ),
        )

        private val SECTION_META = listOf(
            SectionMeta(
                SsqSectionKey.A, "Section A", "SSQ A1 + A2",
                "General Information", "24 mandatory baseline fields captured", 24,
            ),
            SectionMeta(
                SsqSectionKey.B, "Section B", "B1 + B2",
                "Philosophy & Governance", "B1 philosophy/objectives + B2 governance (20 items)", 20,
            ),
            SectionMeta(
                SsqSectionKey.C, "Section C", "C1",
                "Curriculum & Teaching Methods", "C1 teaching methods — 18 of 18 evaluated", 18,
            ),
            SectionMeta(
                SsqSectionKey.D, "Section D", "D1 - D3",
                "Staffing & Academic Administration", "Faculty credentials & license registry attached", 12,
            ),
            SectionMeta(
                SsqSectionKey.E, "Section E", "E1 - E3",
                "Physical Facilities, Labs & Technology", "Equipment, simulation centre & IT inventory", 40,
            ),
            SectionMeta(
                SsqSectionKey.F, "Section F", "F1",
                "Affiliated Clinical Facilities & Sites", "Community postings & MoU verification", 7,
            ),
        )

        private const val DEFAULT_SCORE_ROWS_JSON =
            """[{"sectionIndex":1,"groupLabel":"Group 1: Location & Land Area (§1)","rawScore":"9","maxPoints":10},{"sectionIndex":2,"groupLabel":"Group 2: Head of Department (§2)","rawScore":"10","maxPoints":10},{"sectionIndex":3,"groupLabel":"Group 3: Human Resources & Teaching Cadre (§3)","rawScore":"22","maxPoints":25},{"sectionIndex":4,"groupLabel":"Group 4: Practice Area & Clinical Postings (§5)","rawScore":"14","maxPoints":15},{"sectionIndex":5,"groupLabel":"Group 5: Physical Facilities & Labs (§6)","rawScore":"21","maxPoints":25},{"sectionIndex":6,"groupLabel":"Group 6: Capital Base & Welfare (§7 & §8)","rawScore":"13","maxPoints":15}]"""

        private const val DEFAULT_CONDITIONS_JSON =
            """["Maintain approved teacher-to-student clinical supervision ratio (1:8).","Renew affiliated health facility tripartite MoUs 6 months prior to expiry in Oct 2026.","Submit annual faculty continuing education (MCPDP) audit logbook."]"""
    }
}
