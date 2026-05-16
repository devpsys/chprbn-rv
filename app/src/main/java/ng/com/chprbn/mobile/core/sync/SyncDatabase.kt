package ng.com.chprbn.mobile.core.sync

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Single-table database backing the cross-feature upload queue. Lives in
 * `sync.db` and is encrypted via the shared SQLCipher `SupportOpenHelperFactory`
 * from `core.persistence.encryption.EncryptionModule`.
 *
 * Kept separate from feature DBs (e.g. `exam.db`, `assessment.db`) so the
 * worker and feature repositories can depend on the queue without depending
 * on each other.
 */
@Database(
    entities = [SyncJobEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class SyncDatabase : RoomDatabase() {
    abstract fun syncJobDao(): SyncJobDao
}
