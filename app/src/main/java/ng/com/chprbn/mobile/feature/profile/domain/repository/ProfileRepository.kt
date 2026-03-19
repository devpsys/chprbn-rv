package ng.com.chprbn.mobile.feature.profile.domain.repository

import ng.com.chprbn.mobile.feature.auth.domain.model.User

/**
 * Domain contract for profile data. Single source of truth: local DB.
 * Supports get profile, update profile (local), and logout (clear session).
 */
interface ProfileRepository {
    /** Current user from local cache; null if not logged in. */
    suspend fun getUserProfile(): User?

    /** Persist profile changes locally (e.g. after edit or API refresh). */
    suspend fun updateUserProfile(user: User)

    /** Clear user/session data from local DB (logout). */
    suspend fun logout()
}
