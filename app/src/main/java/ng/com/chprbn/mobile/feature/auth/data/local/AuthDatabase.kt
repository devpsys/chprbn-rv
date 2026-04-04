package ng.com.chprbn.mobile.feature.auth.data.local

import androidx.room.Database
import ng.com.chprbn.mobile.core.persistence.converters.JsonStringListTypeConverter
import androidx.room.TypeConverters
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(JsonStringListTypeConverter::class)
abstract class AuthDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

