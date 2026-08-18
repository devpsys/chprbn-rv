package ng.com.chprbn.mobile.feature.auth.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import ng.com.chprbn.mobile.core.persistence.converters.JsonStringListTypeConverter
import androidx.room.TypeConverters
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class],
    version = 10,
    exportSchema = true,
    autoMigrations = [
        // v7 → v8: adds nullable passwordSalt / passwordVerifier / passwordAlgorithm
        // columns for the offline-login PBKDF2 credential.
        AutoMigration(from = 7, to = 8),
        // v8 → v9: adds nullable `location` column mirroring the
        // `AdhocProfileDataDto.location` wire field.
        AutoMigration(from = 8, to = 9),
        // v9 → v10: adds nullable `phone`, `status`, `assessorId` columns so
        // the sync layer can build the `assessor` block that the three
        // push-record endpoints now require. See UserEntity's doc.
        AutoMigration(from = 9, to = 10),
    ],
)
@TypeConverters(JsonStringListTypeConverter::class)
abstract class AuthDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

