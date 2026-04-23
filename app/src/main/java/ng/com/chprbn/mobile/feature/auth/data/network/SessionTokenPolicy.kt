package ng.com.chprbn.mobile.feature.auth.data.network

/**
 * Rules for tokens that may be sent on authenticated mobile API calls.
 * Demo / seed values must be rejected so we do not hit the API with fake credentials (401).
 */
object SessionTokenPolicy {

    /** Historically seeded in [ng.com.chprbn.mobile.feature.auth.data.local.AuthSeedCallback]. */
    const val LEGACY_SEED_PLACEHOLDER = "offline-token"

    fun isValidForAuthenticatedApi(token: String?): Boolean {
        val t = token?.trim() ?: return false
        if (t.isEmpty()) return false
        if (t.equals(LEGACY_SEED_PLACEHOLDER, ignoreCase = true)) return false
        return true
    }
}
