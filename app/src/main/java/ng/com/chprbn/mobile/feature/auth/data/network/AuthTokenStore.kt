package ng.com.chprbn.mobile.feature.auth.data.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the active Sanctum token for [AuthorizationInterceptor].
 *
 * Backed by [EncryptedSharedPreferences] (AES-256-GCM) so the token stays
 * encrypted at rest. Every read / write / clear is wrapped in try/catch —
 * the AndroidX Security library can throw on devices with a corrupted or
 * regenerated Android KeyStore (https://issuetracker.google.com/issues/158234058
 * and cousins). On such a device we prefer "no session" to a crash: the
 * user is routed back to login, the app keeps running (A4 audit fix).
 */
@Singleton
class AuthTokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    @Volatile
    private var prefs: SharedPreferences? = null

    private fun openPrefs(): SharedPreferences? {
        prefs?.let { return it }
        return synchronized(this) {
            prefs ?: runCatching {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                EncryptedSharedPreferences.create(
                    context,
                    "auth_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                )
            }.onSuccess { prefs = it }
                .onFailure { Log.w(TAG, "EncryptedSharedPreferences unavailable: ${it.message}", it) }
                .getOrNull()
        }
    }

    fun setToken(value: String?) {
        if (value == null) {
            clear()
            return
        }
        runCatching {
            openPrefs()?.edit()?.putString(KEY_ACCESS_TOKEN, value)?.apply()
        }.onFailure {
            Log.w(TAG, "setToken failed: ${it.message}", it)
        }
    }

    fun peekToken(): String? = runCatching {
        openPrefs()?.getString(KEY_ACCESS_TOKEN, null)
    }.getOrNull()

    fun clear() {
        runCatching {
            openPrefs()?.edit()?.remove(KEY_ACCESS_TOKEN)?.apply()
        }.onFailure {
            Log.w(TAG, "clear failed: ${it.message}", it)
        }
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val TAG = "AuthTokenStore"
    }
}
