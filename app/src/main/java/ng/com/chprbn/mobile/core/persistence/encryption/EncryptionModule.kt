package ng.com.chprbn.mobile.core.persistence.encryption

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EncryptionModule {

    @Provides
    @Singleton
    @DatabaseKeyPrefs
    fun provideDatabaseKeyPrefs(
        @ApplicationContext context: Context
    ): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            "db_keys",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    @Singleton
    fun provideSupportFactory(
        keyProvider: DatabaseKeyProvider
    ): SupportFactory {
        // clearPassphrase = false: SQLCipher otherwise zeroes the passphrase byte
        // array after first use, which corrupts subsequent reopens within the same
        // process (a known SQLCipher footgun).
        return SupportFactory(keyProvider.getOrCreatePassphrase(), null, false)
    }
}
