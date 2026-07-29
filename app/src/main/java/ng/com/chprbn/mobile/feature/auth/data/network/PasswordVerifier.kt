package ng.com.chprbn.mobile.feature.auth.data.network

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Password-at-rest verifier used by the offline-login path. On a successful
 * online login the repository derives a PBKDF2-HMAC-SHA256 verifier from the
 * user's password and stores it alongside the cached user; on an offline
 * login attempt it re-derives with the stored salt and compares in constant
 * time. Prevents the pre-A1 hole where any password unlocked a cached
 * username.
 *
 * The stored [Credential.algorithm] string carries both the KDF parameters
 * and the key length so parameters can be tuned later without invalidating
 * existing verifiers — the format is
 * `PBKDF2-HMAC-SHA256:<iterations>:<keyLenBits>`.
 *
 * Injected as a class (not an object) so `AuthRepositoryImpl` can substitute
 * a mock in unit tests without needing the Android runtime for `Base64`.
 */
@Singleton
open class PasswordVerifier @Inject constructor() {

    data class Credential(
        val saltB64: String,
        val verifierB64: String,
        val algorithm: String,
    )

    /** Derive a fresh verifier + random salt for [password]. */
    open fun hash(password: String): Credential {
        val salt = ByteArray(SALT_LENGTH_BYTES).also(random::nextBytes)
        val verifier = derive(password, salt, ITERATIONS, KEY_LENGTH_BITS)
        return Credential(
            saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP),
            verifierB64 = Base64.encodeToString(verifier, Base64.NO_WRAP),
            algorithm = "$ALGORITHM_LABEL:$ITERATIONS:$KEY_LENGTH_BITS",
        )
    }

    /**
     * Constant-time verify [password] against a stored [credential]. Returns
     * `false` on any parse / decode failure — a corrupt credential must never
     * grant access.
     */
    open fun verify(password: String, credential: Credential): Boolean {
        val parts = credential.algorithm.split(":")
        if (parts.size != 3 || parts[0] != ALGORITHM_LABEL) return false
        val iterations = parts[1].toIntOrNull() ?: return false
        val keyLengthBits = parts[2].toIntOrNull() ?: return false
        val salt = runCatching { Base64.decode(credential.saltB64, Base64.NO_WRAP) }
            .getOrNull() ?: return false
        val expected = runCatching { Base64.decode(credential.verifierB64, Base64.NO_WRAP) }
            .getOrNull() ?: return false
        val actual = derive(password, salt, iterations, keyLengthBits)
        // MessageDigest.isEqual is constant-time on all sane JDKs / Android runtimes.
        return MessageDigest.isEqual(actual, expected)
    }

    private fun derive(
        password: String,
        salt: ByteArray,
        iterations: Int,
        keyLengthBits: Int,
    ): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLengthBits)
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private companion object {
        const val ALGORITHM = "PBKDF2WithHmacSHA256"
        const val ALGORITHM_LABEL = "PBKDF2-HMAC-SHA256"
        const val ITERATIONS = 210_000
        const val KEY_LENGTH_BITS = 256
        const val SALT_LENGTH_BYTES = 16
        val random = SecureRandom()
    }
}
