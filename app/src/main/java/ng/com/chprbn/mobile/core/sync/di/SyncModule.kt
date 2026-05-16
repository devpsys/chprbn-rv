package ng.com.chprbn.mobile.core.sync.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncDatabase
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import javax.inject.Singleton

/**
 * Hilt graph for the cross-feature sync engine.
 *
 * - Provides the encrypted `sync.db` and its single DAO.
 * - Declares an empty [SyncEntityHandler] multibinding map so the
 *   [ng.com.chprbn.mobile.core.sync.SyncBatchRunner] can inject `Map<SyncEntityType, SyncEntityHandler>`
 *   even before any feature contributes a handler (Dagger requires the map to
 *   exist; `@Multibinds` makes the empty case legal).
 *
 * Feature modules contribute handlers with:
 * ```
 * @Binds @IntoMap @SyncEntityTypeKey(SyncEntityType.Attendance)
 * abstract fun bindAttendanceHandler(impl: AttendanceSyncHandler): SyncEntityHandler
 * ```
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Multibinds
    abstract fun syncEntityHandlers(): Map<SyncEntityType, SyncEntityHandler>

    companion object {

        @Provides
        @Singleton
        fun provideSyncDatabase(
            @ApplicationContext context: Context,
            supportFactory: SupportOpenHelperFactory,
        ): SyncDatabase =
            Room.databaseBuilder(context, SyncDatabase::class.java, "sync.db")
                .openHelperFactory(supportFactory)
                .build()

        @Provides
        @Singleton
        fun provideSyncJobDao(db: SyncDatabase): SyncJobDao = db.syncJobDao()

        @Provides
        @Singleton
        fun provideClock(): Clock = Clock.System
    }
}
