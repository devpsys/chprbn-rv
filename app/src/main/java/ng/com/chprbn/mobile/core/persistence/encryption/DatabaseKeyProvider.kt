package ng.com.chprbn.mobile.core.persistence.encryption

import android.content.SharedPreferences
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides the SQLCipher passphrase used to encrypt the app's Room databases at rest.
 *
 * On first access, generates a 256-bit random key via [SecureRandom] and persists it
 * (hex-encoded) into the supplied [SharedPreferences]. Subsequent calls return the
 * same key. The [SharedPreferences] is expected to be an EncryptedSharedPreferences
 * instance backed by a hardware-backed [androidx.security.crypto.MasterKey], so the
 * passphrase is itself stored encrypted at rest.
 *
 * Calls are synchronized so concurrent first-launch DB opens cannot race and produce
 * two different keys (which would corrupt the database irrecoverably).
 */
@Singleton
class DatabaseKeyProvider @Inject constructor(
    @DatabaseKeyPrefs private val prefs: SharedPreferences
) {

    @Synchronized
    fun getOrCreatePassphrase(): ByteArray {
        val existingHex = prefs.getString(KEY_PASSPHRASE, null)
        if (existingHex != null) {
            return existingHex.hexToByteArray()
        }

        val bytes = ByteArray(KEY_BYTES).also { SecureRandom().nextBytes(it) }
        // commit() (not apply()) — if the process dies before disk flush we MUST
        // not return a key that hasn't been persisted, or the next launch would
        // generate a different one and the encrypted database would be unreadable.
        prefs.edit().putString(KEY_PASSPHRASE, bytes.toHexString()).commit()
        return bytes
    }

    private fun ByteArray.toHexString(): String {
        val builder = StringBuilder(size * 2)
        for (byte in this) {
            val unsigned = byte.toInt() and 0xff
            builder.append(HEX_CHARS[unsigned ushr 4])
            builder.append(HEX_CHARS[unsigned and 0x0f])
        }
        return builder.toString()
    }

    private fun String.hexToByteArray(): ByteArray {
        require(length == KEY_BYTES * 2) {
            "Persisted database passphrase has unexpected length: $length (expected ${KEY_BYTES * 2})"
        }
        val bytes = ByteArray(length / 2)
        for (i in bytes.indices) {
            val hi = Character.digit(this[i * 2], 16)
            val lo = Character.digit(this[i * 2 + 1], 16)
            require(hi >= 0 && lo >= 0) { "Persisted database passphrase contains non-hex characters" }
            bytes[i] = ((hi shl 4) or lo).toByte()
        }
        return bytes
    }

    private companion object {
        const val KEY_PASSPHRASE = "db_passphrase_v1"
        const val KEY_BYTES = 32
        val HEX_CHARS = "0123456789abcdef".toCharArray()
    }
}
