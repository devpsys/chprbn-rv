package ng.com.chprbn.mobile.feature.accreditation.data.mappers

import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationAssignedInstitutionDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationAssignmentsDataDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationDecisionDataDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationCurrentAssessmentDataDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationDraftDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationFeatureGridDto
import ng.com.chprbn.mobile.feature.accreditation.data.dto.AccreditationOfficerDto
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationDraftSyncPayload
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.model.InstitutionAssignmentSyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccreditationApiMappersTest {

    @Test
    fun `assignments dto maps hub fields`() {
        val hub = AccreditationAssignmentsDataDto(
            officer = AccreditationOfficerDto(
                initials = "DB",
                displayName = "Insp. Officer: D. Bello",
                registrationLabel = "CHPRBN REG #OFF-9942",
                isVerified = true,
            ),
            featureGrid = AccreditationFeatureGridDto(
                startSubtitle = "Protocol",
                continueSubtitle = "Resume",
                draftsSubtitle = "Drafts",
                syncSubtitle = "Synced",
                isOnline = true,
            ),
            assignedInstitutions = listOf(
                AccreditationAssignedInstitutionDto(
                    id = "inst-abuth",
                    name = "ABUTH",
                    locationLabel = "Zaria",
                    protocolLabel = "Cycle 2024/2025",
                    lastSyncedLabel = "Today",
                    syncStatus = "synced",
                    isActive = true,
                ),
            ),
        ).toDomain()

        assertEquals("DB", hub.officer.initials)
        assertEquals(1, hub.assignedInstitutions.size)
        assertEquals(InstitutionAssignmentSyncStatus.Synced, hub.assignedInstitutions[0].syncStatus)
    }

    @Test
    fun `decision dto prefers structured rows and conditions`() {
        val decision = AccreditationDecisionDataDto(
            decisionId = "dec-1",
            institutionId = "inst-abuth",
            institutionName = "ABUTH",
            gradeLabel = "Excellent",
            decisionLabel = "FullAccreditation",
            rawScoreRows = listOf(mapOf("rawScore" to "9")),
            conditions = listOf("Keep ratio 1:8"),
        ).toDomain()

        assertEquals("Excellent", decision.gradeLabel)
        assertTrue(decision.rawScoreRowsJson.contains("rawScore"))
        assertTrue(decision.conditionsJson.contains("Keep ratio"))
    }

    @Test
    fun `sync payload maps to request dto`() {
        val request = AccreditationDraftSyncPayload(
            assessmentId = "assess-1",
            institutionId = "inst-abuth",
            cycleLabel = "Cycle 2024/2025",
            facilityCode = "X",
            locationLabel = "Zaria",
            updatedAt = 1L,
            sections = emptyList(),
            criteriaPayloadJson = """{"ok":true}""",
            criteriaEvaluatedCount = 1,
            criteriaTotal = 8,
            criteriaStatus = "SavedLocally",
            scoresPayloadJson = null,
            scoresCompletedCount = 0,
            scoresTotalGroups = 6,
            scoresStatus = null,
            attachments = emptyList(),
        ).toRequestDto()

        assertEquals("inst-abuth", request.institutionId)
        assertEquals("""{"ok":true}""", request.criteria?.payloadJson)
        assertEquals(null, request.scores)
    }

    @Test
    fun `current assessment maps seed bundle with unknown status fallback`() {
        val bundle = AccreditationCurrentAssessmentDataDto(
            draft = AccreditationDraftDto(
                id = "assess-1",
                institutionId = "inst-abuth",
                institutionName = "ABUTH",
                status = "Nope",
            ),
            sections = emptyList(),
            criteria = null,
            scores = null,
        ).toSeedBundle()

        assertEquals("assess-1", bundle.draft.id)
        assertEquals(DraftPersistenceStatus.NotStarted, bundle.draft.status)
        assertEquals(DraftPersistenceStatus.NotStarted, bundle.criteria.status)
    }
}
