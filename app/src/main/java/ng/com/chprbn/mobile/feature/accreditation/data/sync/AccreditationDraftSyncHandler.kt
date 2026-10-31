package ng.com.chprbn.mobile.feature.accreditation.data.sync

import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.accreditation.data.local.AssessmentDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationSyncRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus
import ng.com.chprbn.mobile.core.sync.Clock
import javax.inject.Inject

/**
 * Uploads pending accreditation assessment drafts via Fake remote in Phase 1.
 * Entity key = assessment draft id.
 */
class AccreditationDraftSyncHandler @Inject constructor(
    private val draftDao: AssessmentDraftDao,
    private val remote: AccreditationSyncRemoteSource,
    private val clock: Clock,
) : SyncEntityHandler {

    override suspend fun uploadBatch(entityKeys: List<String>): Map<String, SyncOutcome> {
        val outcomes = LinkedHashMap<String, SyncOutcome>(entityKeys.size)
        for (key in entityKeys) {
            val entity = draftDao.getById(key)
            if (entity == null) {
                outcomes[key] = SyncOutcome.Drop
                continue
            }
            try {
                val ok = remote.upload(key)
                if (ok) {
                    draftDao.updateStatus(
                        id = key,
                        status = DraftPersistenceStatus.Synced.name,
                        updatedAt = clock.nowMillis(),
                    )
                    outcomes[key] = SyncOutcome.Success
                } else {
                    draftDao.updateStatus(
                        id = key,
                        status = DraftPersistenceStatus.SyncFailed.name,
                        updatedAt = clock.nowMillis(),
                    )
                    outcomes[key] = SyncOutcome.Failure("Upload rejected for $key")
                }
            } catch (t: Throwable) {
                draftDao.updateStatus(
                    id = key,
                    status = DraftPersistenceStatus.SyncFailed.name,
                    updatedAt = clock.nowMillis(),
                )
                outcomes[key] = SyncOutcome.Failure(t.message ?: "Upload failed")
            }
        }
        return outcomes
    }
}
