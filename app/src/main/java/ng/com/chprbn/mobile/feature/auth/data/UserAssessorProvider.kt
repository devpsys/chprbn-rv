package ng.com.chprbn.mobile.feature.auth.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.sync.AssessorProvider
import ng.com.chprbn.mobile.core.sync.dto.AssessorDto
import ng.com.chprbn.mobile.feature.auth.data.local.UserDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DAO-backed [AssessorProvider]. Reads the cached [UserEntity] directly
 * rather than going through `ProfileRepository` — the assessor block is
 * transport-shape data, not a UI concept, and going through the domain
 * layer would force `feature/profile` onto the sync graph for every
 * upload.
 *
 * Yields `null` when either the numeric [UserEntity.assessorId] or
 * `username` is absent (rows carried over from schema < v10, or a
 * partially-populated cache). Remote sources translate that `null` into
 * a fail-fast error so the row stays queued.
 */
@Singleton
class UserAssessorProvider @Inject constructor(
    private val userDao: UserDao,
) : AssessorProvider {

    override suspend fun current(): AssessorDto? = withContext(Dispatchers.IO) {
        val user = userDao.getUser() ?: return@withContext null
        val assessorId = user.assessorId ?: return@withContext null
        if (user.username.isBlank()) return@withContext null
        AssessorDto(
            id = assessorId,
            name = user.fullName,
            email = user.email,
            phone = user.phone,
            username = user.username,
            status = user.status,
            department = user.unit,
            location = user.location,
            roles = user.permissions,
        )
    }
}
