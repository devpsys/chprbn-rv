package ng.com.chprbn.mobile.feature.assessment.domain.repository

import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult

/**
 * Triggers a one-shot pass of the assessment-side sync queue (practical
 * scores + project scores). Returns a [SyncBatchResult] counter so the UI
 * can surface attempted / succeeded / failed in a snackbar or the
 * statistics screen.
 *
 * The actual work is dispatched through the shared `core.sync.SyncWorker`
 * via the feature's contributed `SyncEntityHandler` bindings; this repo is
 * the thin domain seam ViewModels call into.
 */
interface AssessmentSyncRepository {
    suspend fun syncPending(): SyncBatchResult
}
