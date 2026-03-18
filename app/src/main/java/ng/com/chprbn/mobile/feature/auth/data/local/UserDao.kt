package ng.com.chprbn.mobile.feature.auth.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertUser(user: UserEntity): Long

    @Query("SELECT * FROM auth_user LIMIT 1")
    fun getUser(): UserEntity?

    @Query("DELETE FROM auth_user")
    fun clearUser(): Int
}

