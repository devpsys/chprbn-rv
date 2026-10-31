package ng.com.chprbn.mobile.feature.accreditation.data.source

import android.util.Log
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AccreditationHubData
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssessmentDecision
import ng.com.chprbn.mobile.feature.accreditation.domain.model.InstitutionProfile
import ng.com.chprbn.mobile.feature.accreditation.domain.model.LocalDraftListItem

/**
 * Prefer live API; fall back to Fake when the backend returns null / throws
 * (accreditation endpoints are TO BE IMPLEMENTED — keeps screens demoable).
 */
class CompositeAccreditationAssignmentRemoteSource(
    private val primary: AccreditationAssignmentRemoteSource,
    private val fallback: AccreditationAssignmentRemoteSource,
) : AccreditationAssignmentRemoteSource {
    override suspend fun getHub(): AccreditationHubData =
        runCatching { primary.getHub() }
            .onFailure { Log.w(TAG, "Assignments API failed — using Fake", it) }
            .getOrElse { fallback.getHub() }

    private companion object {
        const val TAG = "AccredAssignRemote"
    }
}

class CompositeInstitutionRemoteSource(
    private val primary: InstitutionRemoteSource,
    private val fallback: InstitutionRemoteSource,
) : InstitutionRemoteSource {
    override suspend fun getProfile(institutionId: String): InstitutionProfile? =
        primary.getProfile(institutionId) ?: fallback.getProfile(institutionId)
}

class CompositeAccreditationDraftRemoteSource(
    private val primary: AccreditationDraftRemoteSource,
    private val fallback: AccreditationDraftRemoteSource,
) : AccreditationDraftRemoteSource {
    override suspend fun getSeedBundle(institutionId: String): AssessmentSeedBundle? =
        primary.getSeedBundle(institutionId) ?: fallback.getSeedBundle(institutionId)

    override suspend fun sampleLocalDrafts(): List<LocalDraftListItem> =
        primary.sampleLocalDrafts().ifEmpty { fallback.sampleLocalDrafts() }
}

/**
 * Sync writes: try API first; Fake fallback keeps offline/demo sync UX green
 * until the backend ships (same Composite pattern as license lookup).
 */
class CompositeAccreditationSyncRemoteSource(
    private val primary: AccreditationSyncRemoteSource,
    private val fallback: AccreditationSyncRemoteSource,
) : AccreditationSyncRemoteSource {
    override suspend fun uploadDraft(payload: AccreditationDraftSyncPayload): Boolean =
        runCatching { primary.uploadDraft(payload) }
            .onFailure { Log.w(TAG, "Draft sync API failed — using Fake", it) }
            .getOrElse { fallback.uploadDraft(payload) }

    private companion object {
        const val TAG = "AccredSyncRemote"
    }
}

class CompositeAssessmentSubmissionRemoteSource(
    private val primary: AssessmentSubmissionRemoteSource,
    private val fallback: AssessmentSubmissionRemoteSource,
) : AssessmentSubmissionRemoteSource {
    override suspend fun submit(assessmentId: String): String =
        runCatching { primary.submit(assessmentId) }
            .onFailure { Log.w(TAG, "Submit API failed — using Fake", it) }
            .getOrElse { fallback.submit(assessmentId) }

    override suspend fun getDecision(decisionId: String): AssessmentDecision? =
        primary.getDecision(decisionId) ?: fallback.getDecision(decisionId)

    private companion object {
        const val TAG = "AccredSubmitRemote"
    }
}
