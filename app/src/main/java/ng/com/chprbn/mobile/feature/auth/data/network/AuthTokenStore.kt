package ng.com.chprbn.mobile.feature.auth.data.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the active Sanctum token for [AuthorizationInterceptor].
 * Uses EncryptedSharedPreferences to securely persist the token.
 */
@Singleton
class AuthTokenStore @Inject constructor(
    @ApplicationContext context: Context
) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun setToken(value: String?) {
        if (value == null) {
            clear()
        } else {
            sharedPreferences.edit().putString("access_token", value).apply()
        }
    }

    fun peekToken(): String? = sharedPreferences.getString("access_token", null)

    fun clear() {
        sharedPreferences.edit().remove("access_token").apply()
    }
}
