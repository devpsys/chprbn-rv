package ng.com.chprbn.mobile.feature.exam.domain.repository

import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.feature.exam.domain.model.DownloadDossierResult

/**
 * Two operations that span the network:
 *
 * - [downloadDossier] — destructive download of the day's package
 *   (papers + candidates + assignments), gated by the UI's warning prompt.
 * - [syncPending] — user-initiated "Sync Now"; runs one batch of the
 *   shared `core.sync` queue and returns the counter for snackbar
 *   surfacing.
 */
interface ExamSyncRepository {

    suspend fun downloadDossier(): DownloadDossierResult

    suspend fun syncPending(): SyncBatchResult
}
