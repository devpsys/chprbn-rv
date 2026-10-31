package ng.com.chprbn.mobile.feature.accreditation.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.feature.accreditation.data.source.InstitutionRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetInstitutionProfileResult
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.InstitutionRepository
import javax.inject.Inject

class InstitutionRepositoryImpl @Inject constructor(
    private val remote: InstitutionRemoteSource,
) : InstitutionRepository {

    override suspend fun getProfile(institutionId: String): GetInstitutionProfileResult =
        withContext(Dispatchers.IO) {
            try {
                val profile = remote.getProfile(institutionId)
                if (profile == null) {
                    GetInstitutionProfileResult.NotFound(institutionId)
                } else {
                    GetInstitutionProfileResult.Success(profile)
                }
            } catch (t: Throwable) {
                GetInstitutionProfileResult.Error(t.message ?: "Unable to load institution.")
            }
        }
}
