package ng.com.chprbn.mobile.feature.exam.domain.repository

import kotlinx.coroutines.flow.Flow
import ng.com.chprbn.mobile.feature.exam.domain.model.CachedRecordEntry

/**
 * Read side for the Cached Records screen. Aggregates the four
 * sync-tracked tables (`attendance`, `remarks`, `practical_scores`,
 * `project_scores`) into a single live list per tab.
 *
 * `observePending()` returns rows with `syncStatus == Pending` — items
 * captured on-device that haven't been uploaded yet.
 * `observeFailed()` returns `Failed + Abandoned` rows — items the
 * server rejected or the sync engine gave up on after MAX_ATTEMPTS.
 *
 * Sorted with the most-recently-captured row first so an officer sees
 * their latest work at the top.
 */
interface CachedRecordsRepository {

    fun observePending(): Flow<List<CachedRecordEntry>>

    fun observeFailed(): Flow<List<CachedRecordEntry>>
}
