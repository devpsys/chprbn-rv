package ng.com.chprbn.mobile.core.persistence.encryption

import javax.inject.Qualifier

/**
 * Hilt qualifier for the EncryptedSharedPreferences instance that holds the
 * SQLCipher database passphrase. Distinct from [auth_prefs] so the auth-token
 * storage and the DB-key storage cannot accidentally collide on a key name.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DatabaseKeyPrefs
