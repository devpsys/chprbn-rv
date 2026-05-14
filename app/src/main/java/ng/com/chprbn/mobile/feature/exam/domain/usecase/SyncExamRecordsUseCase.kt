package ng.com.chprbn.mobile.feature.exam.domain.usecase

import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamSyncRepository
import javax.inject.Inject

/**
 * User-initiated "Sync Now" gesture. Runs one cross-feature batch
 * through `core.sync.SyncBatchRunner` and surfaces the counter back to
 * the UI for snackbar / statistics rendering.
 */
class SyncExamRecordsUseCase @Inject constructor(
    private val repository: ExamSyncRepository,
) {
    suspend operator fun invoke(): SyncBatchResult = repository.syncPending()
}
